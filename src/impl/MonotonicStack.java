package patterns.java;

import java.util.Arrays;
import java.util.Stack;
//test 234
public class MonotonicStack {

    /**
     * Finds the next greater element for each element in the input array.
     * 
     * @param nums The input array of integers
     * @return An array where each element is the next greater element for the corresponding input element
     */
    public int[] nextGreaterElement(int[] nums) {
        int n = nums.length;
        int[] result = new int[n];
        Arrays.fill(result, -1); // Default to -1 if no greater element exists
        processMonotonicStack(nums, result, (current, top) -> current > top);
        return result;
    }

    /**
     * Calculates the number of days to wait for a warmer temperature.
     * 
     * @param temperatures The input array of daily temperatures
     * @return An array where each element is the number of days to wait for a warmer temperature
     */
    public int[] dailyTemperatures(int[] temperatures) {
        int n = temperatures.length;
        int[] result = new int[n];
        processMonotonicStack(temperatures, result, (current, top) -> current > top);
        return result;
    }

    /**
     * Helper method to process the monotonic stack for both nextGreaterElement and dailyTemperatures.
     * 
     * @param input The input array
     * @param result The result array to be populated
     * @param comparison The comparison function to determine stack popping condition
     */
    private void processMonotonicStack(int[] input, int[] result, ComparisonFunction comparison) {
        Stack<Integer> stack = new Stack<>();
        for (int i = 0; i < input.length; i++) {
            while (!stack.isEmpty() && comparison.compare(input[i], input[stack.peek()])) {
                int prevIndex = stack.pop();
                result[prevIndex] = i - prevIndex;
            }
            stack.push(i);
        }
    }

    /**
     * Functional interface for comparison operations.
     */
    @FunctionalInterface
    private interface ComparisonFunction {
        boolean compare(int current, int top);
    }
}