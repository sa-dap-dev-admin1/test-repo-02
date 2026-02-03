package patterns.java;

import java.util.Arrays;
import java.util.Deque;
import java.util.ArrayDeque;

public class MonotonicStack {

    /**
     * Finds the next greater element for each element in the given array.
     *
     * @param nums The input array of integers.
     * @return An array where each element is the next greater element for the corresponding element in the input array.
     */
    public int[] nextGreaterElement(int[] nums) {
        int n = nums.length;
        int[] result = new int[n];
        Arrays.fill(result, -1);
        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < n; i++) {
            processStack(nums, result, stack, i);
        }
        return result;
    }

    /**
     * Calculates the number of days until a warmer temperature for each day.
     *
     * @param temperatures The input array of daily temperatures.
     * @return An array where each element is the number of days until a warmer temperature.
     */
    public int[] dailyTemperatures(int[] temperatures) {
        int n = temperatures.length;
        int[] result = new int[n];
        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < n; i++) {
            processStack(temperatures, result, stack, i);
        }

        return result;
    }

    private void processStack(int[] array, int[] result, Deque<Integer> stack, int currentIndex) {
        while (!stack.isEmpty() && array[currentIndex] > array[stack.peek()]) {
            int prevIndex = stack.pop();
            result[prevIndex] = currentIndex - prevIndex;
        }
        stack.push(currentIndex);
    }
}