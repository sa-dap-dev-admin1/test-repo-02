import java.util.function.Function;

public class Nightmare {
    public static void main(String[] args) {
        String input = args != null && args.length > 0 ? args[0] : "";
        String processedString = StringProcessor.processInput(input);
        int checksum = NumberCalculator.calculateChecksum(processedString);
        OutputStrategy outputStrategy = OutputStrategyFactory.getStrategy(checksum);
        System.out.println(outputStrategy.generateOutput(processedString, checksum));
    }
}

interface OutputStrategy {
    String generateOutput(String processedString, int checksum);
}

class OkOutputStrategy implements OutputStrategy {
    public String generateOutput(String processedString, int checksum) {
        return "OK:" + processedString + ":" + checksum;
    }
}

class WarnOutputStrategy implements OutputStrategy {
    public String generateOutput(String processedString, int checksum) {
        return "WARN:" + processedString + ":" + checksum;
    }
}

class FailOutputStrategy implements OutputStrategy {
    public String generateOutput(String processedString, int checksum) {
        return "FAIL:" + processedString + ":" + checksum;
    }
}

class OutputStrategyFactory {
    public static OutputStrategy getStrategy(int checksum) {
        switch (checksum % 3) {
            case 0: return new OkOutputStrategy();
            case 1: return new WarnOutputStrategy();
            default: return new FailOutputStrategy();
        }
    }
}