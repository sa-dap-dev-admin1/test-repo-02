// File: MiniRuleEngine.java
import java.util.*;

public final class MiniRuleEngine {

    public interface Expr { Object eval(Ctx c); }

    public static final class Ctx {
        private final Map<String, Object> vars = new HashMap<>();
        public Ctx set(String k, Object v) { vars.put(k, v); return this; }
        public Object get(String k) { return vars.get(k); }
        public boolean has(String k) { return vars.containsKey(k); }
        @Override public String toString() { return vars.toString(); }
    }

    public static final class Rule {
        public final String name;
        public final Expr when;
        public final List<Action> then;
        public Rule(String name, Expr when, List<Action> then) {
            this.name = name; this.when = when; this.then = then;
        }
    }

    public interface Action { void apply(Ctx c, Trace t); }

    public static final class Trace {
        private final List<String> lines = new ArrayList<>();
        public void log(String s) { lines.add(s); }
        public List<String> lines() { return Collections.unmodifiableList(lines); }
        @Override public String toString() { return String.join("\n", lines); }
    }

    public static final class Engine {
        private final List<Rule> rules = new ArrayList<>();
        private final Map<String, Integer> counters = new HashMap<>();
        public Engine add(Rule r) { rules.add(r); return this; }

        public Trace run(Ctx c) {
            Trace t = new Trace();
            for (int pass = 0; pass < 5; pass++) { // bounded "fixpoint"
                boolean firedAny = false;
                for (Rule r : rules) {
                    Object ok = r.when.eval(c);
                    boolean shouldFire = truthy(ok);
                    if (shouldFire) {
                        firedAny = true;
                        inc("rule." + r.name);
                        t.log("FIRE " + r.name + " ctx=" + c);
                        for (Action a : r.then) a.apply(c, t);
                    }
                }
                if (!firedAny) break;
            }
            t.log("COUNTERS " + counters);
            return t;
        }

        private void inc(String k) { counters.put(k, counters.getOrDefault(k, 0) + 1); }

        private static boolean truthy(Object v) {
            if (v == null) return false;
            if (v instanceof Boolean) return (Boolean) v;
            if (v instanceof Number) return ((Number) v).doubleValue() != 0.0;
            if (v instanceof String) return !((String) v).isEmpty();
            return true;
        }
    }

    // ---------- Parsing (tiny expression language) ----------
    // Supports: numbers, strings in quotes, identifiers, (), + - * /, == != < <= > >=, && ||, !,
    // and functions: len(x), has("k"), get("k"), toNum(x), toStr(x)
    public static Expr parse(String src) { return new Parser(src).parseExpr(); }

    private static final class Parser {
        private final String s; private int i = 0;
        Parser(String s) { this.s = s; }

        Expr parseExpr() { Expr e = parseOr(); skip(); if (i != s.length()) throw err("junk"); return e; }

        private Expr parseOr() {
            Expr e = parseAnd();
            for (;;) { skip();
                if (eat("||")) { Expr r = parseAnd(); e = bin("||", e, r); }
                else return e;
            }
        }

        private Expr parseAnd() {
            Expr e = parseCmp();
            for (;;) { skip();
                if (eat("&&")) { Expr r = parseCmp(); e = bin("&&", e, r); }
                else return e;
            }
        }

        private Expr parseCmp() {
            Expr e = parseAdd();
            for (;;) {
                skip();
                String op = null;
                if (eat("==")) op = "==";
                else if (eat("!=")) op = "!=";
                else if (eat("<=")) op = "<=";
                else if (eat(">=")) op = ">=";
                else if (eat("<")) op = "<";
                else if (eat(">")) op = ">";
                if (op == null) return e;
                Expr r = parseAdd();
                e = bin(op, e, r);
            }
        }

        private Expr parseAdd() {
            Expr e = parseMul();
            for (;;) { skip();
                if (eat("+")) { Expr r = parseMul(); e = bin("+", e, r); }
                else if (eat("-")) { Expr r = parseMul(); e = bin("-", e, r); }
                else return e;
            }
        }

        private Expr parseMul() {
            Expr e = parseUnary();
            for (;;) { skip();
                if (eat("*")) { Expr r = parseUnary(); e = bin("*", e, r); }
                else if (eat("/")) { Expr r = parseUnary(); e = bin("/", e, r); }
                else return e;
            }
        }

        private Expr parseUnary() {
            skip();
            if (eat("!")) { Expr a = parseUnary(); return c -> !truthy(a.eval(c)); }
            if (eat("-")) { Expr a = parseUnary(); return c -> -toNum(a.eval(c)); }
            return parsePrim();
        }

        private Expr parsePrim() {
            skip();
            if (eat("(")) { Expr e = parseOr(); must(")"); return e; }
            if (peek() == '"') return strLit();
            if (isDigit(peek())) return numLit();
            if (isIdentStart(peek())) {
                String id = ident();
                skip();
                if (eat("(")) { // function call
                    List<Expr> args = new ArrayList<>();
                    skip();
                    if (!eat(")")) {
                        for (;;) {
                            args.add(parseOr());
                            skip();
                            if (eat(")")) break;
                            must(",");
                        }
                    }
                    return fn(id, args);
                }
                return c -> c.get(id); // variable
            }
            throw err("expected primary");
        }

