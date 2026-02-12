package patterns.java;

import java.util.Arrays;
import java.util.Stack;
//test 2345fhdfff
public class MonotonicStack {

    private static final int NO_GREATER_ELEMENT = -1;
    private static final int DEFAULT_WAIT_TIME = 0;

    /**
     * Finds the next greater element for each element in the input array.
     *
     * @param nums The input array of integers
     * @return An array where each element is the next greater element for the corresponding element in nums
     * @throws IllegalArgumentException if nums is null
     */
    public int[] nextGreaterElement(int[] nums) {
        if (nums == null) {
            throw new IllegalArgumentException("Input array cannot be null");
        }

        int n = nums.length;
        int[] result = new int[n];
        Arrays.fill(result, NO_GREATER_ELEMENT);
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            processStack(nums, result, stack, i, (curr, top) -> curr > top);
        }
        return result;
    }

    /**
     * Calculates the number of days to wait for a warmer temperature.
     *
     * @param temperatures The input array of daily temperatures
     * @return An array where each element is the number of days to wait for a warmer temperature
     * @throws IllegalArgumentException if temperatures is null
     */
    public int[] dailyTemperatures(int[] temperatures) {
        if (temperatures == null) {
            throw new IllegalArgumentException("Input array cannot be null");
        }

        int n = temperatures.length;
        int[] result = new int[n];
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            processStack(temperatures, result, stack, i, (curr, top) -> curr > top);
        }

        return result;
    }

    private void processStack(int[] arr, int[] result, Stack<Integer> stack, int currentIndex, 
                              StackCondition condition) {
        while (!stack.isEmpty() && condition.test(arr[currentIndex], arr[stack.peek()])) {
            int prevIndex = stack.pop();
            result[prevIndex] = currentIndex - prevIndex;
        }
        stack.push(currentIndex);
    }

    @FunctionalInterface
    private interface StackCondition {
        boolean test(int current, int top);
    }
}