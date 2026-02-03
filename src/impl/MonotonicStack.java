package patterns.java;

import java.util.Arrays;
import java.util.Stack;

public class MonotonicStack {

    /**
     * Finds the next greater element for each element in the given array.
     *
     * @param nums The input array of integers
     * @return An array where each element is the next greater element for the corresponding input element
     */
    public int[] nextGreaterElement(int[] nums) {
        int n = nums.length;
        int[] result = initializeResultArray(n);
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            processStackForNextGreater(nums, result, stack, i);
            stack.push(i);
        }
        return result;
    }

    private int[] initializeResultArray(int n) {
        int[] result = new int[n];
        Arrays.fill(result, -1);
        return result;
    }

    private void processStackForNextGreater(int[] nums, int[] result, Stack<Integer> stack, int currentIndex) {
        while (!stack.isEmpty() && nums[currentIndex] > nums[stack.peek()]) {
            int index = stack.pop();
            result[index] = nums[currentIndex];
        }
    }

    /**
     * Calculates the number of days to wait for a warmer temperature for each day.
     *
     * @param temperatures The input array of daily temperatures
     * @return An array where each element is the number of days to wait for a warmer temperature
     */
    public int[] dailyTemperatures(int[] temperatures) {
        int n = temperatures.length;
        int[] result = new int[n];
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            processStackForDailyTemperatures(temperatures, result, stack, i);
            stack.push(i);
        }

        return result;
    }

    private void processStackForDailyTemperatures(int[] temperatures, int[] result, Stack<Integer> stack, int currentIndex) {
        while (!stack.isEmpty() && temperatures[currentIndex] > temperatures[stack.peek()]) {
            int prevIndex = stack.pop();
            result[prevIndex] = currentIndex - prevIndex;
        }
    }
}