package patterns.java;

import java.util.Arrays;
import java.util.Stack;

public class MonotonicStack {

    private static final int DEFAULT_VALUE = -1;

    public int[] nextGreaterElement(int[] nums) {
        // Test 3
        if (nums == null || nums.length == 0) {
            return new int[0];
        }

        int[] result = initializeResultArray(nums.length);
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < nums.length; i++) {
            processStack(nums, result, stack, i);
            stack.push(i);
        }

        return result;
    }

    public int[] dailyTemperatures(int[] temperatures) {
        if (temperatures == null || temperatures.length == 0) {
            return new int[0];
        }

        int[] result = new int[temperatures.length];
        Stack<Integer> stack = new Stack<>();

        for (int currentDay = 0; currentDay < temperatures.length; currentDay++) {
            int currentTemperature = temperatures[currentDay];
            processTemperatureStack(temperatures, result, stack, currentDay, currentTemperature);
            stack.push(currentDay);
        }

        return result;
    }

    private int[] initializeResultArray(int length) {
        int[] result = new int[length];
        Arrays.fill(result, DEFAULT_VALUE);
        return result;
    }

    private void processStack(int[] nums, int[] result, Stack<Integer> stack, int currentIndex) {
        while (isStackNotEmptyAndCurrentElementGreater(stack, nums, currentIndex)) {
            int index = stack.pop();
            result[index] = nums[currentIndex];
        }
    }

    private boolean isStackNotEmptyAndCurrentElementGreater(Stack<Integer> stack, int[] nums, int currentIndex) {
        return !stack.isEmpty() && nums[currentIndex] > nums[stack.peek()];
    }

    private void processTemperatureStack(int[] temperatures, int[] result, Stack<Integer> stack, int currentDay, int currentTemperature) {
        while (isStackNotEmptyAndCurrentTemperatureWarmer(stack, temperatures, currentTemperature)) {
            int prevDay = stack.pop();
            result[prevDay] = currentDay - prevDay;
        }
    }

    private boolean isStackNotEmptyAndCurrentTemperatureWarmer(Stack<Integer> stack, int[] temperatures, int currentTemperature) {
        return !stack.isEmpty() && currentTemperature > temperatures[stack.peek()];
    }
}