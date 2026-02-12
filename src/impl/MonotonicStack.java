package patterns.java;

import java.util.Arrays;
import java.util.Stack;

public class MonotonicStack {

    /**
     * Finds the next greater element for each element in the input array.
     *
     * @param nums The input array of integers
     * @return An array where each element is the next greater element in the original array,
     *         or -1 if no greater element exists
     */
    public int[] nextGreaterElement(int[] nums) {
        int n = nums.length;
        int[] result = initializeResult(n);
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            processNextGreaterElement(nums, result, stack, i);
        }
        return result;
    }

    private int[] initializeResult(int n) {
        int[] result = new int[n];
        Arrays.fill(result, -1);
        return result;
    }

    private void processNextGreaterElement(int[] nums, int[] result, Stack<Integer> stack, int currentIndex) {
        while (!stack.isEmpty() && nums[currentIndex] > nums[stack.peek()]) {
            int index = stack.pop();
            result[index] = nums[currentIndex];
        }
        stack.push(currentIndex);
    }

    /**
     * Calculates the number of days until a warmer temperature for each day.
     *
     * @param temperatures The input array of daily temperatures
     * @return An array where each element represents the number of days until a warmer temperature,
     *         or 0 if no warmer temperature is found
     */
    public int[] dailyTemperatures(int[] temperatures) {
        int n = temperatures.length;
        int[] result = new int[n];
        Stack<Integer> stack = new Stack<>();

        for (int currentDay = 0; currentDay < n; currentDay++) {
            processTemperatures(temperatures, result, stack, currentDay);
        }

        return result;
    }

    private void processTemperatures(int[] temperatures, int[] result, Stack<Integer> stack, int currentDay) {
        while (!stack.isEmpty() && temperatures[currentDay] > temperatures[stack.peek()]) {
            int prevDay = stack.pop();
            result[prevDay] = currentDay - prevDay;
        }
        stack.push(currentDay);
    }
}