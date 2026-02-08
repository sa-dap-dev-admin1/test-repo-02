package patterns.java;

import java.util.Arrays;
import java.util.Deque;
import java.util.ArrayDeque;

/**
 * This class provides implementations of monotonic stack-based algorithms.
 */
public class MonotonicStack {

    /**
     * Finds the next greater element for each element in the input array.
     *
     * @param nums The input array of integers.
     * @return An array where each element is the next greater element for the corresponding input element.
     */
    public int[] nextGreaterElement(int[] nums) {
        int length = nums.length;
        int[] result = new int[length];
        Arrays.fill(result, -1);
        Deque<Integer> stack = new ArrayDeque<>();

        for (int currentIndex = 0; currentIndex < length; currentIndex++) {
            processMonotonicStack(nums, result, stack, currentIndex);
        }
        return result;
    }

    /**
     * Calculates the number of days until a warmer temperature for each day.
     *
     * @param temperatures The input array of daily temperatures.
     * @return An array where each element is the number of days to wait for a warmer temperature.
     */
    public int[] dailyTemperatures(int[] temperatures) {
        int length = temperatures.length;
        int[] result = new int[length];
        Deque<Integer> stack = new ArrayDeque<>();

        for (int currentIndex = 0; currentIndex < length; currentIndex++) {
            processMonotonicStack(temperatures, result, stack, currentIndex);
        }

        return result;
    }

    /**
     * Processes the monotonic stack for both nextGreaterElement and dailyTemperatures methods.
     *
     * @param inputArray The input array (either nums or temperatures).
     * @param result The result array to be populated.
     * @param stack The monotonic stack used for processing.
     * @param currentIndex The current index being processed.
     */
    private void processMonotonicStack(int[] inputArray, int[] result, Deque<Integer> stack, int currentIndex) {
        while (!stack.isEmpty() && inputArray[currentIndex] > inputArray[stack.peek()]) {
            int prevIndex = stack.pop();
            result[prevIndex] = isNextGreaterElement(result) ? inputArray[currentIndex] : currentIndex - prevIndex;
        }
        stack.push(currentIndex);
    }

    /**
     * Determines if the result array is for nextGreaterElement or dailyTemperatures.
     *
     * @param result The result array being populated.
     * @return true if it's for nextGreaterElement, false for dailyTemperatures.
     */
    private boolean isNextGreaterElement(int[] result) {
        return result[0] == -1;
    }
}
//test 23