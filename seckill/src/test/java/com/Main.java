package com;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String args[]){
        Scanner sc = new Scanner(System.in);
        List<Integer> list = new ArrayList();
        while(sc.hasNextInt()){
            int i = sc.nextInt();
            if(i != -1){
                list.add(i);

            }else{
                break;
            }

        }
        for (int i = list.size()-1;i>-1;i--) {
            System.out.println(list.get(i));

        }
    }
}
