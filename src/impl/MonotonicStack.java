package patterns.java;

import java.util.Arrays;
import java.util.Stack;

public class MonotonicStack {
    private static final int NO_GREATER_ELEMENT = -1;

    /**
     * Finds the next greater element for each element in the input array.
     *
     * @param nums The input array of integers
     * @return An array where each element is the next greater element for the corresponding input element
     * @throws IllegalArgumentException if the input array is null
     */
    public int[] nextGreaterElement(int[] nums) {
        if (nums == null) {
            throw new IllegalArgumentException("Input array cannot be null");
        }
        int n = nums.length;
        int[] result = new int[n];
        Arrays.fill(result, NO_GREATER_ELEMENT);
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            updateStack(nums, result, stack, i, (index, value) -> value);
        }
        return result;
    }

    /**
     * Calculates the number of days to wait for a warmer temperature.
     *
     * @param temperatures The input array of daily temperatures
     * @return An array where each element represents the number of days to wait for a warmer temperature
     * @throws IllegalArgumentException if the input array is null
     */
    public int[] dailyTemperatures(int[] temperatures) {
        if (temperatures == null) {
            throw new IllegalArgumentException("Input array cannot be null");
        }
        int n = temperatures.length;
        int[] result = new int[n];
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            updateStack(temperatures, result, stack, i, (index, value) -> index - value);
        }
        return result;
    }

    private void updateStack(int[] values, int[] result, Stack<Integer> stack, int currentIndex, ResultUpdater updater) {
        while (!stack.isEmpty() && values[currentIndex] > values[stack.peek()]) {
            int topIndex = stack.pop();
            result[topIndex] = updater.update(currentIndex, topIndex);
        }
        stack.push(currentIndex);
    }

    @FunctionalInterface
    private interface ResultUpdater {
        int update(int currentIndex, int topIndex);
    }
}