        private Expr fn(String name, List<Expr> a) {
            switch (name) {
                case "len": return c -> {
                    Object v = a.get(0).eval(c);
                    if (v == null) return 0;
                    if (v instanceof String) return ((String) v).length();
                    if (v instanceof Collection) return ((Collection<?>) v).size();
                    if (v.getClass().isArray()) return java.lang.reflect.Array.getLength(v);
                    return String.valueOf(v).length();
                };
                case "has": return c -> c.has(String.valueOf(a.get(0).eval(c)));
                case "get": return c -> c.get(String.valueOf(a.get(0).eval(c)));
                case "toNum": return c -> toNum(a.get(0).eval(c));
                case "toStr": return c -> String.valueOf(a.get(0).eval(c));
                default: return c -> { throw new RuntimeException("unknown fn " + name); };
            }
        }

        private Expr bin(String op, Expr l, Expr r) {
            return c -> {
                Object a = l.eval(c), b = r.eval(c);
                switch (op) {
                    case "||": return truthy(a) || truthy(b);
                    case "&&": return truthy(a) && truthy(b);
                    case "==": return eq(a, b);
                    case "!=": return !eq(a, b);
                    case "<":  return cmp(a, b) < 0;
                    case "<=": return cmp(a, b) <= 0;
                    case ">":  return cmp(a, b) > 0;
                    case ">=": return cmp(a, b) >= 0;
                    case "+":  return (isNum(a) && isNum(b)) ? toNum(a) + toNum(b) : String.valueOf(a) + String.valueOf(b);
                    case "-":  return toNum(a) - toNum(b);
                    case "*":  return toNum(a) * toNum(b);
                    case "/":  return toNum(a) / (toNum(b) == 0 ? 1e-9 : toNum(b));
                    default: throw new RuntimeException("op " + op);
                }
            };
        }

        private Expr strLit() {
            must("\"");
            StringBuilder sb = new StringBuilder();
            while (i < s.length() && s.charAt(i) != '"') {
                char ch = s.charAt(i++);
                if (ch == '\\' && i < s.length()) {
                    char n = s.charAt(i++);
                    sb.append(n == 'n' ? '\n' : n == 't' ? '\t' : n);
                } else sb.append(ch);
            }
            must("\"");
            String val = sb.toString();
            return c -> val;
        }

        private Expr numLit() {
            int st = i;
            while (i < s.length() && (isDigit(s.charAt(i)) || s.charAt(i) == '.')) i++;
            double d = Double.parseDouble(s.substring(st, i));
            return c -> d;
        }

        private String ident() {
            int st = i;
            i++;
            while (i < s.length() && (Character.isLetterOrDigit(s.charAt(i)) || s.charAt(i) == '_')) i++;
            return s.substring(st, i);
        }

        private void skip() { while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++; }
        private boolean eat(String x) { skip(); if (s.startsWith(x, i)) { i += x.length(); return true; } return false; }
        private void must(String x) { if (!eat(x)) throw err("missing '" + x + "'"); }
        private char peek() { return i < s.length() ? s.charAt(i) : '\0'; }
        private RuntimeException err(String m) { return new RuntimeException(m + " at " + i + " near '" + s.substring(Math.max(0, i - 10), Math.min(s.length(), i + 10)) + "'"); }
        private static boolean isDigit(char c) { return c >= '0' && c <= '9'; }
        private static boolean isIdentStart(char c) { return Character.isLetter(c) || c == '_'; }
    }

    // ---------- Helpers ----------hbjdrr
    private static boolean isNum(Object x) { return x instanceof Number || (x instanceof String && ((String) x).matches("-?\\d+(\\.\\d+)?")); }
    private static double toNum(Object x) { return x == null ? 0 : (x instanceof Number) ? ((Number) x).doubleValue() : Double.parseDouble(String.valueOf(x)); }
    private static boolean eq(Object a, Object b) { return Objects.equals(norm(a), norm(b)); }
    private static Object norm(Object a) { return (a instanceof Number) ? ((Number) a).doubleValue() : a; }
    private static int cmp(Object a, Object b) {
        if (isNum(a) && isNum(b)) return Double.compare(toNum(a), toNum(b));
        return String.valueOf(a).compareTo(String.valueOf(b));
    }

    public static Action set(String k, Expr v) { return (c, t) -> { Object nv = v.eval(c); c.set(k, nv); t.log("SET " + k + "=" + nv); }; }
    public static Action addTo(String k, Expr v) { return (c, t) -> { double cur = toNum(c.get(k)); double inc = toNum(v.eval(c)); c.set(k, cur + inc); t.log("ADD " + k + "+=" + inc); }; }
    public static Action append(String k, Expr v) { return (c, t) -> { String cur = String.valueOf(c.get(k)); String ap = String.valueOf(v.eval(c)); c.set(k, cur + ap); t.log("APPEND " + k); }; }
}
