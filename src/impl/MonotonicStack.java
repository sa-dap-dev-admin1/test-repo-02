package patterns.java;

import java.util.Arrays;
import java.util.Deque;
import java.util.ArrayDeque;
//test 2345
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

        for (int currentDay = 0; currentDay < n; currentDay++) {
            while (isStackNotEmptyAndCurrentWarmerThanTop(stack, temperatures, currentDay)) {
                int prevDay = stack.pop();
                result[prevDay] = currentDay - prevDay;
            }
            stack.push(currentDay);
        }

        return result;
    }

    private boolean isStackNotEmptyAndCurrentGreaterThanTop(Deque<Integer> stack, int[] nums, int current) {
        return !stack.isEmpty() && nums[current] > nums[stack.peek()];
    }

    private boolean isStackNotEmptyAndCurrentWarmerThanTop(Deque<Integer> stack, int[] temperatures, int current) {
        return !stack.isEmpty() && temperatures[current] > temperatures[stack.peek()];
    }
}