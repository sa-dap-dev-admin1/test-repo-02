package patterns.java;

import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.Deque;

public class MonotonicStack {

    /**
     * Finds the next greater element for each element in the input array.
     *
     * @param nums The input array of integers.
     * @return An array where each element is the next greater element for the corresponding element in the input array.
     */
    public int[] nextGreaterElement(int[] nums) {
        if (nums == null || nums.length == 0) {
            return new int[0];
        }

        int n = nums.length;
        int[] result = new int[n];
        Arrays.fill(result, -1);
        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < n; i++) {
            processMonotonicStack(nums, result, stack, i);
        }
        return result;
    }

    /**
     * Calculates the number of days to wait for a warmer temperature.
     *
     * @param temperatures The input array of daily temperatures.
     * @return An array where each element represents the number of days to wait for a warmer temperature.
     */
    public int[] dailyTemperatures(int[] temperatures) {
        if (temperatures == null || temperatures.length == 0) {
            return new int[0];
        }

        int n = temperatures.length;
        int[] result = new int[n];
        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < n; i++) {
            processMonotonicStack(temperatures, result, stack, i);
        }

        return result;
    }

    private void processMonotonicStack(int[] array, int[] result, Deque<Integer> stack, int currentIndex) {
        while (!stack.isEmpty() && array[currentIndex] > array[stack.peek()]) {
            int prevIndex = stack.pop();
            result[prevIndex] = currentIndex - prevIndex;
        }
        stack.push(currentIndex);
    }
}