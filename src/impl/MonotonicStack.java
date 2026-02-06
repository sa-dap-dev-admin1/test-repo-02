package patterns.java;

import java.util.Arrays;
import java.util.Stack;
import java.util.function.BiPredicate;

public class MonotonicStack {

    /**
     * Finds the next greater element for each element in the input array.
     * Uses a monotonic stack technique to efficiently solve the problem.
     *
     * @param nums The input array of integers
     * @return An array where each element is the next greater element for the corresponding input element
     */
    public int[] nextGreaterElement(int[] nums) {
        return processWithMonotonicStack(nums, (current, top) -> current > top);
    }

    /**
     * Calculates the number of days to wait for a warmer temperature.
     * Uses a monotonic stack technique to efficiently solve the problem.
     *
     * @param temperatures The input array of daily temperatures
     * @return An array where each element is the number of days to wait for a warmer temperature
     */
    public int[] dailyTemperatures(int[] temperatures) {
        int[] result = processWithMonotonicStack(temperatures, (current, top) -> current > top);
        for (int i = 0; i < result.length; i++) {
            if (result[i] != -1) {
                result[i] = result[i] - i;
            } else {
                result[i] = 0;
            }
        }
        return result;
    }

    /**
     * Helper method to process an array using a monotonic stack.
     *
     * @param arr The input array
     * @param comparator A BiPredicate to define the comparison logic
     * @return An array of processed results
     */
    private int[] processWithMonotonicStack(int[] arr, BiPredicate<Integer, Integer> comparator) {
        int n = arr.length;
        int[] result = new int[n];
        Arrays.fill(result, -1);
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            while (!stack.isEmpty() && comparator.test(arr[i], arr[stack.peek()])) {
                int index = stack.pop();
                result[index] = i;
            }
            stack.push(i);
        }

        return result;
    }
}