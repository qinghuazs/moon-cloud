package com.moon.leetcode.arrays;

/**
 * 
给定一个无序数组arr，找到数组中未出现的最小正整数
例如arr = [-1, 2, 3, 4]。返回1
arr = [1, 2, 3, 4]。返回5
 */
public class MinNum {

    public int minNum2(int[] nums) {
        int n = nums.length;
        int ans = 1;
        for (int i = 0; i < n; i++) {
            if (nums[i] < ans && nums[i] < 1) {
                
            }
        }
        for (int i = 0; i < n; i++) {
            if (nums[i] != i + 1) {
                return i + 1;
            }
        }
        return n + 1;
    }
    /**
     * 找到数组中未出现的最小正整数
     * 时间复杂度：O(n)
     * 空间复杂度：O(1)
     */
    public int minNum(int[] nums) {
        int n = nums.length;
        
        // 将数组中的数放到对应的位置上
        // 例如：将数字1放到索引0的位置，数字2放到索引1的位置，以此类推
        for (int i = 0; i < n; i++) {
            // 当前数在有效范围内且不在正确位置上时，将其交换到正确位置
            while (nums[i] > 0 && nums[i] <= n && nums[nums[i] - 1] != nums[i]) {
                // 交换nums[i]和nums[nums[i] - 1]
                int temp = nums[nums[i] - 1];
                nums[nums[i] - 1] = nums[i];
                nums[i] = temp;
            }
        }
        
        // 遍历数组，找到第一个不在正确位置上的数
        for (int i = 0; i < n; i++) {
            if (nums[i] != i + 1) {
                return i + 1;
            }
        }
        
        // 如果所有数都在正确位置上，则返回n+1
        return n + 1;
    }
    
    /**
     * 测试方法
     */
    public static void main(String[] args) {
        MinNum solution = new MinNum();
        
        // 测试用例1: [-1, 2, 3, 4] 应返回 1
        int[] arr1 = {-1, 2, 3, 4};
        System.out.println("测试用例1: [-1, 2, 3, 4], 期望结果: 1, 实际结果: " + solution.minNum(arr1));
        
        // 测试用例2: [1, 2, 3, 4] 应返回 5
        int[] arr2 = {1, 2, 3, 4};
        System.out.println("测试用例2: [1, 2, 3, 4], 期望结果: 5, 实际结果: " + solution.minNum(arr2));
        
        // 测试用例3: [3, 4, -1, 1] 应返回 2
        int[] arr3 = {3, 4, -1, 1};
        System.out.println("测试用例3: [3, 4, -1, 1], 期望结果: 2, 实际结果: " + solution.minNum(arr3));
        
        // 测试用例4: [7, 8, 9, 11, 12] 应返回 1
        int[] arr4 = {7, 8, 9, 11, 12};
        System.out.println("测试用例4: [7, 8, 9, 11, 12], 期望结果: 1, 实际结果: " + solution.minNum(arr4));
    }
}
