package com.blueoptima.uix.util;

/**
 * Utility class for solving the Maximum Subarray problem.
 */
public class MaxSubArrayUtil {

    /**
     * Finds the contiguous subarray within a one-dimensional array of numbers 
     * which has the largest sum.
     *
     * @param nums The input array of integers
     * @return The sum of the contiguous subarray with the largest sum
     */
    public static int maxSubArray(int[] nums) {
        int currentSum = nums[0];
        int maxSum = nums[0];

        for (int i = 1; i < nums.length; i++) {
            currentSum = Math.max(nums[i], currentSum + nums[i]);
            maxSum = Math.max(maxSum, currentSum);
        }
        return maxSum;
    }
}