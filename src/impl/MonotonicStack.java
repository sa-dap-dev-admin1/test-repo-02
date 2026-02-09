package patterns.java;

import java.util.Arrays;
import java.util.Stack;

public class MonotonicStack {

    /**
     * Finds the next greater element for each element in the input array.
     * 
     * @param nums The input array of integers.
     * @return An array where each element is the next greater element for the corresponding element in nums.
     */
    public int[] nextGreaterElement(int[] nums) {
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
     * Calculates the number of days to wait for a warmer temperature.
     * 
     * @param temperatures The input array of daily temperatures.
     * @return An array where each element represents the number of days to wait for a warmer temperature.
     */
    public int[] dailyTemperatures(int[] temperatures) {
        int n = temperatures.length;
        int[] result = new int[n];
        Stack<Integer> stack = new Stack<>();

        for (int currentDay = 0; currentDay < n; currentDay++) {
            processStack(temperatures, result, stack, currentDay);
        }

        return result;
    }

    /**
     * Helper method to process the stack for both nextGreaterElement and dailyTemperatures.
     * 
     * @param values The input array (either nums or temperatures).
     * @param result The result array to be populated.
     * @param stack The stack used for processing.
     * @param currentIndex The current index being processed.
     */
    private void processStack(int[] values, int[] result, Stack<Integer> stack, int currentIndex) {
        while (!stack.isEmpty() && values[currentIndex] > values[stack.peek()]) {
            int prevIndex = stack.pop();
            result[prevIndex] = currentIndex - prevIndex;
        }
        stack.push(currentIndex);
    }
}