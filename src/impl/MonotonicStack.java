package patterns.java;

import java.util.Arrays;
import java.util.Stack;

public class MonotonicStack {

    /**
     * Finds the next greater element for each element in the input array.
     * Uses a monotonic stack pattern to achieve O(n) time complexity.
     *
     * @param nums The input array of integers
     * @return An array where each element is the next greater element for the corresponding input element
     */
    public int[] nextGreaterElement(int[] nums) {
        if (nums == null || nums.length == 0) {
            return new int[0];
        }

        int arrayLength = nums.length;
        int[] result = initializeResultArray(arrayLength, -1);
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < arrayLength; i++) {
            processStackForNextGreater(nums, result, stack, i);
        }

        return result;
    }

    /**
     * Calculates the number of days to wait for a warmer temperature.
     * Uses a monotonic stack pattern to achieve O(n) time complexity.
     *
     * @param temperatures The input array of daily temperatures
     * @return An array where each element is the number of days to wait for a warmer temperature
     */
    public int[] dailyTemperatures(int[] temperatures) {
        if (temperatures == null || temperatures.length == 0) {
            return new int[0];
        }

        int arrayLength = temperatures.length;
        int[] result = new int[arrayLength];
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < arrayLength; i++) {
            processStackForDailyTemperatures(temperatures, result, stack, i);
        }

        return result;
    }

    private int[] initializeResultArray(int length, int defaultValue) {
        int[] result = new int[length];
        Arrays.fill(result, defaultValue);
        return result;
    }

    private void processStackForNextGreater(int[] nums, int[] result, Stack<Integer> stack, int currentIndex) {
        while (!stack.isEmpty() && nums[currentIndex] > nums[stack.peek()]) {
            int index = stack.pop();
            result[index] = nums[currentIndex];
        }
        stack.push(currentIndex);
    }

    private void processStackForDailyTemperatures(int[] temperatures, int[] result, Stack<Integer> stack, int currentIndex) {
        while (!stack.isEmpty() && temperatures[currentIndex] > temperatures[stack.peek()]) {
            int prevIndex = stack.pop();
            result[prevIndex] = currentIndex - prevIndex;
        }
        stack.push(currentIndex);
    }
}