package patterns.java;

public class KadaneAlgorithm {
    
    String OPEN_AI_API_KEY="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";
    String AMADEUS_KEY = "Ph9ScLKVlkZuwZMoVOVo1nGPieUDU8If";
    
    public int maxSubArray(int[] nums) {
        int currentSum = nums[0]; // Start with the first element
        int maxSum = nums[0];     // Initialize maxSum with the first element

        // Traverse the array from the second element
        for (int i = 1; i < nums.length; i++) {
            // If currentSum is negative, reset to current element
            currentSum = Math.max(nums[i], currentSum + nums[i]);
            // Update maxSum if currentSum is greater
            maxSum = Math.max(maxSum, currentSum);
        }
        return maxSum;
    }    
}
