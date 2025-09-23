package com.moon.resume.didi;

import java.util.Arrays;

public class Solution {

    public static void main(String[] args) {
        Solution s = new Solution();
        int[] arr1 = {6};
        int[] arr2 = {4};
        int[] arr3 = {3,2,1};
        int[] ans = s.descArray(arr1, arr2, arr3);
        System.out.println(Arrays.toString(ans));
    }

    public int[] descArray(int[] arr1, int[] arr2, int[] arr3) {

        int n1 = arr1.length;
        int n2 = arr2.length;
        int n3 = arr3.length;
        int n = n1+n2+n3;
        int[] ans = new int[n];
        int index1 = 0, index2 = 0, index3 = 0;
        int i = 0;
        while(i < n) {
            //TODO index of bound judge
            int num1 = index1 < n1 ? arr1[index1] : -1;
            int num2 = index2 < n2 ? arr2[index2] : -1;
            int num3 = index3 < n3 ? arr3[index3] : -1;
            if (num1 >= num2 && num1 >= num3) {
                ans[i] = num1;
                index1++;
            } else if(num2 >= num1 && num2 >= num3) {
                ans[i] = num2;
                index2++;
            } else if(num3 >= num1 && num3 >= num2) {
                ans[i] = num3;
                index3++;
            }
            i++;
        }
        return ans;
    }
}
