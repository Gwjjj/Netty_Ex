/**
 * @author: guangxush
 * @create: 2020/02/11
 */
public class Main {
    public static void main(String[] args) {
        int [] a = {1,0,1,0,1,0,1,0,0,1,1,1,1};
        int pre = 0;
        int count = 0;
        int max = 0;
        int length = a.length;
        for (int i = 0; i < length; i++) {
            int now = a[i];
            if(now == 1){
                count++;
                pre++;
            }
            else{
                if(count > max)
                    max = count;
                count = pre;
                pre = 0;
            }
        }
        if(count > max)
            max = count;
        System.out.println(max);
    }
}

