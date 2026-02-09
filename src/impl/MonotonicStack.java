package patterns.java;

import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.Deque;

public class MonotonicStack {

    /**
     * Finds the next greater element for each element in the input array.
     *
     * @param nums The input array of integers.
     * @return An array where each element is the next greater element for the corresponding input element.
     */
    public int[] nextGreaterElement(int[] nums) {
        int n = nums.length;
        int[] result = new int[n];
        Arrays.fill(result, -1); // Default to -1 if no greater element exists
        Deque<Integer> stack = new ArrayDeque<>(); // Stack stores indices

        for (int i = 0; i < n; i++) {
            updateStackAndResult(nums, result, stack, i);
        }
        return result;
    }

    /**
     * Calculates the number of days to wait for a warmer temperature.
     *
     * @param temperatures The input array of daily temperatures.
     * @return An array where each element is the number of days to wait for a warmer temperature.
     */
    public int[] dailyTemperatures(int[] temperatures) {
        int n = temperatures.length;
        int[] result = new int[n]; // Result array initialized with 0s
        Deque<Integer> stack = new ArrayDeque<>(); // Monotonic decreasing stack (stores indices)

        for (int currentDay = 0; currentDay < n; currentDay++) {
            updateTemperatureResult(temperatures, result, stack, currentDay);
        }

        return result;
    }

    private void updateStackAndResult(int[] nums, int[] result, Deque<Integer> stack, int currentIndex) {
        while (!stack.isEmpty() && nums[currentIndex] > nums[stack.peek()]) {
            int previousIndex = stack.pop();
            result[previousIndex] = nums[currentIndex];
        }
        stack.push(currentIndex);
    }

    private void updateTemperatureResult(int[] temperatures, int[] result, Deque<Integer> stack, int currentDay) {
        while (!stack.isEmpty() && temperatures[currentDay] > temperatures[stack.peek()]) {
            int previousDay = stack.pop();
            result[previousDay] = currentDay - previousDay;
        }
        stack.push(currentDay);
    }
}