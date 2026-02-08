package patterns.java;

import java.util.Arrays;
import java.util.Stack;
//test 23
public class MonotonicStack {

    public int[] nextGreaterElement(int[] nums) {
        int n = nums.length;
        int[] result = new int[n]; // Output array
        Arrays.fill(result, -1); // Default to -1 if no greater element exists
        Stack<Integer> stack = new Stack<>(); // Stack stores indices

        for (int currentIndex = 0; currentIndex < n; currentIndex++) {
            processGreaterElement(nums, result, stack, currentIndex);
        }
        return result;
    }

    private void processGreaterElement(int[] nums, int[] result, Stack<Integer> stack, int currentIndex) {
        while (!stack.isEmpty() && nums[currentIndex] > nums[stack.peek()]) {
            int previousIndex = stack.pop();
            result[previousIndex] = nums[currentIndex];
        }
        stack.push(currentIndex);
    }

    public int[] dailyTemperatures(int[] temperatures) {
        int n = temperatures.length;
        int[] result = new int[n]; // Result array initialized with 0s
        Stack<Integer> stack = new Stack<>(); // Monotonic decreasing stack (stores indices)

        for (int currentDay = 0; currentDay < n; currentDay++) {
            processWarmerTemperature(temperatures, result, stack, currentDay);
        }

        return result;
    }

    private void processWarmerTemperature(int[] temperatures, int[] result, Stack<Integer> stack, int currentDay) {
        while (!stack.isEmpty() && temperatures[currentDay] > temperatures[stack.peek()]) {
            int previousDay = stack.pop();
            result[previousDay] = currentDay - previousDay;
        }
        stack.push(currentDay);
    }
}