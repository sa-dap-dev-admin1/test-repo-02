package LogAnalyzer;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class LogFileComparator implements Comparator<Path> {

    private static final Pattern DATE_PATTERN = Pattern.compile("\\.(\\d{2})-(\\d+)");
    private static final String CURRENT_LOG = "integrator-server.log";

    @Override
    public int compare(Path p1, Path p2) {
        String n1 = p1.getFileName().toString();
        String n2 = p2.getFileName().toString();

        if (n1.equals(CURRENT_LOG)) return 1;
        if (n2.equals(CURRENT_LOG)) return -1;

        int score1 = getChronologicalScore(n1);
        int score2 = getChronologicalScore(n2);

        if (score1 != score2) return Integer.compare(score1, score2);
        return n1.compareTo(n2);
    }

    private int getChronologicalScore(String filename) {
        Matcher m = DATE_PATTERN.matcher(filename);
        if (!m.find()) return 999999;
        try {
            int day = Integer.parseInt(m.group(1));
            int rotation = Integer.parseInt(m.group(2));
            int monthOffset = (day > 20) ? 0 : 100;
            return (monthOffset + day) * 100 + rotation;
        } catch (Exception e) {
            return 999999;
        }
    }
}
