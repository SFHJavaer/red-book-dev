package com;

//谁说递归一定是while，也可以是函数嵌套调用
public class Solution {
    int count = 0;
    public int InversePairs(int [] array) {
        
        int left = 0;
        int right = array.length - 1;
        mergeSort(array,left,right);
        count %= 1000000007;
        return count;
    }
    //归算法(排序需不需要返回值，直接对定义好的变量加值进行统计)
    public void mergeSort(int [] array,int left,int right){
//         int left = 0;
//         int right = array.length - 1;
        int mid = left +(right - left >> 1);
        //方法嵌套循环终止的条件就是方法在执行到某一层时不再执行
        if(left < right){
        mergeSort(array,left,mid);
        mergeSort(array,mid+1,right);
        mergeSort(array,left,right,mid);
        }
    }
    //并算法
    public void mergeSort(int [] array,int left,int right,int mid){
        int l = left;
        int r = mid+1;
        while(l <= mid && r<=right){
            if(array[l] < array[r]){
                l++;
            }else{
                count += (mid +1 -l);//??????????
                r++;
            }
            //仅统计个数，当需要得到并好的数组时才需要把剩余的导入
//             while(l <= mid){
//                 arr[c++];
//             }
        }
       
        

    }

    public static void main(String[] args) {
        int [] arr = {2,0,4,5,6,1};
        Solution solution = new Solution();
        int i = solution.InversePairs(arr);
        System.out.println(i);
    }
}