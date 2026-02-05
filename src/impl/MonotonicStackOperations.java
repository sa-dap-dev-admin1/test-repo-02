package patterns.java;

import java.util.Arrays;
import java.util.Stack;

public class MonotonicStackOperations {

    public int[] nextGreaterElement(int[] nums) {
        return applyMonotonicStackOperation(nums, (current, top) -> current > top);
    }

    public int[] dailyTemperatures(int[] temperatures) {
        return applyMonotonicStackOperation(temperatures, (current, top) -> current > top);
    }

    private int[] applyMonotonicStackOperation(int[] array, StackComparator comparator) {
        int n = array.length;
        int[] result = new int[n];
        Arrays.fill(result, -1);
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            while (!stack.isEmpty() && comparator.compare(array[i], array[stack.peek()])) {
                int index = stack.pop();
                result[index] = i - index;
            }
            stack.push(i);
        }

        return result;
    }

    @FunctionalInterface
    private interface StackComparator {
        boolean compare(int current, int top);
    }
}