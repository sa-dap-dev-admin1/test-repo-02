package patterns.java;

import java.util.Arrays;
import java.util.Stack;

public class MonotonicStack {

    /**
     * Finds the next greater element for each element in the input array.
     * @param nums The input array of integers.
     * @return An array where each element is the next greater element in the original array,
     *         or -1 if no such element exists.
     */
    public int[] nextGreaterElement(int[] nums) {
        int n = nums.length;
        int[] result = new int[n];
        Arrays.fill(result, -1);
        return processMonotonicStack(nums, result, (current, top) -> current > top);
    }

    /**
     * Calculates the number of days to wait for a warmer temperature.
     * @param temperatures The input array of daily temperatures.
     * @return An array where each element represents the number of days to wait
     *         for a warmer temperature, or 0 if no such day exists.
     */
    public int[] dailyTemperatures(int[] temperatures) {
        int n = temperatures.length;
        int[] result = new int[n];
        return processMonotonicStack(temperatures, result, (current, top) -> current > top);
    }

    /**
     * Generic method to process arrays using a monotonic stack.
     * @param input The input array to process.
     * @param result The result array to populate.
     * @param comparison A lambda function defining the comparison logic for the monotonic property.
     * @return The populated result array.
     */
    private int[] processMonotonicStack(int[] input, int[] result, ComparisonFunction comparison) {
        Stack<Integer> stack = new Stack<>();
        
        for (int i = 0; i < input.length; i++) {
            while (!stack.isEmpty() && comparison.compare(input[i], input[stack.peek()])) {
                int prevIndex = stack.pop();
                result[prevIndex] = i - prevIndex;
            }
            stack.push(i);
        }
        
        return result;
    }

    /**
     * Functional interface for defining comparison logic in the monotonic stack.
     */
    @FunctionalInterface
    private interface ComparisonFunction {
        boolean compare(int current, int top);
    }
}