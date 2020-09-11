package halfpackage;

import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()){
            int n = scanner.nextInt();
            int m = scanner.nextInt();
            int waterArray[][] = new int[n][m];
            int max[][] = new int[n][m];
            boolean isGo[][] = new boolean[n][m];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    waterArray[i][j] = scanner.nextInt();
                }
            }
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    isGo[i][j] = true;
                    findMax(1,waterArray,isGo, i, j, n, m);
                    isGo[i][j] = false;
                }
            }
            System.out.println(MAX);
        }
    }

    static int MAX = 0;

    static void findMax(int now,int[][] waterArray,boolean isGo[][],int x ,int y,int n,int m){
        boolean isFinal = false;
        int height = waterArray[x][y];
        if(y<m-1 && !isGo[x][y+1] && waterArray[x][y+1] < height){
            isGo[x][y+1] = true;
            findMax(now+1,waterArray, isGo,x,y+1,n,m);
            isGo[x][y+1] = false;
            isFinal = true;
        }
        if(y>0 && !isGo[x][y-1] && waterArray[x][y-1] < height){
            isGo[x][y-1] = true;
            findMax(now+1,waterArray, isGo,x,y-1,n,m);
            isGo[x][y-1] = false;
            isFinal = true;
        }
        if(x<n-1 && !isGo[x+1][y] && waterArray[x+1][y] < height){
            isGo[x+1][y] = true;
            findMax(now+1,waterArray, isGo,x+1,y,n,m);
            isGo[x+1][y] = false;
            isFinal = true;
        }
        if(x>0 &&!isGo[x-1][y] && waterArray[x-1][y] < height){
            isGo[x-1][y] = true;
            findMax(now+1,waterArray, isGo,x-1,y,n,m);
            isGo[x-1][y] = false;
            isFinal = true;
        }
        if(!isFinal){
            if(now > MAX){
                MAX = now;
            }
        }
    }
}
