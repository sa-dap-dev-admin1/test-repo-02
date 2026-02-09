package patterns.java;

import java.util.Arrays;
import java.util.Stack;
//test 2345f
public class MonotonicStack {

    /**
     * Finds the next greater element for each element in the input array.
     * @param nums The input array of integers.
     * @return An array where each element is the next greater element for the corresponding element in the input array.
     */
    public int[] nextGreaterElement(int[] nums) {
        if (nums == null || nums.length == 0) {
            return new int[0];
        }
        return processMonotonicStack(nums, (i, j) -> nums[i]);
    }

    /**
     * Calculates the number of days to wait for a warmer temperature.
     * @param temperatures The input array of daily temperatures.
     * @return An array where each element represents the number of days to wait for a warmer temperature.
     */
    public int[] dailyTemperatures(int[] temperatures) {
        if (temperatures == null || temperatures.length == 0) {
            return new int[0];
        }
        return processMonotonicStack(temperatures, (i, j) -> j - i);
    }

    /**
     * Helper method to process the monotonic stack for both nextGreaterElement and dailyTemperatures.
     * @param arr The input array.
     * @param resultCalculator A function to calculate the result based on indices.
     * @return The processed result array.
     */
    private int[] processMonotonicStack(int[] arr, ResultCalculator resultCalculator) {
        int n = arr.length;
        int[] result = new int[n];
        Arrays.fill(result, -1); // Default to -1 if no greater element exists
        Stack<Integer> stack = new Stack<>(); // Stack stores indices

        // Iterate through the array
        for (int i = 0; i < n; i++) {
            processStackTop(arr, result, stack, i, resultCalculator);
            stack.push(i); // Push the current index onto the stack
        }
        return result;
    }

    /**
     * Processes the top of the stack for the monotonic stack algorithm.
     */
    private void processStackTop(int[] arr, int[] result, Stack<Integer> stack, int currentIndex, ResultCalculator resultCalculator) {
        while (!stack.isEmpty() && arr[currentIndex] > arr[stack.peek()]) {
            int index = stack.pop();
            result[index] = resultCalculator.calculate(index, currentIndex);
        }
    }

    /**
     * Functional interface to calculate the result based on indices.
     */
    @FunctionalInterface
    private interface ResultCalculator {
        int calculate(int fromIndex, int toIndex);
    }
}