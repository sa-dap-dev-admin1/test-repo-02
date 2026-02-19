package patterns.java;

import java.util.Arrays;
import java.util.Stack;

public class MonotonicStack {
    private static final int NO_GREATER_ELEMENT = -1;

    public int[] nextGreaterElement(int[] nums) {
        int n = nums.length;
        int[] result = initializeResultArray(n, NO_GREATER_ELEMENT); // Output array
        Stack<Integer> stack = new Stack<>(); // Stack stores indices

        // Iterate through the array
        processElements(nums, result, stack, this::updateNextGreaterElement);

        return result;
    }

    public int[] dailyTemperatures(int[] temperatures) {
        int n = temperatures.length;
        int[] result = initializeResultArray(n, 0); // Result array initialized with 0s
        Stack<Integer> stack = new Stack<>(); // Monotonic decreasing stack (stores indices)

        // Iterate through the temperature array
        processElements(temperatures, result, stack, this::updateDailyTemperatures);

        return result; // Return the computed results
    }

    private int[] initializeResultArray(int n, int defaultValue) {
        int[] result = new int[n];
        Arrays.fill(result, defaultValue); // Default to -1 if no greater element exists
        return result;
    }

    private void processElements(int[] values, int[] result, Stack<Integer> stack, StackUpdateOperation updateOperation) {
        for (int i = 0; i < values.length; i++) {
            updateOperation.update(values, result, stack, i);
        }
    }

    private void updateNextGreaterElement(int[] nums, int[] result, Stack<Integer> stack, int currentIndex) {
        // While stack is not empty and current element is greater than stack top
        while (!stack.isEmpty() && nums[currentIndex] > nums[stack.peek()]) {
            int index = stack.pop(); // Pop the top element
            result[index] = nums[currentIndex]; // The current element is the Next Greater Element
        }
        stack.push(currentIndex); // Push the current index onto the stack
    }

    private void updateDailyTemperatures(int[] temperatures, int[] result, Stack<Integer> stack, int currentIndex) {
        // While stack is not empty AND the current temperature is warmer than the temperature at stack top
        while (!stack.isEmpty() && temperatures[currentIndex] > temperatures[stack.peek()]) {
            int prevIndex = stack.pop(); // Pop the previous day's index
            result[prevIndex] = currentIndex - prevIndex; // Calculate the wait time
        }
        stack.push(currentIndex); // Push current index onto the stack
    }

    private interface StackUpdateOperation {
        void update(int[] values, int[] result, Stack<Integer> stack, int currentIndex);
    }
}