package patterns.java;

import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.Deque;

public class MonotonicStack {

    /**
     * Finds the next greater element for each element in the given array.
     *
     * @param nums The input array of integers.
     * @return An array where each element is the next greater element for the corresponding element in nums.
     */
    public int[] nextGreaterElement(int[] nums) {
        return processMonotonicStack(nums, (current, top) -> current > top);
    }

    /**
     * Calculates the number of days to wait for a warmer temperature.
     *
     * @param temperatures The input array of daily temperatures.
     * @return An array where each element is the number of days to wait for a warmer temperature.
     */
    public int[] dailyTemperatures(int[] temperatures) {
        return processMonotonicStack(temperatures, (current, top) -> current > top);
    }

    /**
     * Generic method to process arrays using a monotonic stack.
     *
     * @param array The input array.
     * @param comparator A function to compare two elements.
     * @return The processed result array.
     */
    private int[] processMonotonicStack(int[] array, Comparator comparator) {
        if (array == null || array.length == 0) {
            return new int[0];
        }

        int length = array.length;
        int[] result = new int[length];
        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < length; i++) {
            while (!stack.isEmpty() && comparator.compare(array[i], array[stack.peek()])) {
                int index = stack.pop();
                result[index] = i - index;
            }
            stack.push(i);
        }

        return result;
    }

    /**
     * Functional interface for comparing two integers.
     */
    @FunctionalInterface
    private interface Comparator {
        boolean compare(int current, int top);
    }
}