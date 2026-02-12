package patterns.java;

import java.util.Arrays;
import java.util.Stack;

/**
 * Implements monotonic stack operations for various array problems.
 */
public class MonotonicStack {

    /**
     * Finds the next greater element for each element in the input array.
     *
     * @param nums the input array
     * @return an array where each element is the next greater element in the original array,
     *         or -1 if no such element exists
     * @throws IllegalArgumentException if the input array is null or empty
     */
    public int[] nextGreaterElement(int[] nums) {
        if (nums == null || nums.length == 0) {
            throw new IllegalArgumentException("Input array must not be null or empty");
        }

        int n = nums.length;
        int[] result = new int[n];
        Arrays.fill(result, -1);
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            processStack(stack, nums, result, i, (current, top) -> current > top);
        }

        return result;
    }

    /**
     * Calculates the number of days to wait for a warmer temperature.
     *
     * @param temperatures the input array of daily temperatures
     * @return an array where each element is the number of days to wait for a warmer temperature,
     *         or 0 if no such day exists
     * @throws IllegalArgumentException if the input array is null or empty
     */
    public int[] dailyTemperatures(int[] temperatures) {
        if (temperatures == null || temperatures.length == 0) {
            throw new IllegalArgumentException("Input array must not be null or empty");
        }

        int n = temperatures.length;
        int[] result = new int[n];
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            processStack(stack, temperatures, result, i, (current, top) -> current > top);
        }

        return result;
    }

    private void processStack(Stack<Integer> stack, int[] array, int[] result, int currentIndex,
                              StackCondition condition) {
        while (!stack.isEmpty() && condition.test(array[currentIndex], array[stack.peek()])) {
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