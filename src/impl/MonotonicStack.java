package patterns.java;

import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.Deque;

public class MonotonicStack {

    /**
     * Finds the next greater element for each element in the input array.
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
     * @return An array where each element represents the number of days to wait for a warmer temperature.
     */
    public int[] dailyTemperatures(int[] temperatures) {
        return processMonotonicStack(temperatures, (current, top) -> current > top);
    }

    private int[] processMonotonicStack(int[] arr, ComparisonStrategy strategy) {
        int n = arr.length;
        int[] result = new int[n];
        Arrays.fill(result, -1);
        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < n; i++) {
            while (!stack.isEmpty() && strategy.compare(arr[i], arr[stack.peek()])) {
                int index = stack.pop();
                result[index] = i - index;
            }
            stack.push(i);
        }

        return result;
    }

    @FunctionalInterface
    private interface ComparisonStrategy {
        boolean compare(int current, int top);
    }
}