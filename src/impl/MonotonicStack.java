package patterns.java;

import java.util.Arrays;
import java.util.Stack;

public class MonotonicStack {

    /**
     * Finds the next greater element for each element in the given array.
     *
     * @param nums The input array of integers
     * @return An array where each element is the next greater element for the corresponding input element
     * @throws IllegalArgumentException if the input array is null
     */
    public int[] nextGreaterElement(int[] nums) {
        if (nums == null) {
            throw new IllegalArgumentException("Input array cannot be null");
        }

        int n = nums.length;
        int[] result = new int[n];
        Arrays.fill(result, -1);
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            processStack(nums, result, stack, i);
        }

        return result;
    }

    /**
     * Calculates the number of days until a warmer temperature for each day.
     *
     * @param temperatures The input array of daily temperatures
     * @return An array where each element represents the number of days until a warmer temperature
     * @throws IllegalArgumentException if the input array is null
     */
    public int[] dailyTemperatures(int[] temperatures) {
        if (temperatures == null) {
            throw new IllegalArgumentException("Input array cannot be null");
        }

        int n = temperatures.length;
        int[] result = new int[n];
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            processStackForTemperatures(temperatures, result, stack, i);
        }

        return result;
    }

    private void processStack(int[] nums, int[] result, Stack<Integer> stack, int currentIndex) {
        while (!stack.isEmpty() && nums[currentIndex] > nums[stack.peek()]) {
            int index = stack.pop();
            result[index] = nums[currentIndex];
        }
        stack.push(currentIndex);
    }

    private void processStackForTemperatures(int[] temperatures, int[] result, Stack<Integer> stack, int currentDay) {
        while (!stack.isEmpty() && temperatures[currentDay] > temperatures[stack.peek()]) {
            int prevDay = stack.pop();
            result[prevDay] = currentDay - prevDay;
        }
        stack.push(currentDay);
    }
}