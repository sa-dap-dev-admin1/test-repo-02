package patterns.java;

import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.function.BiPredicate;

//test 2345
public class MonotonicStack {

    /**
     * Finds the next greater element for each element in the input array.
     *
     * @param nums The input array of integers.
     * @return An array where each element is the next greater element for the corresponding element in nums.
     */
    public int[] nextGreaterElement(int[] nums) {
        if (nums == null || nums.length == 0) {
            return new int[0];
        }
        return monotonicStackOperation(nums, (current, top) -> current > top);
    }

    /**
     * Calculates the number of days to wait for a warmer temperature.
     *
     * @param temperatures The input array of daily temperatures.
     * @return An array where each element is the number of days to wait for a warmer temperature.
     */
    public int[] dailyTemperatures(int[] temperatures) {
        if (temperatures == null || temperatures.length == 0) {
            return new int[0];
        }
        return monotonicStackOperation(temperatures, (current, top) -> current > top);
    }

    /**
     * Generic monotonic stack operation.
     *
     * @param arr The input array.
     * @param comparison The comparison function to determine the monotonic property.
     * @return The result array based on the monotonic stack operation.
     */
    private int[] monotonicStackOperation(int[] arr, BiPredicate<Integer, Integer> comparison) {
        int n = arr.length;
        int[] result = new int[n];
        ArrayDeque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < n; i++) {
            while (!stack.isEmpty() && comparison.test(arr[i], arr[stack.peek()])) {
                int index = stack.pop();
                result[index] = i - index;
            }
            stack.push(i);
        }

        return result;
    }
}