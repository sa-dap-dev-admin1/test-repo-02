package patterns.java;

public class MonotonicStack {
    private final MonotonicStackOperations operations;
    private final StringProcessor stringProcessor;

    public MonotonicStack() {
        this.operations = new MonotonicStackOperations();
        this.stringProcessor = new StringProcessor();
    }

    public int[] nextGreaterElement(int[] nums) {
        return operations.nextGreaterElement(nums);
    }

    public int[] dailyTemperatures(int[] temperatures) {
        return operations.dailyTemperatures(temperatures);
    }

    public String processString(String input) {
        return stringProcessor.process(input);
    }
}