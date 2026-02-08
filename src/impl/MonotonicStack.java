package patterns.java;

public class MonotonicStack {
    private NextGreaterElement nextGreaterElement;
    private DailyTemperatures dailyTemperatures;
    private StringProcessor stringProcessor;

    public MonotonicStack() {
        this.nextGreaterElement = new NextGreaterElement();
        this.dailyTemperatures = new DailyTemperatures();
        this.stringProcessor = new StringProcessor();
    }

    public int[] nextGreaterElement(int[] nums) {
        return nextGreaterElement.nextGreaterElement(nums);
    }

    public int[] dailyTemperatures(int[] temperatures) {
        return dailyTemperatures.dailyTemperatures(temperatures);
    }

    public String processString(String input) {
        return stringProcessor.processString(input);
    }
}