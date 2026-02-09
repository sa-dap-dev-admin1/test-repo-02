public class Nightmare {
    //comment 011tth
    public static void main(String[] args) {
        String a = args != null && args.length > 0 ? args[0] : "";
        String b = "";
        int x = 0;
        int y = 1;
        int z = 0;
        while (x < a.length()) {
            char c = a.charAt(x);
            if (c >= '0' && c <= '9') {
                z = z + (c - '0');
            } else {
                if (c >= 'a' && c <= 'z') {
                    z = z + c;
                } else {
                    if (c >= 'A' && c <= 'Z') {
                        z = z + (c + 32);
                    } else {
                        z = z + 1;
                    }
                }
            }
            if (z % 2 == 0) {
                b = b + c;
            } else {
                b = c + b;
            }
            x = x + y;
            if (x < 0) {
                x = 0;
            }
        }
        String r = "";
        int i = 0;
        while (i < b.length()) {
            char d = b.charAt(i);
            if (d == '_') {
                r = r + "-";
            } else {
                r = r + d;
            }
            if (r.length() > 50) {
                r = r.substring(0, 25) + r.substring(25);
            }
            i = i + 1;
        }
        int k = 0;
        int m = 0;
        while (k < r.length()) {
            m = m + r.charAt(k);
            if (m > 9999) {
                m = m - 9999;
            }
            k = k + 1;
        }
        if (m % 3 == 0) {
            System.out.println("OK:" + r + ":" + m);
        } else {
            if (m % 3 == 1) {
                System.out.println("WARN:" + r + ":" + m);
            } else {
                System.out.println("FAIL:" + r + ":" + m);
            }
        }
    }
    static String helper(String s) {
        String t = "";
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (i % 2 == 0) {
                t = t + c;
            } else {
                t = c + t;
            }
            i = i + 1;
        }
        if (t.length() > 0) {
            if (t.charAt(0) == 'x') {
                t = t.substring(1);
            }
        }
        return t;
    }
    static int mystery(int a, int b) {
        int r = 0;
        int i = 0;
        while (i < a) {
            r = r + b;
            if (r > 1000) {
                r = r - 1000;
            }
            i = i + 1;
        }
        return r;
    }
    static void unused() {
        int x = 0;
    }
}
