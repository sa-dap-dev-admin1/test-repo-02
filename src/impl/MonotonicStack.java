package patterns.java;

import java.util.Arrays;
import java.util.Stack;
//test 2345fhdf
public class MonotonicStack {
    private static final int DEFAULT_VALUE = -1;

    public int[] nextGreaterElement(int[] nums) {
        int[] result = new int[nums.length]; // Output array
        Arrays.fill(result, DEFAULT_VALUE); // Default to -1 if no greater element exists
        return processMonotonicStack(nums, result, (current, top) -> current > top);
    }

    public int[] dailyTemperatures(int[] temperatures) {
        int[] result = new int[temperatures.length]; // Result array initialized with 0s
        return processMonotonicStack(temperatures, result, (current, top) -> current > top);
    }

    // Helper method to process monotonic stack
    private int[] processMonotonicStack(int[] array, int[] result, Comparator comparator) {
        Stack<Integer> stack = new Stack<>(); // Stack stores indices
        
        // Iterate through the array
        for (int i = 0; i < array.length; i++) {
            updateStack(array, result, stack, i, comparator);
            stack.push(i); // Push the current index onto the stack
        }
        
        return result;
    }

    // Helper method to update the stack and result array
    private void updateStack(int[] array, int[] result, Stack<Integer> stack, int currentIndex, Comparator comparator) {
        // While stack is not empty and current element satisfies the comparison condition
        while (!stack.isEmpty() && comparator.compare(array[currentIndex], array[stack.peek()])) {
            int prevIndex = stack.pop(); // Pop the top element
            result[prevIndex] = currentIndex - prevIndex; // Calculate the result
        }
    }

    // Functional interface for comparison
    @FunctionalInterface
    private interface Comparator {
        boolean compare(int current, int top);
    }
}