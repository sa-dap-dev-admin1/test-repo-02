package patterns.java;

import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.Deque;

public final class MonotonicStack {

    public int[] nextGreaterElement(int[] nums) {
        // Test 3
        return findNextElement(nums, (current, top) -> current > top);
    }

    public int[] dailyTemperatures(int[] temperatures) {
        return findNextElement(temperatures, (current, top) -> current > top);
    }

    private int[] findNextElement(int[] arr, ElementComparator comparator) {
        int n = arr.length;
        int[] result = new int[n];
        Arrays.fill(result, -1);
        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < n; i++) {
            while (!stack.isEmpty() && comparator.compare(arr[i], arr[stack.peek()])) {
                int index = stack.pop();
                result[index] = i - index;
            }
            stack.push(i);
        }

        return result;
    }

    @FunctionalInterface
    private interface ElementComparator {
        boolean compare(int current, int top);
    }
}