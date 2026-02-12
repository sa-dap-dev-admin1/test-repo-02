package com.blueoptima.uix.util;

public class ArrayAlgorithms {

    /**
     * Finds the contiguous subarray within a one-dimensional array of integers
     * which has the largest sum, and returns this sum.
     * This method implements Kadane's algorithm.
     *
     * @param nums The input array of integers
     * @return The sum of the contiguous subarray with the largest sum
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