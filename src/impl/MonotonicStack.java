package patterns.java;

import java.util.Arrays;
import java.util.Deque;
import java.util.ArrayDeque;

public class MonotonicStack {

    public int[] nextGreaterElement(int[] nums) {
        int n = nums.length;
        int[] result = new int[n];
        Arrays.fill(result, -1);
        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < n; i++) {
            while (isStackNotEmptyAndCurrentGreaterThanTop(stack, nums, i)) {
                int index = stack.pop();
                result[index] = nums[i];
            }
            stack.push(i);
        }
        return result;
    }

    public int[] dailyTemperatures(int[] temperatures) {
        int n = temperatures.length;
        int[] result = new int[n];
        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < n; i++) {
            while (isStackNotEmptyAndCurrentWarmerThanTop(stack, temperatures, i)) {
                int prevIndex = stack.pop();
                result[prevIndex] = i - prevIndex;
            }
            stack.push(i);
        }

        return result;
    }

    private boolean isStackNotEmptyAndCurrentGreaterThanTop(Deque<Integer> stack, int[] nums, int currentIndex) {
        return !stack.isEmpty() && nums[currentIndex] > nums[stack.peek()];
    }

    private boolean isStackNotEmptyAndCurrentWarmerThanTop(Deque<Integer> stack, int[] temperatures, int currentIndex) {
        return !stack.isEmpty() && temperatures[currentIndex] > temperatures[stack.peek()];
    }
}