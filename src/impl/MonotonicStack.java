package patterns.java;

import java.util.Arrays;
import java.util.Stack;
//test 2345
public class MonotonicStack {

    /**
     * Finds the next greater element for each element in the given array.
     * @param nums The input array of integers.
     * @return An array where each element is the next greater element for the corresponding element in nums.
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
     * @param comparison A lambda function to define the comparison logic.
     * @return The result array based on the specific operation.
     */
    private int[] monotonicStackOperation(int[] array, ComparisonOperation comparison) {
        int n = array.length;
        int[] result = new int[n];
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            while (!stack.isEmpty() && comparison.compare(array[i], array[stack.peek()])) {
                int prevIndex = stack.pop();
                result[prevIndex] = i - prevIndex;
            }
            stack.push(i);
        }

        return result;
    }

    /**
     * Functional interface for comparison operations.
     */
    @FunctionalInterface
    private interface ComparisonOperation {
        boolean compare(int current, int top);
    }
}