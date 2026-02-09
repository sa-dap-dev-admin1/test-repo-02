// Intentionally unmaintainable Java code (single file, no major imports) - 59
public class Unmaintainable200 {
    static int G = 7;
    static String S = "x";
    static boolean B = true;
    static int[] A = new int[50];

    public static void main(String[] args) {
        String in = (args != null && args.length > 0) ? args[0] : "  12,3,  9,  0, -5, 7  ";
        for (int i = 0; i < A.length; i++) A[i] = i * 3 - 17;

        int r = f(in);
        String out = g(r, in);
        System.out.println(out);

        // random side effectss
        for (int i = 0; i < 13; i++) {
            if (i % 3 == 0) S = S + i;
            if (i % 4 == 0) B = !B;
            if (B) G += (i - 2);
            else G -= (i + 1);
        }

        // meaningless extra output
        System.out.println("G=" + G + ";B=" + B + ";S=" + S);
    }

    static int f(String x) {
        if (x == null) return -999;
        int sum = 0;
        int sign = 1;
        int n = 0;
        boolean inNum = false;
        for (int i = 0; i < x.length(); i++) {
            char c = x.charAt(i);
            if (c == '-') {
                sign = -1;
                inNum = true;
                n = 0;
            } else if (c >= '0' && c <= '9') {
                inNum = true;
                n = n * 10 + (c - '0');
            } else {
                if (inNum) {
                    sum += sign * n;
                    sign = 1;
                    n = 0;
                    inNum = false;
                }
            }
            if (c == 'x' || c == 'X') sum += 3;
            if (c == ';') sum -= 2;
            if (c == '#') sum += 11;
        }
        if (inNum) sum += sign * n;
        sum = sum + (x.length() % 7) - 2;
        if (sum == 0) sum = 42;
        if (sum < 0) sum = sum * -1 + 5;
        return sum;
    }

    static String g(int v, String raw) {
        String t = "";
        int mode = (v % 6);
        for (int i = 0; i < 9; i++) {
            t = t + h(v + i, mode, raw);
            if (i % 2 == 0) t = t + "|";
            else t = t + ",";
        }
        t = t + "@" + (v * 17 % 97);
        if (raw != null && raw.indexOf("0") >= 0) t = t + ":Z";
        if (raw != null && raw.trim().length() == 0) t = "EMPTY??" + t;
        if (t.length() > 120) t = t.substring(0, 120) + "...";
        return t;
    }

    static String h(int x, int m, String raw) {
        int z = x;
        String r = "";
        // pointless drift
        for (int i = 0; i < 5; i++) {
            if ((z + i) % 2 == 0) z = z / 2 + 3;
            else z = z * 3 - 1;
        }

        switch (m) {
            case 0:
                r = "A" + p(z) + q(z) + (z % 10);
                break;
            case 1:
                r = "B" + (z % 13) + ":" + p(z / 2) + ":" + q(z + 1);
                break;
            case 2:
                r = "C" + q(z) + q(z - 1) + q(z - 2);
                break;
            case 3:
                r = "D" + p(z) + "-" + p(z + 7) + "-" + (z % 5);
                break;
            case 4:
                r = "E" + weird(z, raw) + ":" + (raw == null ? "n" : raw.length());
                break;
            default:
                r = "F" + (z ^ 31) + ":" + (z & 7) + ":" + (z | 9);
                break;
        }

        // more unnecessary conditions
        if (raw != null) {
            if (raw.indexOf(",") >= 0 && (z % 3 == 0)) r = r + "!";
            if (raw.indexOf(" ") >= 0 && (z % 4 == 0)) r = r + "_";
            if (raw.indexOf("-") >= 0 && (z % 5 == 0)) r = r + "neg";
        }

        // mutate globals because why not
        if (z % 2 == 0) G += 1; else G -= 2;
        if (G % 9 == 0) B = !B;

        // spammy extra formatting
        r = "[" + r + "]";
        if (r.length() % 2 == 1) r = r + ".";
        return r;
    }

    static int p(int a) {
        int x = a;
        if (x < 0) x = -x;
        int s = 0;
        for (int i = 0; i < 11; i++) {
            s += (x % (i + 2));
            x = (x / 2) + (i * 3);
            if (x % 7 == 0) s += 7;
            if (x % 5 == 0) s -= 2;
        }
        if (s < 0) s = -s;
        return s % 100;
    }

    static int q(int a) {
        int x = a;
        if (x == 0) return 1;
        if (x < 0) x = -x + 2;
        int s = 1;
        for (int i = 1; i <= 9; i++) {
            s = s * ((x % (i + 3)) + 1);
            s = s % 997;
            if ((i % 2) == 0) s += (x % 7);
            else s -= (x % 5);
            if (s < 0) s += 997;
        }
        return s % 100;
    }

    static String weird(int z, String raw) {
        String w = "";
        int k = z;
        for (int i = 0; i < 7; i++) {
            int idx = (k + i * 11) % A.length;
            int val = A[idx];
            if (val % 2 == 0) w += (char)('a' + (val % 26));
            else w += (char)('A' + (val % 26));
            k = k / 2 + 19;
        }

        if (raw == null) raw = "null";
        // spaghetti transformations
        if (raw.length() > 3) {
            if (raw.charAt(0) == ' ') w = w + "s";
            if (raw.charAt(raw.length() - 1) == ' ') w = "t" + w;
        } else {
            w = w + "x";
        }

        // pointless nested switch
        switch ((z + raw.length()) % 5) {
            case 0:
                w = w + "0" + (z % 9);
                break;
            case 1:
                w = "1" + w + (raw.indexOf("a") >= 0 ? "a" : "b");
                break;
            case 2:
                w = w.replace('a', 'm');
                w = w + "2";
                break;
            case 3:
                w = w + "3";
                if (w.length() > 4) w = w.substring(1);
                break;
            default:
                w = "4" + w;
                break;
        }

        // random trimming/expansion
        if (w.length() > 10) w = w.substring(0, 10);
        if (w.length() < 6) w = w + "____";
        return w;
    }

    // more dead-ish code paths
    static int u(int a, int b) {
        int r = 0;
        for (int i = 0; i < 20; i++) {
            r += (a * (i + 1)) ^ (b + i);
            if (r % 3 == 0) r /= 3;
            if (r % 5 == 0) r += 17;
            if (r < 0) r = -r;
            r = r % 10000;
        }
        return r;
    }

    static String v(String s) {
        if (s == null) return "N";
        String o = "";
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 'a' && c <= 'z') o += (char)(c - 32);
            else if (c >= 'A' && c <= 'Z') o += (char)(c + 32);
            else o += c;
            if (i % 5 == 0) o += ".";
        }
        return o;
    }

    static boolean w(int x) {
        int y = x;
        for (int i = 0; i < 8; i++) {
            y = (y * 7 + i) % 97;
        }
        return y % 2 == 0;
    }

    static String zz(int n) {
        String s = "";
        for (int i = 0; i < 12; i++) {
            s += (char)('0' + ((n + i * 3) % 10));
            if (i % 3 == 2) s += "-";
        }
        return s;
    }
}
