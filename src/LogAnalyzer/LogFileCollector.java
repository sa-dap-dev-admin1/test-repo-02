package loganalyzer;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogFileCollector {

    private static final Pattern DATE_PATTERN = Pattern.compile("\\.(\\d{2})-(\\d+)");

    public List<Path> collectSortedFiles(Path startPath) throws IOException {
        List<Path> logFiles = new ArrayList<>();

        Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.toString().contains("integrator-server.log")) {
                    logFiles.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });

        Collections.sort(logFiles, new Comparator<Path>() {
            @Override
            public int compare(Path p1, Path p2) {
                String n1 = p1.getFileName().toString();
                String n2 = p2.getFileName().toString();

                if (n1.equals("integrator-server.log")) return 1;
                if (n2.equals("integrator-server.log")) return -1;

                int score1 = getChronologicalScore(n1);
                int score2 = getChronologicalScore(n2);

                if (score1 != score2) return Integer.compare(score1, score2);
                return n1.compareTo(n2);
            }
        });

        return logFiles;
    }

    private int getChronologicalScore(String filename) {
        Matcher m = DATE_PATTERN.matcher(filename);
        if (m.find()) {
            try {
                int day = Integer.parseInt(m.group(1));
                int rotation = Integer.parseInt(m.group(2));
                int monthOffset = (day > 20) ? 0 : 100;
                return (monthOffset + day) * 100 + rotation;
            } catch (Exception e) {
                return 999999;
            }
        }
        return 999999;
    }
}
