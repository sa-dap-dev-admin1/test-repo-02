package patterns.java;

import java.util.Arrays;
import java.util.Stack;

public class MonotonicStack {

    /**
     * Finds the next greater element for each element in the input array.
     *
     * @param nums The input array of integers.
     * @return An array where each element is the next greater element for the corresponding input element.
     */
    public int[] nextGreaterElement(int[] nums) {
        int n = nums.length;
        int[] result = new int[n];
        Arrays.fill(result, -1); // Default to -1 if no greater element exists
        return processMonotonicStack(nums, result, (current, top) -> current > top);
    }

    /**
     * Calculates the number of days to wait for a warmer temperature.
     *
     * @param temperatures The input array of daily temperatures.
     * @return An array where each element represents the number of days to wait for a warmer temperature.
     */
    public int[] dailyTemperatures(int[] temperatures) {
        int n = temperatures.length;
        int[] result = new int[n];
        return processMonotonicStack(temperatures, result, (current, top) -> current > top);
    }

    /**
     * Processes the input array using a monotonic stack approach.
     *
     * @param input The input array to process.
     * @param result The result array to populate.
     * @param comparison A lambda function defining the comparison logic for the monotonic stack.
     * @return The populated result array.
     */
    private int[] processMonotonicStack(int[] input, int[] result, ComparisonStrategy comparison) {
        Stack<Integer> stack = new Stack<>();
        for (int i = 0; i < input.length; i++) {
            while (!stack.isEmpty() && comparison.compare(input[i], input[stack.peek()])) {
                int prevIndex = stack.pop();
                result[prevIndex] = (result[prevIndex] == 0) ? i - prevIndex : input[i];
            }
            stack.push(i);
        }
        return result;
    }

    /**
     * Functional interface for defining comparison strategy in monotonic stack processing.
     */
    @FunctionalInterface
    private interface ComparisonStrategy {
        boolean compare(int current, int top);
    }
}