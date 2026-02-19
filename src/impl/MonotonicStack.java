package patterns.java;

import java.util.Arrays;
import java.util.Stack;
import java.util.Optional;

public class MonotonicStack {
    private static final int NO_GREATER_ELEMENT = -1;
    private static final int DEFAULT_WAIT_TIME = 0;

    /**
     * Finds the next greater element for each element in the input array.
     * @param nums The input array of integers.
     * @return An array where each element is the next greater element in the original array.
     */
    public int[] nextGreaterElement(final int[] nums) {
        if (nums == null || nums.length == 0) {
            return new int[0];
        }

        final int n = nums.length;
        final int[] result = new int[n];
        Arrays.fill(result, NO_GREATER_ELEMENT);
        final Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            updateStack(nums, result, stack, i, (index, value) -> value);
        }
        return result;
    }

    /**
     * Calculates the number of days to wait for a warmer temperature.
     * @param temperatures The input array of daily temperatures.
     * @return An array where each element is the number of days to wait for a warmer temperature.
     */
    public int[] dailyTemperatures(final int[] temperatures) {
        if (temperatures == null || temperatures.length == 0) {
            return new int[0];
        }

        final int n = temperatures.length;
        final int[] result = new int[n];
        final Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            updateStack(temperatures, result, stack, i, (index, value) -> index);
        }
        return result;
    }

    /**
     * Updates the stack and result array based on current values.
     * @param values The input array of values.
     * @param result The result array to be updated.
     * @param stack The stack of indices.
     * @param currentIndex The current index being processed.
     * @param resultCalculator A function to calculate the result value.
     */
    private void updateStack(final int[] values, final int[] result, final Stack<Integer> stack, 
                             final int currentIndex, final ResultCalculator resultCalculator) {
        while (!stack.isEmpty() && values[currentIndex] > values[stack.peek()]) {
            final int topIndex = stack.pop();
            result[topIndex] = resultCalculator.calculate(currentIndex, values[currentIndex]) - topIndex;
        }
        stack.push(currentIndex);
    }

    @FunctionalInterface
    private interface ResultCalculator {
        int calculate(int index, int value);
    }
}