package patterns.java;

import java.util.Arrays;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.function.BiPredicate;

public class MonotonicStack {

    public int[] nextGreaterElement(int[] nums) {
        // Test 3
        int n = nums.length;
        int[] result = new int[n]; // Output array
        Arrays.fill(result, -1); // Default to -1 if no greater element exists

        BiPredicate<Integer, Integer> condition = (current, top) -> nums[current] > nums[top];
        applyMonotonicStack(nums, result, condition, (i, top) -> nums[i]);

        return result;
    }

    public int[] dailyTemperatures(int[] temperatures) {
        int n = temperatures.length;
        int[] result = new int[n]; // Result array initialized with 0s

        BiPredicate<Integer, Integer> condition = (current, top) -> temperatures[current] > temperatures[top];
        applyMonotonicStack(temperatures, result, condition, (i, top) -> i - top);

        return result;
    }

    private void applyMonotonicStack(int[] array, int[] result, BiPredicate<Integer, Integer> condition, ResultComputer resultComputer) {
        Deque<Integer> stack = new ArrayDeque<>(); // Monotonic decreasing stack (stores indices)

        // Iterate through the array
        for (int i = 0; i < array.length; i++) {
            processStackTop(i, stack, result, condition, resultComputer);
            stack.push(i); // Push current index onto the stack
        }
    }

    private void processStackTop(int currentIndex, Deque<Integer> stack, int[] result, BiPredicate<Integer, Integer> condition, ResultComputer resultComputer) {
        while (!stack.isEmpty() && condition.test(currentIndex, stack.peek())) {
            int prevIndex = stack.pop();
            result[prevIndex] = resultComputer.compute(currentIndex, prevIndex);
        }
    }

    @FunctionalInterface
    private interface ResultComputer {
        int compute(int currentIndex, int prevIndex);
    }
}