package patterns.java;

import java.util.Arrays;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.IntStream;

//test 2345fhdfffff
public class MonotonicStack {
    private static final int NO_GREATER_ELEMENT = -1;

    public int[] nextGreaterElement(int[] nums) {
        if (nums == null || nums.length == 0) {
            return new int[0];
        }

        int n = nums.length;
        int[] result = IntStream.generate(() -> NO_GREATER_ELEMENT).limit(n).toArray();
        Stack<Integer> stack = new Stack<>();

        // Iterate through the array
        for (int i = 0; i < n; i++) {
            updateStack(nums, result, stack, i, (index, value) -> value);
        }
        return result;
    }

    public int[] dailyTemperatures(int[] temperatures) {
        if (temperatures == null || temperatures.length == 0) {
            return new int[0];
        }

        int n = temperatures.length;
        int[] result = new int[n]; // Result array initialized with 0s
        Stack<Integer> stack = new Stack<>(); // Monotonic decreasing stack (stores indices)

        // Iterate through the temperature array
        for (int i = 0; i < n; i++) {
            updateStack(temperatures, result, stack, i, (index, currentIndex) -> currentIndex - index);
        }

        return result; // Return the computed results
    }

    private void updateStack(int[] values, int[] result, Stack<Integer> stack, int currentIndex, 
                             ResultUpdater updater) {
        // While stack is not empty and current element is greater than stack top
        while (!stack.isEmpty() && values[currentIndex] > values[stack.peek()]) {
            int topIndex = stack.pop(); // Pop the top element
            result[topIndex] = updater.update(topIndex, currentIndex); // Update the result
        }
        stack.push(currentIndex); // Push the current index onto the stack
    }

    @FunctionalInterface
    private interface ResultUpdater {
        int update(int index, int currentIndex);
    }
}