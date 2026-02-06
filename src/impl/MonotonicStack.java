package patterns.java;

import java.util.Arrays;
import java.util.Stack;

public class MonotonicStack {

    /**
     * Finds the next greater element for each element in the input array.
     * @param nums The input array of integers.
     * @return An array where each element is the next greater element for the corresponding element in nums.
     */
    public int[] nextGreaterElement(int[] nums) {
        if (nums == null || nums.length == 0) {
            return new int[0];
        }

        int n = nums.length;
        int[] result = new int[n];
        Arrays.fill(result, -1);
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            processMonotonicStack(nums, result, stack, i);
        }

        return result;
    }

    /**
     * Calculates the number of days to wait for a warmer temperature.
     * @param temperatures The input array of daily temperatures.
     * @return An array where each element is the number of days to wait for a warmer temperature.
     */
    public int[] dailyTemperatures(int[] temperatures) {
        if (temperatures == null || temperatures.length == 0) {
            return new int[0];
        }

        int n = temperatures.length;
        int[] result = new int[n];
        Stack<Integer> stack = new Stack<>();

        for (int currentDay = 0; currentDay < n; currentDay++) {
            processMonotonicStack(temperatures, result, stack, currentDay);
        }

        return result;
    }

    private void processMonotonicStack(int[] values, int[] result, Stack<Integer> stack, int currentIndex) {
        while (!stack.isEmpty() && values[currentIndex] > values[stack.peek()]) {
            int prevIndex = stack.pop();
            result[prevIndex] = currentIndex - prevIndex;
        }
        stack.push(currentIndex);
    }
}