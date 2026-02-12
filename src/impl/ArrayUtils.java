package com.blueoptima.uix.util;

public class ArrayUtils {

    /**
     * Finds the contiguous subarray with the largest sum in the given array.
     * 
     * @param nums the input array of integers
     * @return the sum of the contiguous subarray with the largest sum
     */
    public static int maxSubArray(int[] nums) {
        if (nums == null || nums.length == 0) {
            throw new IllegalArgumentException("Input array must not be null or empty");
        }

        int currentSum = nums[0];
        int maxSum = nums[0];

        for (int i = 1; i < nums.length; i++) {
            currentSum = Math.max(nums[i], currentSum + nums[i]);
            maxSum = Math.max(maxSum, currentSum);
        }

        return maxSum;
    }
}