package patterns.java;

import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.BiPredicate;

public class MonotonicStack {

    /**
     * Finds the next greater element for each element in the input array.
     * @param nums The input array of integers.
     * @return An array where each element is the next greater element for the corresponding element in the input array.
     */
    public int[] nextGreaterElement(int[] nums) {
        return monotonicStackOperation(nums, (current, top) -> current > top);
    }

    /**
     * Calculates the number of days to wait for a warmer temperature.
     * @param temperatures The input array of daily temperatures.
     * @return An array where each element is the number of days to wait for a warmer temperature.
     */
    public int[] dailyTemperatures(int[] temperatures) {
        return monotonicStackOperation(temperatures, (current, top) -> current > top);
    }

    /**
     * Generic method to perform monotonic stack operations.
     * @param array The input array.
     * @param comparison The comparison function to determine stack operations.
     * @return The result array after applying the monotonic stack algorithm.
     */
    private int[] monotonicStackOperation(int[] array, BiPredicate<Integer, Integer> comparison) {
        int n = array.length;
        int[] result = new int[n];
        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < n; i++) {
            while (!stack.isEmpty() && comparison.test(array[i], array[stack.peek()])) {
                int prevIndex = stack.pop();
                result[prevIndex] = i - prevIndex;
            }
            stack.push(i);
        }

        return result;
    }
}