package patterns.java;

import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.Deque;

public class MonotonicStack {

    public int[] nextGreaterElement(int[] nums) {
        if (nums == null || nums.length == 0) {
            return new int[0];
        }
        int n = nums.length;
        int[] result = new int[n];
        Arrays.fill(result, -1);
        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < n; i++) {
            processStack(nums, result, stack, i, (current, top) -> current > top);
        }
        return result;
    }

    public int[] dailyTemperatures(int[] temperatures) {
        if (temperatures == null || temperatures.length == 0) {
            return new int[0];
        }
        int n = temperatures.length;
        int[] result = new int[n];
        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < n; i++) {
            processStack(temperatures, result, stack, i, (current, top) -> current > top);
        }

        return result;
    }

    private void processStack(int[] array, int[] result, Deque<Integer> stack, int currentIndex, ComparisonStrategy strategy) {
        while (!stack.isEmpty() && strategy.compare(array[currentIndex], array[stack.peek()])) {
            int prevIndex = stack.pop();
            result[prevIndex] = currentIndex - prevIndex;
        }
        stack.push(currentIndex);
    }

    @FunctionalInterface
    private interface ComparisonStrategy {
        boolean compare(int current, int top);
    }
}