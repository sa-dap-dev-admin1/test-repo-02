package patterns.java;

import java.util.Arrays;
import java.util.Deque;
import java.util.ArrayDeque;

public class MonotonicStack {

    /**
     * Finds the next greater element for each element in the input array.
     * Uses a monotonic stack approach.
     *
     * @param nums The input array of integers
     * @return An array where each element is the next greater element for the corresponding input element
     */
    public int[] nextGreaterElement(int[] nums) {
        int n = nums.length;
        int[] result = new int[n];
        Arrays.fill(result, -1);
        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < n; i++) {
            while (hasGreaterElement(stack, nums, i)) {
                int index = stack.pop();
                result[index] = nums[i];
            }
            stack.push(i);
        }
        return result;
    }

    /**
     * Calculates the number of days to wait for a warmer temperature.
     * Uses a monotonic stack approach.
     *
     * @param temperatures The input array of daily temperatures
     * @return An array where each element is the number of days to wait for a warmer temperature
     */
    public int[] dailyTemperatures(int[] temperatures) {
        int n = temperatures.length;
        int[] result = new int[n];
        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < n; i++) {
            while (hasWarmerTemperature(stack, temperatures, i)) {
                int prevIndex = stack.pop();
                result[prevIndex] = i - prevIndex;
            }
            stack.push(i);
        }

        return result;
    }

    private boolean hasGreaterElement(Deque<Integer> stack, int[] nums, int currentIndex) {
        return !stack.isEmpty() && nums[currentIndex] > nums[stack.peek()];
    }

    private boolean hasWarmerTemperature(Deque<Integer> stack, int[] temperatures, int currentIndex) {
        return !stack.isEmpty() && temperatures[currentIndex] > temperatures[stack.peek()];
    }
}