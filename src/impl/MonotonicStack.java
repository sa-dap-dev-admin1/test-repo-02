package patterns.java;

import java.util.Arrays;
import java.util.Stack;

/**
 * This class implements monotonic stack algorithms for solving array-based problems.
 * It provides methods to find the next greater element and calculate daily temperatures.
 */
public class MonotonicStack {
    private static final int NO_NEXT_GREATER_ELEMENT = -1;

    /**
     * Finds the next greater element for each element in the input array.
     *
     * @param nums The input array of integers
     * @return An array where each element is the next greater element in the original array
     */
    public int[] nextGreaterElement(int[] nums) {
        if (nums == null || nums.length == 0) {
            return new int[0];
        }

        int n = nums.length;
        int[] result = new int[n];
        Arrays.fill(result, NO_NEXT_GREATER_ELEMENT);
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            updateMonotonicStack(nums, result, stack, i, (index, value) -> result[index] = value);
        }
        return result;
    }

    /**
     * Calculates the number of days to wait for a warmer temperature.
     *
     * @param temperatures The input array of daily temperatures
     * @return An array where each element is the number of days to wait for a warmer temperature
     */
    public int[] dailyTemperatures(int[] temperatures) {
        if (temperatures == null || temperatures.length == 0) {
            return new int[0];
        }

        int n = temperatures.length;
        int[] result = new int[n];
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            updateMonotonicStack(temperatures, result, stack, i, (index, value) -> result[index] = i - index);
        }
        return result;
    }

    /**
     * Updates the monotonic stack and result array based on the current element.
     *
     * @param values The input array of values
     * @param result The result array to be updated
     * @param stack The monotonic stack of indices
     * @param currentIndex The current index being processed
     * @param resultUpdater A functional interface to update the result array
     */
    private void updateMonotonicStack(int[] values, int[] result, Stack<Integer> stack, int currentIndex, ResultUpdater resultUpdater) {
        while (!stack.isEmpty() && values[currentIndex] > values[stack.peek()]) {
            int topIndex = stack.pop();
            resultUpdater.update(topIndex, values[currentIndex]);
        }
        stack.push(currentIndex);
    }

    /**
     * Functional interface for updating the result array.
     */
    @FunctionalInterface
    private interface ResultUpdater {
        void update(int index, int value);
    }
}