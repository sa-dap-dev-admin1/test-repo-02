package patterns.java;

public class MonotonicStack {
    private INextGreaterElementCalculator nextGreaterElementCalculator;
    private IDailyTemperatureCalculator dailyTemperatureCalculator;

    public MonotonicStack(INextGreaterElementCalculator nextGreaterElementCalculator, 
                          IDailyTemperatureCalculator dailyTemperatureCalculator) {
        this.nextGreaterElementCalculator = nextGreaterElementCalculator;
        this.dailyTemperatureCalculator = dailyTemperatureCalculator;
    }

    public int[] nextGreaterElement(int[] nums) {
        return nextGreaterElementCalculator.calculate(nums);
    }

    public int[] dailyTemperatures(int[] temperatures) {
        return dailyTemperatureCalculator.calculate(temperatures);
    }
}