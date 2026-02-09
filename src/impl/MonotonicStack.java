package patterns.java;

import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.IntBinaryOperator;

//test 2345
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
     * Calculates the number of days until a warmer temperature for each day.
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
     * @param comparison A binary operator to compare elements.
     * @return The result array after performing the monotonic stack operation.
     */
    private int[] monotonicStackOperation(int[] arr, IntBinaryOperator comparison) {
        final int n = arr.length;
        final int[] result = new int[n];
        final Deque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < n; i++) {
            while (!stack.isEmpty() && comparison.applyAsInt(arr[i], arr[stack.peek()])) {
                int index = stack.pop();
                result[index] = i - index;
            }
            stack.push(i);
        }

        return result;
    }
}