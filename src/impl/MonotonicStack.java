package patterns.java;

import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.IntPredicate;

public class MonotonicStack {

    /**
     * Finds the next greater element for each element in the input array.
     *
     * @param nums The input array of integers.
     * @return An array where each element is the next greater element for the corresponding input element.
     */
    public int[] nextGreaterElement(int[] nums) {
        if (nums == null || nums.length == 0) {
            return new int[0];
        }

        int n = nums.length;
        int[] result = new int[n];
        Arrays.fill(result, -1);
        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < n; i++) {
            processMonotonicStack(nums, result, stack, i, current -> nums[i] > nums[current]);
        }

        return result;
    }

    /**
     * Calculates the number of days to wait for a warmer temperature.
     *
     * @param temperatures The input array of daily temperatures.
     * @return An array where each element represents the number of days to wait for a warmer temperature.
     */
    public int[] dailyTemperatures(int[] temperatures) {
        if (temperatures == null || temperatures.length == 0) {
            return new int[0];
        }

        int n = temperatures.length;
        int[] result = new int[n];
        Deque<Integer> stack = new ArrayDeque<>();

        for (int currentDay = 0; currentDay < n; currentDay++) {
            processMonotonicStack(temperatures, result, stack, currentDay,
                    prevDay -> temperatures[currentDay] > temperatures[prevDay]);
        }

        return result;
    }

    /**
     * Helper method to process the monotonic stack for both nextGreaterElement and dailyTemperatures.
     *
     * @param inputArray The input array (nums or temperatures).
     * @param result The result array to be populated.
     * @param stack The monotonic stack.
     * @param currentIndex The current index being processed.
     * @param comparisonPredicate The predicate for comparing elements.
     */
    private void processMonotonicStack(int[] inputArray, int[] result, Deque<Integer> stack,
                                       int currentIndex, IntPredicate comparisonPredicate) {
        while (!stack.isEmpty() && comparisonPredicate.test(stack.peek())) {
            int prevIndex = stack.pop();
            result[prevIndex] = currentIndex - prevIndex;
        }
        stack.push(currentIndex);
    }
}