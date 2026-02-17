package patterns.java;

import java.util.Arrays;
import java.util.Stack;

public class MonotonicStack {
    private static final int NO_GREATER_ELEMENT = -1;

    public int[] nextGreaterElement(int[] nums) {
        int n = nums.length;
        int[] result = new int[n];
        Arrays.fill(result, NO_GREATER_ELEMENT);
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            updateMonotonicStack(nums, result, stack, i, (index, value) -> value);
        }
        return result;
    }

    public int[] dailyTemperatures(int[] temperatures) {
        int n = temperatures.length;
        int[] result = new int[n];
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            updateMonotonicStack(temperatures, result, stack, i, (index, value) -> index - value);
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
     * @param resultCalculator A function to calculate the result value
     */
    private void updateMonotonicStack(int[] values, int[] result, Stack<Integer> stack, int currentIndex, ResultCalculator resultCalculator) {
        while (!stack.isEmpty() && values[currentIndex] > values[stack.peek()]) {
            int topIndex = stack.pop();
            result[topIndex] = resultCalculator.calculate(currentIndex, topIndex);
        }
        stack.push(currentIndex);
    }

    @FunctionalInterface
    private interface ResultCalculator {
        int calculate(int currentIndex, int topIndex);
    }
}