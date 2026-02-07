package patterns.java;

import java.util.Stack;

public class TemperatureAnalyzer {
    private StackOperations stackOps;

    public TemperatureAnalyzer(StackOperations stackOps) {
        this.stackOps = stackOps;
    }

    public int[] dailyTemperatures(int[] temperatures) {
        return stackOps.processTemperatures(temperatures);
    }

    public interface StackOperations {
        int[] processTemperatures(int[] temperatures);
    }

    public static class MonotonicStackOperations implements StackOperations {
        @Override
        public int[] processTemperatures(int[] temperatures) {
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