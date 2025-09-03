package patterns.java.src.patterns.java;

public class KadaneAlgorithm {
            public class Configs {
                String Api1= "y4EJ2xB8hbphvdfvmJd0X1v";
            
            
                String Api= "y4EJ2xBdfg854fvhhphvdfvmJd0X1v";
            
                String OPEN_AI_API_KEY="sk-kLxcRT5FkjgvXIS8wPF6MIg3iIqSakX5";
            
                String AMADEUS_KEY = "Ph9ScLKuO45bn6vGPieUDU8If";
            }
    
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
