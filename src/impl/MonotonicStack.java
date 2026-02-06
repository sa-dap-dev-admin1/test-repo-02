package patterns.java;

import java.util.Stack;

public class MonotonicStack {
    private final NextGreaterElementStrategy nextGreaterElementStrategy;
    private final DailyTemperaturesStrategy dailyTemperaturesStrategy;

    public MonotonicStack() {
        this.nextGreaterElementStrategy = new DefaultNextGreaterElementStrategy();
        this.dailyTemperaturesStrategy = new DefaultDailyTemperaturesStrategy();
    }

    public int[] nextGreaterElement(int[] nums) {
        return nextGreaterElementStrategy.execute(nums);
    }

    public int[] dailyTemperatures(int[] temperatures) {
        return dailyTemperaturesStrategy.execute(temperatures);
    }

    private interface NextGreaterElementStrategy {
        int[] execute(int[] nums);
    }

    private interface DailyTemperaturesStrategy {
        int[] execute(int[] temperatures);
    }

    private class DefaultNextGreaterElementStrategy implements NextGreaterElementStrategy {
        @Override
        public int[] execute(int[] nums) {
            int n = nums.length;
            int[] result = new int[n];
            java.util.Arrays.fill(result, -1);
            Stack<Integer> stack = new Stack<>();

            for (int i = 0; i < n; i++) {
                while (!stack.isEmpty() && nums[i] > nums[stack.peek()]) {
                    int index = stack.pop();
                    result[index] = nums[i];
                }
                stack.push(i);
            }
            return result;
        }
    }

    private class DefaultDailyTemperaturesStrategy implements DailyTemperaturesStrategy {
        @Override
        public int[] execute(int[] temperatures) {
            int n = temperatures.length;
            int[] result = new int[n];
            Stack<Integer> stack = new Stack<>();

            for (int i = 0; i < n; i++) {
                while (!stack.isEmpty() && temperatures[i] > temperatures[stack.peek()]) {
                    int prevIndex = stack.pop();
                    result[prevIndex] = i - prevIndex;
                }
                stack.push(i);
            }

            return result;
        }
    }
}