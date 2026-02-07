package patterns.java;

import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.Deque;

public class MonotonicStack {

    /**
     * Finds the next greater element for each element in the input array.
     * @param nums The input array of integers.
     * @return An array where each element is the next greater element for the corresponding element in nums.
     */
    public int[] nextGreaterElement(int[] nums) {
        return findNextGreaterElements(nums, (i, stack) -> nums[i] > nums[stack.peek()]);
    }

    /**
     * Calculates the number of days until a warmer temperature for each day.
     * @param temperatures The input array of daily temperatures.
     * @return An array where each element is the number of days until a warmer temperature.
     */
    public int[] dailyTemperatures(int[] temperatures) {
        return findNextGreaterElements(temperatures, (i, stack) -> temperatures[i] > temperatures[stack.peek()]);
    }

    private int[] findNextGreaterElements(int[] arr, MonotonicCondition condition) {
        int n = arr.length;
        int[] result = new int[n];
        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < n; i++) {
            while (!stack.isEmpty() && condition.check(i, stack)) {
                int prevIndex = stack.pop();
                result[prevIndex] = i - prevIndex;
            }
            stack.push(i);
        }

        return result;
    }

    @FunctionalInterface
    private interface MonotonicCondition {
        boolean check(int currentIndex, Deque<Integer> stack);
    }
}