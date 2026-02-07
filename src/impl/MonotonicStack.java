package patterns.java;

import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Class containing methods for solving problems using monotonic stack pattern.
 */
public class MonotonicStack {

    /**
     * Finds the next greater element for each element in the given array.
     *
     * @param nums The input array of integers.
     * @return An array where each element is the next greater element for the corresponding element in nums.
     */
    public int[] nextGreaterElement(int[] nums) {
        int arrayLength = nums.length;
        int[] result = new int[arrayLength];
        Arrays.fill(result, -1);
        
        processMonotonicStack(nums, (i, top) -> {
            result[top] = nums[i];
            return true;
        });
        
        return result;
    }

    /**
     * Calculates the number of days to wait for a warmer temperature.
     *
     * @param temperatures The input array of daily temperatures.
     * @return An array where each element represents the number of days to wait for a warmer temperature.
     */
    public int[] dailyTemperatures(int[] temperatures) {
        int arrayLength = temperatures.length;
        int[] result = new int[arrayLength];
        
        processMonotonicStack(temperatures, (i, top) -> {
            result[top] = i - top;
            return true;
        });
        
        return result;
    }

    /**
     * Helper method to process the array using a monotonic stack pattern.
     *
     * @param arr The input array to process.
     * @param onPop A lambda function to execute when an element is popped from the stack.
     */
    private void processMonotonicStack(int[] arr, StackPopOperation onPop) {
        Deque<Integer> stack = new ArrayDeque<>();
        
        for (int i = 0; i < arr.length; i++) {
            while (!stack.isEmpty() && arr[i] > arr[stack.peek()]) {
                int top = stack.pop();
                onPop.execute(i, top);
            }
            stack.push(i);
        }
    }

    /**
     * Functional interface for stack pop operation.
     */
    @FunctionalInterface
    private interface StackPopOperation {
        boolean execute(int currentIndex, int stackTopIndex);
    }
}