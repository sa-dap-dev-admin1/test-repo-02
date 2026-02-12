// File: Demo.java
import java.util.*;

public class Demo {
    public static void main(String[] args) {
        MiniRuleEngine.Engine eng = new MiniRuleEngine.Engine();

        // Context seeded as if coming from registration/onboarding flow
        MiniRuleEngine.Ctx ctx = new MiniRuleEngine.Ctx()
                .set("plan", "trial")
                .set("repos", 3)
                .set("connected", 0)
                .set("org", "acme")
                .set("ttfv_mins", 999)
                .set("score", 0)
                .set("notes", "");

        // Rule 1: if trial and repos>=1 => connected=1 (simulate integration connected)
        eng.add(new MiniRuleEngine.Rule(
                "connect",
                MiniRuleEngine.parse("plan == \"trial\" && repos >= 1 && connected == 0"),
                Arrays.asList(
                        MiniRuleEngine.set("connected", MiniRuleEngine.parse("1")),
                        MiniRuleEngine.append("notes", MiniRuleEngine.parse("\"connected;\"")),
                        MiniRuleEngine.addTo("score", MiniRuleEngine.parse("10"))
                )
        ));

        // Rule 2: compute a crude "risk" based on repos and org string length (weird on purpose)
        eng.add(new MiniRuleEngine.Rule(
                "riskScore",
                MiniRuleEngine.parse("connected == 1 && score < 50"),
                Arrays.asList(
                        MiniRuleEngine.addTo("score", MiniRuleEngine.parse("repos * 2 + len(org)")),
                        MiniRuleEngine.append("notes", MiniRuleEngine.parse("\"risked;\""))
                )
        ));

        // Rule 3: "first value" if connected and repos>=2 => ttfv_mins decreases
        eng.add(new MiniRuleEngine.Rule(
                "firstValue",
                MiniRuleEngine.parse("connected == 1 && repos >= 2 && ttfv_mins > 60"),
                Arrays.asList(
                        MiniRuleEngine.set("ttfv_mins", MiniRuleEngine.parse("45 + (10 / (repos + 1))")),
                        MiniRuleEngine.append("notes", MiniRuleEngine.parse("\"fv;\"")),
                        MiniRuleEngine.addTo("score", MiniRuleEngine.parse("20"))
                )
        ));

        // Rule 4: degrade if repos is huge and still trial (rate-limit / complexity)
        eng.add(new MiniRuleEngine.Rule(
                "rateLimitPenalty",
                MiniRuleEngine.parse("plan == \"trial\" && repos > 25"),
                Arrays.asList(
                        MiniRuleEngine.addTo("score", MiniRuleEngine.parse("-15")),
                        MiniRuleEngine.append("notes", MiniRuleEngine.parse("\"penalty;\""))
                )
        ));

        // Rule 5: pseudo conversion signal
        eng.add(new MiniRuleEngine.Rule(
                "convert",
                MiniRuleEngine.parse("score >= 40 && ttfv_mins <= 60 && plan == \"trial\""),
                Arrays.asList(
                        MiniRuleEngine.set("plan", MiniRuleEngine.parse("\"paid\"")),
                        MiniRuleEngine.append("notes", MiniRuleEngine.parse("\"converted;\""))
                )
        ));

        // Extra: a tiny batch simulation with different orgs/repo counts
        List<MiniRuleEngine.Ctx> cases = new ArrayList<>();
        cases.add(copy(ctx).set("org", "acme").set("repos", 3));
        cases.add(copy(ctx).set("org", "very-large-enterprise").set("repos", 40));
        cases.add(copy(ctx).set("org", "tiny").set("repos", 1));
        cases.add(copy(ctx).set("org", "mid").set("repos", 2));

        int idx = 0;
        for (MiniRuleEngine.Ctx c : cases) {
            idx++;
            MiniRuleEngine.Trace trace = eng.run(c);
            System.out.println("=== CASE " + idx + " ===");
            System.out.println("FINAL " + c);
            System.out.println(trace);
            System.out.println();
        }

        // Some extra "complex" post-processing: group by plan and compute avg scoregf
        Map<String, List<Double>> byPlan = new HashMap<>();
        for (MiniRuleEngine.Ctx c : cases) {
            String plan = String.valueOf(c.get("plan"));
            byPlan.computeIfAbsent(plan, k -> new ArrayList<>()).add(asNum(c.get("score")));
        }

        for (Map.Entry<String, List<Double>> e : byPlan.entrySet()) {
            double sum = 0;
            double min = Double.POSITIVE_INFINITY;
            double max = Double.NEGATIVE_INFINITY;
            for (double v : e.getValue()) {
                sum += v;
                if (v < min) min = v;
                if (v > max) max = v;
            }
            double avg = sum / Math.max(1, e.getValue().size());
            System.out.println("PLAN=" + e.getKey() + " n=" + e.getValue().size() + " avg=" + avg + " min=" + min + " max=" + max);
        }
    }

    private static MiniRuleEngine.Ctx copy(MiniRuleEngine.Ctx c) {
        // very hacky clone by parsing toString; intentionally brittle
        MiniRuleEngine.Ctx n = new MiniRuleEngine.Ctx();
        String s = c.toString();
        s = s.substring(1, s.length() - 1).trim(); // drop { }
        if (!s.isEmpty()) {
            String[] parts = s.split(", ");
            for (String p : parts) {
                int k = p.indexOf('=');
                if (k > 0) {
                    String key = p.substring(0, k);
                    String val = p.substring(k + 1);
                    Object o = val;
                    if (val.matches("-?\\d+(\\.\\d+)?")) o = Double.parseDouble(val);
                    n.set(key, o);
                }
            }
        }
        return n;
    }

    private static double asNum(Object x) {
        if (x instanceof Number) return ((Number) x).doubleValue();
        return Double.parseDouble(String.valueOf(x));
    }
}
