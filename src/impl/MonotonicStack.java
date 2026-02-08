package patterns.java;

import java.util.Arrays;
import java.util.Stack;
import java.util.function.BiPredicate;

public class MonotonicStack {

    /**
     * Finds the next greater element for each element in the input array.
     *
     * @param nums The input array of integers.
     * @return An array where each element is the next greater element for the corresponding element in the input array.
     */
    public int[] nextGreaterElement(int[] nums) {
        return monotonicStackOperation(nums, (current, top) -> current > top);
    }

    /**
     * Calculates the number of days to wait for a warmer temperature.
     *
     * @param temperatures The input array of daily temperatures.
     * @return An array where each element represents the number of days to wait for a warmer temperature.
     */
    public int[] dailyTemperatures(int[] temperatures) {
        return monotonicStackOperation(temperatures, (current, top) -> current > top);
    }

    /**
     * Generic method to perform monotonic stack operations.
     *
     * @param arr The input array.
     * @param comparator The comparison logic for stack operations.
     * @return The result array after performing monotonic stack operations.
     */
    private int[] monotonicStackOperation(int[] arr, BiPredicate<Integer, Integer> comparator) {
        int n = arr.length;
        int[] result = new int[n];
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            while (!stack.isEmpty() && comparator.test(arr[i], arr[stack.peek()])) {
                int index = stack.pop();
                result[index] = i - index;
            }
            stack.push(i);
        }

        return result;
    }
}