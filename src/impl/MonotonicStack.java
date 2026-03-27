package patterns.java;

import java.util.Arrays;
import java.util.Stack;

public class MonotonicStack {
    private static final int NO_GREATER_ELEMENT = -1;

    /**
     * Finds the next greater element for each element in the input array.
     * Uses a monotonic increasing stack approach.
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
            updateMonotonicStack(nums, result, stack, i, (curr, top) -> curr > top, (curr, top) -> curr);
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
        int n = temperatures.length;
        int[] result = new int[n];
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            updateMonotonicStack(temperatures, result, stack, i, (curr, top) -> curr > top, (curr, top) -> curr - top);
        }
        return result;
    }

    /**
     * Helper method to update the monotonic stack and result array.
     *
     * @param values Array of input values
     * @param result Array to store the results
     * @param stack Monotonic stack of indices
     * @param currentIndex Current index being processed
     * @param compareFunc Function to compare current value with stack top
     * @param resultFunc Function to calculate result value
     */
    private void updateMonotonicStack(int[] values, int[] result, Stack<Integer> stack, int currentIndex,
                                      java.util.function.BiPredicate<Integer, Integer> compareFunc,
                                      java.util.function.BiFunction<Integer, Integer, Integer> resultFunc) {
        while (!stack.isEmpty() && compareFunc.test(values[currentIndex], values[stack.peek()])) {
            int topIndex = stack.pop();
            result[topIndex] = resultFunc.apply(currentIndex, topIndex);
        }
        stack.push(currentIndex);
    }
}