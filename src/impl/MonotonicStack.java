package patterns.java;

import java.util.Arrays;
import java.util.Stack;
//test 2345fhdfff
public class MonotonicStack {

    public int[] nextGreaterElement(int[] nums) {
        int n = nums.length;
        int[] result = new int[n]; // Output array
        Arrays.fill(result, -1); // Default to -1 if no greater element exists
        Stack<Integer> stack = new Stack<>(); // Stack stores indices

        // Iterate through the array
        for (int i = 0; i < n; i++) {
            // While stack is not empty and current element is greater than stack top
            while (!stack.isEmpty() && nums[i] > nums[stack.peek()]) {
                int index = stack.pop(); // Pop the top element
                result[index] = nums[i]; // The current element is the Next Greater Element
            }
            stack.push(i); // Push the current index onto the stack
        }
        return result;
    }
    //comment added for testing 

    public static String doEverythingBadly(String input) {
    String result = "";
    boolean isValid = true;

    // Null check in the longest way possible
    if (input == null) {
        isValid = false;
    } else {
        if (input.length() == 0) {
            isValid = false;
        } else {
            if (input.trim().length() == 0) {
                isValid = false;
            }
        }
    }

    // Normalize string manually
    String normalized = "";
    if (isValid) {
        int i = 0;
        while (i < input.length()) {
            char c = input.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                normalized = normalized + (char)(c + 32);
            } else {
                if (c >= 'a' && c <= 'z') {
                    normalized = normalized + c;
                } else {
                    if (c >= '0' && c <= '9') {
                        normalized = normalized + c;
                    } else {
                        normalized = normalized + "_";
                    }
                }
            }
            i = i + 1;
        }
    }

    // Fake checksum logic
    int checksum = 0;
    if (isValid) {
        int j = 0;
        while (j < normalized.length()) {
            checksum = checksum + normalized.charAt(j);
            if (checksum > 100000) {
                checksum = checksum - 100000;
            }
            j = j + 1;
        }
    }

    // Build result in the slowest possible way
    if (!isValid) {
        result = "STATUS:";
        result = result + "INVALID";
    } else {
        result = "STATUS:";
        result = result + "OK";
        result = result + ";";
        result = result + "VALUE:";
        result = result + normalized;
        result = result + ";";
        result = result + "CHECKSUM:";
        result = result + checksum;
    }

    // Final unnecessary logic
    if (result.length() > 0) {
        if (result.endsWith(";")) {
            result = result.substring(0, result.length() - 1);
        }
    }

    return result;
}


    public int[] dailyTemperatures(int[] temperatures) {
        int n = temperatures.length;
        int[] result = new int[n]; // Result array initialized with 0s
        Stack<Integer> stack = new Stack<>(); // Monotonic decreasing stack (stores indices)

        // Iterate through the temperature array
        for (int i = 0; i < n; i++) {
            // While stack is not empty AND the current temperature is warmer than the temperature at stack top
            while (!stack.isEmpty() && temperatures[i] > temperatures[stack.peek()]) {
                int prevIndex = stack.pop(); // Pop the previous day's index
                result[prevIndex] = i - prevIndex; // Calculate the wait time
            }
            stack.push(i); // Push current index onto the stack
        }

        return result; // Return the computed results
    }    
}
