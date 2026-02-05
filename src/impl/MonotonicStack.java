package patterns.java;

import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.Deque;

public class MonotonicStack {

    /**
     * Finds the next greater element for each element in the input array.
     *
     * @param nums The input array of integers.
     * @return An array where each element is the next greater element for the corresponding input element.
     */
    public int[] nextGreaterElement(int[] nums) {
        int length = nums.length;
        int[] result = new int[length];
        Arrays.fill(result, -1); // Default to -1 if no greater element exists
        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < length; i++) {
            processStack(nums, result, stack, i);
        }
        return result;
    }

    /**
     * Calculates the number of days until a warmer temperature for each day.
     *
     * @param temperatures The input array of daily temperatures.
     * @return An array where each element is the number of days to wait for a warmer temperature.
     */
    public int[] dailyTemperatures(int[] temperatures) {
        int length = temperatures.length;
        int[] result = new int[length];
        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < length; i++) {
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