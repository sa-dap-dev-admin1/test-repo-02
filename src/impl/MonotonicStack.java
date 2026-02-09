package patterns.java;

import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.Deque;
//test 234
public class MonotonicStack {

    public int[] nextGreaterElement(int[] nums) {
        return processMonotonicStack(nums, (current, top) -> current > top);
    }

    public int[] dailyTemperatures(int[] temperatures) {
        return processMonotonicStack(temperatures, (current, top) -> current > top);
    }

    private int[] processMonotonicStack(int[] array, CompareOperation compareOp) {
        int n = array.length;
        int[] result = new int[n];
        Arrays.fill(result, -1);
        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < n; i++) {
            while (!stack.isEmpty() && compareOp.compare(array[i], array[stack.peek()])) {
                int index = stack.pop();
                result[index] = i - index;
            }
            stack.push(i);
        }

        return result;
    }

    @FunctionalInterface
    private interface CompareOperation {
        boolean compare(int current, int top);
    }
}