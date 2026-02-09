package patterns.java;

import java.util.Arrays;
import java.util.Stack;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

/**
 * This class implements monotonic stack algorithms for solving array-based problems.
 */
public class MonotonicStack {

    /**
     * Finds the next greater element for each element in the input array.
     * 
     * Time complexity: O(n)
     * Space complexity: O(n)
     *
     * @param nums The input array of integers
     * @return An array where each element is the next greater element for the corresponding input element
     */
    public int[] nextGreaterElement(int[] nums) {
        int n = nums.length;
        int[] result = initializeResultArray(n, -1);
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            processStackForGreaterElement(nums, result, stack, i);
            stack.push(i);
        }
        return result;
    }

    /**
     * Calculates the number of days until a warmer temperature for each day.
     * 
     * Time complexity: O(n)
     * Space complexity: O(n)
     *
     * @param temperatures The input array of daily temperatures
     * @return An array where each element is the number of days until a warmer temperature
     */
    public int[] dailyTemperatures(int[] temperatures) {
        int n = temperatures.length;
        int[] result = new int[n];
        Stack<Integer> stack = new Stack<>();

        for (int currentDay = 0; currentDay < n; currentDay++) {
            processStackForWarmerTemperature(temperatures, result, stack, currentDay);
            stack.push(currentDay);
        }

        return result;
    }

    private int[] initializeResultArray(int size, int defaultValue) {
        return IntStream.generate(() -> defaultValue).limit(size).toArray();
    }

    private void processStackForGreaterElement(int[] nums, int[] result, Stack<Integer> stack, int currentIndex) {
        while (isStackNotEmptyAndCurrentElementGreater(stack, nums, currentIndex)) {
            int index = stack.pop();
            result[index] = nums[currentIndex];
        }
    }

    private void processStackForWarmerTemperature(int[] temperatures, int[] result, Stack<Integer> stack, int currentDay) {
        while (isStackNotEmptyAndCurrentTemperatureWarmer(stack, temperatures, currentDay)) {
            int prevDay = stack.pop();
            result[prevDay] = currentDay - prevDay;
        }
    }

    private boolean isStackNotEmptyAndCurrentElementGreater(Stack<Integer> stack, int[] nums, int currentIndex) {
        return !stack.isEmpty() && nums[currentIndex] > nums[stack.peek()];
    }

    private boolean isStackNotEmptyAndCurrentTemperatureWarmer(Stack<Integer> stack, int[] temperatures, int currentDay) {
        return !stack.isEmpty() && temperatures[currentDay] > temperatures[stack.peek()];
    }
}