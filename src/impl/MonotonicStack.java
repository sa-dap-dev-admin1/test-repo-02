package patterns.java;

import java.util.Arrays;
import java.util.Stack;

public class MonotonicStack {
    private static final int NO_GREATER_ELEMENT = -1;

    /**
     * Finds the next greater element for each element in the input array.
     * Uses a monotonic stack approach.
     *
     * @param nums Input array of integers
     * @return Array where each element is the next greater element for the corresponding input element
     */
    public int[] nextGreaterElement(int[] nums) {
        if (nums == null || nums.length == 0) {
            return new int[0];
        }

        int n = nums.length;
        int[] result = new int[n];
        Arrays.fill(result, NO_GREATER_ELEMENT);
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            updateMonotonicStack(nums, result, stack, i, (index, value) -> value);
        }
        return result;
    }

    /**
     * Calculates the number of days to wait for a warmer temperature.
     * Uses a monotonic decreasing stack approach.
     *
     * @param temperatures Array of daily temperatures
     * @return Array where each element is the number of days to wait for a warmer temperature
     */
    public int[] dailyTemperatures(int[] temperatures) {
        if (temperatures == null || temperatures.length == 0) {
            return new int[0];
        }

        int n = temperatures.length;
        int[] result = new int[n];
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            updateMonotonicStack(temperatures, result, stack, i, (index, value) -> index);
        }

        return result;
    }

    /**
     * Updates the monotonic stack based on the current element.
     *
     * @param values Array of values being processed
     * @param result Result array to be updated
     * @param stack Monotonic stack storing indices
     * @param currentIndex Current index being processed
     * @param resultCalculator Function to calculate the result value
     */
    private void updateMonotonicStack(int[] values, int[] result, Stack<Integer> stack, int currentIndex,
                                      ResultCalculator resultCalculator) {
        while (!stack.isEmpty() && values[currentIndex] > values[stack.peek()]) {
            int topIndex = stack.pop();
            result[topIndex] = resultCalculator.calculate(currentIndex, topIndex);
        }
        stack.push(currentIndex);
    }

    /**
     * Functional interface for calculating result values.
     */
    @FunctionalInterface
    private interface ResultCalculator {
        int calculate(int currentIndex, int topIndex);
    }
}