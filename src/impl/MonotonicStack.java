package patterns.java;

import java.util.Arrays;
import java.util.Stack;

public class MonotonicStack {
    private static final int NO_GREATER_ELEMENT = -1;

    /**
     * Finds the next greater element for each element in the array.
     * Uses a monotonic stack pattern for efficient O(n) time complexity.
     *
     * @param nums Input array of integers
     * @return Array where each element is the next greater element in the original array
     */
    public int[] nextGreaterElement(int[] nums) {
        int n = nums.length;
        int[] result = new int[n];
        Arrays.fill(result, NO_GREATER_ELEMENT);
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            updateStack(nums, result, stack, i, (index, value) -> result[index] = value);
        }
        return result;
    }

    /**
     * Calculates the number of days to wait for a warmer temperature.
     * Uses a monotonic decreasing stack for efficient O(n) time complexity.
     *
     * @param temperatures Array of daily temperatures
     * @return Array where each element is the number of days to wait for a warmer temperature
     */
    public int[] dailyTemperatures(int[] temperatures) {
        int n = temperatures.length;
        int[] result = new int[n];
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            updateStack(temperatures, result, stack, i, (index, value) -> result[index] = value - index);
        }
        return result;
    }

    /**
     * Updates the monotonic stack and result array based on the current value.
     * This method encapsulates the common logic used in both nextGreaterElement and dailyTemperatures.
     *
     * @param values Array of input values
     * @param result Array to store results
     * @param stack Monotonic stack of indices
     * @param currentIndex Current index being processed
     * @param resultUpdater Functional interface to update the result array
     */
    private void updateStack(int[] values, int[] result, Stack<Integer> stack, int currentIndex, ResultUpdater resultUpdater) {
        while (!stack.isEmpty() && values[currentIndex] > values[stack.peek()]) {
            int topIndex = stack.pop();
            resultUpdater.update(topIndex, currentIndex);
        }
        stack.push(currentIndex);
    }

    /**
     * Functional interface for updating the result array.
     * Allows for flexible result calculation in different contexts.
     */
    @FunctionalInterface
    private interface ResultUpdater {
        void update(int index, int value);
    }
}