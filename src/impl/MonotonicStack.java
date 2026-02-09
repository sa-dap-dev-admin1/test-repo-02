package patterns.java;

import java.util.Arrays;
import java.util.Stack;
//test 2345f
public class MonotonicStack {

    public int[] nextGreaterElement(int[] nums) {
        return findNextGreaterElements(nums);
    }

    public int[] dailyTemperatures(int[] temperatures) {
        return findNextGreaterElements(temperatures, true);
    }

    private int[] findNextGreaterElements(int[] arr) {
        return findNextGreaterElements(arr, false);
    }

    private int[] findNextGreaterElements(int[] arr, boolean isTemperature) {
        if (arr == null || arr.length == 0) {
            return new int[0];
        }

        int n = arr.length;
        int[] result = new int[n];
        Arrays.fill(result, isTemperature ? 0 : -1);
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            while (!stack.isEmpty() && arr[i] > arr[stack.peek()]) {
                int prevIndex = stack.pop();
                result[prevIndex] = isTemperature ? i - prevIndex : arr[i];
            }
            stack.push(i);
        }

        return result;
    }
}