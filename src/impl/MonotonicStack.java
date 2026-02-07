package patterns.java;

import java.util.Stack;

public class MonotonicStack {
    private StackOperationStrategy strategy;

    public MonotonicStack(StackOperationStrategy strategy) {
        this.strategy = strategy;
    }

    public int[] nextGreaterElement(int[] nums) {
        return strategy.processArray(nums);
    }

    public interface StackOperationStrategy {
        int[] processArray(int[] nums);
    }

    public static class NextGreaterElementStrategy implements StackOperationStrategy {
        @Override
        public int[] processArray(int[] nums) {
            int n = nums.length;
            int[] result = new int[n];
            Stack<Integer> stack = new Stack<>();

            for (int i = n - 1; i >= 0; i--) {
                while (!stack.isEmpty() && nums[i] >= nums[stack.peek()]) {
                    stack.pop();
                }
                result[i] = stack.isEmpty() ? -1 : nums[stack.peek()];
                stack.push(i);
            }
            return result;
        }
    }
}