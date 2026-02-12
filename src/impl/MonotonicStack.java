package patterns.java;

import java.util.Arrays;
import java.util.Stack;
import java.util.function.BiPredicate;

public class MonotonicStack {

    /**
     * Finds the next greater element for each element in the input array.
     * Uses a monotonic stack pattern for efficient calculation.
     *
     * @param nums Input array of integers
     * @return Array containing the next greater element for each input element
     */
    public int[] nextGreaterElement(int[] nums) {
        return calculateNextElement(nums, (current, top) -> current > top);
    }

    /**
     * Calculates the number of days to wait for a warmer temperature.
     * Uses a monotonic decreasing stack pattern for efficient calculation.
     *
     * @param temperatures Array of daily temperatures
     * @return Array containing the wait times for each day
     */
    public int[] dailyTemperatures(int[] temperatures) {
        return calculateNextElement(temperatures, (current, top) -> current > top);
    }

    /**
     * Generic method to calculate the next element satisfying a condition using monotonic stack pattern.
     *
     * @param array Input array
     * @param condition BiPredicate to define the condition for popping elements from the stack
     * @return Result array based on the input condition
     */
    private int[] calculateNextElement(int[] array, BiPredicate<Integer, Integer> condition) {
        int n = array.length;
        int[] result = new int[n];
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            while (isStackTopSatisfyingCondition(stack, array, i, condition)) {
                int prevIndex = stack.pop();
                result[prevIndex] = i - prevIndex;
            }
            stack.push(i);
        }

        return result;
    }

    /**
     * Checks if the top element of the stack satisfies the given condition.
     *
     * @param stack The stack of indices
     * @param array The input array
     * @param currentIndex The current index being processed
     * @param condition The condition to check
     * @return true if the condition is satisfied, false otherwise
     */
    private boolean isStackTopSatisfyingCondition(Stack<Integer> stack, int[] array, int currentIndex, BiPredicate<Integer, Integer> condition) {
        return !stack.isEmpty() && condition.test(array[currentIndex], array[stack.peek()]);
    }
}