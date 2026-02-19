package patterns.java;

import java.util.Arrays;
import java.util.Stack;

public class MonotonicStack {
    private static final int NO_GREATER_ELEMENT = -1;

    /**
     * Finds the next greater element for each element in the input array.
     * @param nums The input array of integers.
     * @return An array with the next greater elements or -1 if none exists.
     */
    public int[] nextGreaterElement(int[] nums) {
        if (nums == null || nums.length == 0) {
            return new int[0];
        }
        int n = nums.length;
        int[] result = new int[n]; // Output array
        Arrays.fill(result, NO_GREATER_ELEMENT); // Default to -1 if no greater element exists
        processMonotonicStack(nums, result, (i, j) -> nums[i] > nums[j], (i, j) -> nums[i]);
        return result;
    }

    /**
     * Calculates the number of days to wait for a warmer temperature.
     * @param temperatures The input array of daily temperatures.
     * @return An array with the wait times for warmer temperatures.
     */
    public int[] dailyTemperatures(int[] temperatures) {
        if (temperatures == null || temperatures.length == 0) {
            return new int[0];
        }
        int n = temperatures.length;
        int[] result = new int[n]; // Result array initialized with 0s
        processMonotonicStack(temperatures, result, (i, j) -> temperatures[i] > temperatures[j], (i, j) -> i - j);
        return result; // Return the computed results
    }

    /**
     * Helper method to process the monotonic stack pattern.
     * @param values The input array of values.
     * @param result The result array to be filled.
     * @param condition The condition for maintaining the monotonic property.
     * @param computation The computation to be performed when condition is met.
     */
    private void processMonotonicStack(int[] values, int[] result, MonotonicCondition condition, ResultComputation computation) {
        Stack<Integer> stack = new Stack<>(); // Stack stores indices
        // Iterate through the array
        for (int i = 0; i < values.length; i++) {
            // While stack is not empty and condition is met
            while (!stack.isEmpty() && condition.test(i, stack.peek())) {
                int index = stack.pop(); // Pop the top element
                result[index] = computation.compute(i, index); // Compute the result
            }
            stack.push(i); // Push the current index onto the stack
        }
    }

    /**
     * Functional interface for the monotonic condition.
     */
    @FunctionalInterface
    private interface MonotonicCondition {
        boolean test(int currentIndex, int stackTopIndex);
    }

    /**
     * Functional interface for the result computation.
     */
    @FunctionalInterface
    private interface ResultComputation {
        int compute(int currentIndex, int stackTopIndex);
    }
}