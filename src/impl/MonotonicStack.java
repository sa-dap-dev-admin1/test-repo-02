package patterns.java;

import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.Deque;
//test 23
public class MonotonicStack {

    /**
     * Finds the next greater element for each element in the input array.
     * Uses a monotonic decreasing stack to achieve O(n) time complexity.
     *
     * @param nums The input array of integers
     * @return An array where each element is the next greater element for the corresponding input element
     */
    public int[] nextGreaterElement(int[] nums) {
        int n = nums.length;
        int[] result = new int[n];
        Arrays.fill(result, -1); // Default to -1 if no greater element exists
        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < n; i++) {
            updateMonotonicStack(nums, result, stack, i);
        }
        return result;
    }

    /**
     * Calculates the number of days to wait for a warmer temperature.
     * Uses a monotonic decreasing stack to achieve O(n) time complexity.
     *
     * @param temperatures The input array of daily temperatures
     * @return An array where each element is the number of days to wait for a warmer temperature
     */
    public int[] dailyTemperatures(int[] temperatures) {
        int n = temperatures.length;
        int[] result = new int[n];
        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < n; i++) {
            updateMonotonicStack(temperatures, result, stack, i);
        }

        return result;
    }

    /**
     * Helper method to update the monotonic stack and result array.
     * This method encapsulates the common logic used in both nextGreaterElement and dailyTemperatures.
     *
     * @param values The input array (either nums or temperatures)
     * @param result The result array to be updated
     * @param stack The monotonic stack
     * @param currentIndex The current index being processed
     */
    private void updateMonotonicStack(int[] values, int[] result, Deque<Integer> stack, int currentIndex) {
        while (!stack.isEmpty() && values[currentIndex] > values[stack.peek()]) {
            int prevIndex = stack.pop();
            result[prevIndex] = dailyTemperatures(values) ? currentIndex - prevIndex : values[currentIndex];
        }
        stack.push(currentIndex);
    }

    /**
     * Helper method to determine if we're processing daily temperatures.
     * This is used to decide how to update the result array in updateMonotonicStack.
     *
     * @param values The input array being processed
     * @return true if processing daily temperatures, false otherwise
     */
    private boolean dailyTemperatures(int[] values) {
        return values.length > 0 && values[0] >= 30 && values[0] <= 100;
    }
}