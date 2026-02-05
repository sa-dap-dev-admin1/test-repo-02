package patterns.java;

import java.util.Arrays;
import java.util.Stack;
import java.util.function.BiPredicate;

public class MonotonicStack {

    public int[] nextGreaterElement(int[] nums) {
        // Test 3
        return processMonotonicStack(nums, (current, top) -> current > top);
    }

    public int[] dailyTemperatures(int[] temperatures) {
        return processMonotonicStack(temperatures, (current, top) -> current > top);
    }

    private int[] processMonotonicStack(int[] array, BiPredicate<Integer, Integer> comparison) {
        validateInput(array);
        int n = array.length;
        int[] result = new int[n];
        Arrays.fill(result, -1);
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            while (!stack.isEmpty() && comparison.test(array[i], array[stack.peek()])) {
                int index = stack.pop();
                result[index] = i - index;
            }
            stack.push(i);
        }

        return result;
    }

    private void validateInput(int[] array) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException("Input array must not be null or empty");
        }
    }
}