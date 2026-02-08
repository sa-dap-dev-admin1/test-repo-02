package patterns.java;

import java.util.Arrays;
import java.util.Stack;
//test 23
public class MonotonicStack {

    /**
     * Finds the next greater element for each element in the given array.
     * Uses a monotonic stack technique to achieve O(n) time complexity.
     *
     * @param nums The input array of integers
     * @return An array where each element is the next greater element in the original array,
     *         or -1 if no greater element exists
     */
    public int[] nextGreaterElement(int[] nums) {
        int n = nums.length;
        int[] result = new int[n];
        Arrays.fill(result, -1);
        return processMonotonicStack(nums, result, (i, top) -> nums[i] > nums[top]);
    }

    /**
     * Calculates the number of days to wait for a warmer temperature.
     * Uses a monotonic stack technique to achieve O(n) time complexity.
     *
     * @param temperatures The input array of daily temperatures
     * @return An array where each element is the number of days to wait for a warmer temperature,
     *         or 0 if no warmer temperature is found
     */
    public int[] dailyTemperatures(int[] temperatures) {
        int n = temperatures.length;
        int[] result = new int[n];
        return processMonotonicStack(temperatures, result, (i, top) -> temperatures[i] > temperatures[top]);
    }

    /**
     * Helper method to process arrays using the monotonic stack technique.
     *
     * @param input The input array
     * @param result The result array to be filled
     * @param comparator A lambda expression defining the comparison logic
     * @return The processed result array
     */
    private int[] processMonotonicStack(int[] input, int[] result, StackComparator comparator) {
        Stack<Integer> stack = new Stack<>();
        for (int i = 0; i < input.length; i++) {
            while (!stack.isEmpty() && comparator.compare(i, stack.peek())) {
                int prevIndex = stack.pop();
                result[prevIndex] = i - prevIndex;
            }
            stack.push(i);
        }
        return result;
    }

    /**
     * Functional interface for stack element comparison.
     */
    @FunctionalInterface
    private interface StackComparator {
        boolean compare(int current, int top);
    }
}