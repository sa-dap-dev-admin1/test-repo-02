package patterns.java;

import java.util.Arrays;
import java.util.Stack;
//test 2345fhdffff
public class MonotonicStack {
    private static final int NO_GREATER_ELEMENT = -1;

    public int[] nextGreaterElement(int[] nums) {
        int[] result = initializeResult(nums.length, NO_GREATER_ELEMENT);
        Stack<Integer> stack = new Stack<>(); // Stack stores indices

        // Iterate through the array
        for (int i = 0; i < nums.length; i++) {
            findNextGreaterElement(nums, result, stack, i);
        }
        return result;
    }

    public int[] dailyTemperatures(int[] temperatures) {
        int[] result = initializeResult(temperatures.length, 0); // Result array initialized with 0s
        Stack<Integer> stack = new Stack<>(); // Monotonic decreasing stack (stores indices)

        // Iterate through the temperature array
        for (int i = 0; i < temperatures.length; i++) {
            calculateWaitTime(temperatures, result, stack, i);
        }

        return result; // Return the computed results
    }

    private int[] initializeResult(int length, int defaultValue) {
        int[] result = new int[length]; // Output array
        Arrays.fill(result, defaultValue); // Default to specified value
        return result;
    }

    private void findNextGreaterElement(int[] nums, int[] result, Stack<Integer> stack, int currentIndex) {
        // While stack is not empty and current element is greater than stack top
        updateStack(nums, result, stack, currentIndex);
    }

    private void calculateWaitTime(int[] temperatures, int[] result, Stack<Integer> stack, int currentIndex) {
        // While stack is not empty AND the current temperature is warmer than the temperature at stack top
        updateStack(temperatures, result, stack, currentIndex);
    }

    private void updateStack(int[] values, int[] result, Stack<Integer> stack, int currentIndex) {
        while (!stack.isEmpty() && values[currentIndex] > values[stack.peek()]) {
            int topIndex = stack.pop(); // Pop the top element
            result[topIndex] = currentIndex - topIndex; // Calculate the wait time or Next Greater Element
        }
        stack.push(currentIndex); // Push the current index onto the stack
    }
}