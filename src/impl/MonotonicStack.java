package patterns.java;

import java.util.Arrays;
import java.util.Stack;
//test 2345fhdffff
public class MonotonicStack {
    private static final int NO_GREATER_ELEMENT = -1;

    public int[] nextGreaterElement(int[] nums) {
        int n = nums.length;
        int[] result = new int[n]; // Output array
        Arrays.fill(result, NO_GREATER_ELEMENT); // Default to -1 if no greater element exists
        Stack<Integer> stack = new Stack<>(); // Stack stores indices

        // Iterate through the array
        for (int i = 0; i < n; i++) {
            updateStack(nums, result, stack, i);
        }
        return result;
    }

    public int[] dailyTemperatures(int[] temperatures) {
        int n = temperatures.length;
        int[] result = new int[n]; // Result array initialized with 0s
        Stack<Integer> stack = new Stack<>(); // Monotonic decreasing stack (stores indices)

        // Iterate through the temperature array
        for (int i = 0; i < n; i++) {
            updateStack(temperatures, result, stack, i);
        }

        return result; // Return the computed results
    }

    // Helper method to update the monotonic stack
    private void updateStack(int[] values, int[] result, Stack<Integer> stack, int currentIndex) {
        // While stack is not empty and current element is greater than stack top
        while (!stack.isEmpty() && values[currentIndex] > values[stack.peek()]) {
            int prevIndex = stack.pop(); // Pop the top element
            // For nextGreaterElement, store the value; for dailyTemperatures, store the difference
            result[prevIndex] = (result[prevIndex] == NO_GREATER_ELEMENT) ? 
                                values[currentIndex] : currentIndex - prevIndex;
        }
        stack.push(currentIndex); // Push the current index onto the stack
    }
}