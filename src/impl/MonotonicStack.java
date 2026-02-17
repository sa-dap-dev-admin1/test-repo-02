package patterns.java;

import java.util.Arrays;
import java.util.Stack;

public class MonotonicStack {
    private static final int NO_GREATER_ELEMENT = -1;

    /**
     * Finds the next greater element for each element in the input array.
     *
     * @param nums The input array of integers
     * @return An array where each element is the next greater element in the original array,
     *         or -1 if no greater element exists
     */
    public int[] nextGreaterElement(int[] nums) {
        int n = nums.length;
        int[] result = new int[n];
        Arrays.fill(result, NO_GREATER_ELEMENT);
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            updateStack(nums, result, stack, i);
        }
        return result;
    }

    /**
     * Calculates the number of days to wait for a warmer temperature.
     *
     * @param temperatures The input array of daily temperatures
     * @return An array where each element represents the number of days to wait
     *         for a warmer temperature, or 0 if no warmer temperature is found
     */
    public int[] dailyTemperatures(int[] temperatures) {
        int n = temperatures.length;
        int[] result = new int[n];
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            updateStack(temperatures, result, stack, i);
        }

        return result;
    }

    private void updateStack(int[] values, int[] result, Stack<Integer> stack, int currentIndex) {
        while (!stack.isEmpty() && values[currentIndex] > values[stack.peek()]) {
            int topIndex = stack.pop();
            result[topIndex] = currentIndex - topIndex;
        }
        stack.push(currentIndex);
    }
}