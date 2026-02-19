package patterns.java;

import java.util.Arrays;
import java.util.Stack;

//test 2345fhdffff
public class MonotonicStack {
    private static final int NO_GREATER_ELEMENT = -1;

    /**
     * Finds the next greater element for each element in the input array.
     * 
     * @param nums The input array of integers
     * @return An array where each element is the next greater element for the corresponding input element
     */
    public int[] nextGreaterElement(int[] nums) {
        if (nums == null || nums.length == 0) {
            return new int[0];
        }
        int[] result = new int[nums.length];
        Arrays.fill(result, NO_GREATER_ELEMENT);
        MonotonicDecreasingStack stack = new MonotonicDecreasingStack();

        for (int i = 0; i < nums.length; i++) {
            updateStackForNextGreater(nums, result, stack, i);
        }
        return result;
    }

    /**
     * Calculates the number of days to wait for a warmer temperature.
     * 
     * @param temperatures The input array of daily temperatures
     * @return An array where each element is the number of days to wait for a warmer temperature
     */
    public int[] dailyTemperatures(int[] temperatures) {
        if (temperatures == null || temperatures.length == 0) {
            return new int[0];
        }
        int[] result = new int[temperatures.length];
        MonotonicDecreasingStack stack = new MonotonicDecreasingStack();

        for (int i = 0; i < temperatures.length; i++) {
            updateStack(temperatures, result, stack, i);
        }
        return result;
    }

    private void updateStack(int[] values, int[] result, MonotonicDecreasingStack stack, int currentIndex) {
        while (!stack.isEmpty() && values[currentIndex] > values[stack.peek()]) {
            int topIndex = stack.pop();
            result[topIndex] = currentIndex - topIndex;
        }
        stack.push(currentIndex);
    }

    private void updateStackForNextGreater(int[] values, int[] result, MonotonicDecreasingStack stack, int currentIndex) {
        while (!stack.isEmpty() && values[currentIndex] > values[stack.peek()]) {
            int topIndex = stack.pop();
            result[topIndex] = values[currentIndex];
        }
        stack.push(currentIndex);
    }

    private static class MonotonicDecreasingStack {
        private final Stack<Integer> stack = new Stack<>();

        public void push(int value) {
            stack.push(value);
        }

        public int pop() {
            return stack.pop();
        }

        public int peek() {
            return stack.peek();
        }

        public boolean isEmpty() {
            return stack.isEmpty();
        }
    }
}