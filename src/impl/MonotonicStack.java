package patterns.java;

import java.util.Arrays;
import java.util.Stack;

public class MonotonicStack {
    private static final int NO_GREATER_ELEMENT = -1;

    public int[] nextGreaterElement(int[] nums) {
        int n = nums.length;
        int[] result = new int[n]; // Output array
        Arrays.fill(result, NO_GREATER_ELEMENT); // Default to -1 if no greater element exists
        Stack<Integer> stack = new Stack<>(); // Stack stores indices

        // Iterate through the array using monotonic stack pattern
        for (int i = 0; i < n; i++) {
            updateStack(nums, result, stack, i, (index, value) -> value);
        }
        return result;
    }

    public int[] dailyTemperatures(int[] temperatures) {
        int n = temperatures.length;
        int[] result = new int[n]; // Result array initialized with 0s
        Stack<Integer> stack = new Stack<>(); // Monotonic decreasing stack (stores indices)

        // Iterate through the temperature array using monotonic stack pattern
        for (int i = 0; i < n; i++) {
            updateStack(temperatures, result, stack, i, (index, value) -> index - value);
        }
        return result; // Return the computed results
    }

    private void updateStack(int[] values, int[] result, Stack<Integer> stack, int currentIndex, ResultUpdater updater) {
        // While stack is not empty and current element is greater than stack top
        while (!stack.isEmpty() && values[currentIndex] > values[stack.peek()]) {
            int topIndex = stack.pop(); // Pop the top element
            result[topIndex] = updater.update(currentIndex, topIndex); // Update the result
        }
        stack.push(currentIndex); // Push the current index onto the stack
    }

    @FunctionalInterface
    private interface ResultUpdater {
        int update(int currentIndex, int stackTopIndex);
    }
}