package patterns.java;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

public class MonotonicStack {

    /**
     * Finds the next greater element for each element in the given array.
     * 
     * @param nums The input array of integers.
     * @return An array where each element is the next greater element for the corresponding element in nums.
     */
    public int[] nextGreaterElement(int[] nums) {
        return monotonicStackOperation(nums, (current, top) -> current > top);
    }

    /**
     * Calculates the number of days until a warmer temperature for each day.
     * 
     * @param temperatures The input array of daily temperatures.
     * @return An array where each element is the number of days until a warmer temperature.
     */
    public int[] dailyTemperatures(int[] temperatures) {
        int[] result = monotonicStackOperation(temperatures, (current, top) -> current > top);
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
     * Generic monotonic stack operation.
     * 
     * @param array The input array.
     * @param comparison A lambda function defining the comparison logic.
     * @return An array of results based on the monotonic stack operation.
     */
    private int[] monotonicStackOperation(int[] array, ComparisonFunction comparison) {
        if (array == null || array.length == 0) {
            return new int[0];
        }

        int arrayLength = array.length;
        int[] result = new int[arrayLength];
        Arrays.fill(result, -1);
        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < arrayLength; i++) {
            while (!stack.isEmpty() && comparison.compare(array[i], array[stack.peek()])) {
                int index = stack.pop();
                result[index] = i;
            }
            stack.push(i);
        }

        return result;
    }

    /**
     * Functional interface for comparison logic.
     */
    @FunctionalInterface
    private interface ComparisonFunction {
        boolean compare(int current, int top);
    }
}