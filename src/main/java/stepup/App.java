package stepup;

import static java.lang.Thread.sleep;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main(String[] args) throws InterruptedException {
        Fraction fr= new Fraction(2,3);
        Fractionable num = Utils.cache(fr);
        num.setNum(5);
        num.doubleValue();
        num.setNum(6);
        num.doubleValue();
        num.setNum(7);
        num.doubleValue();// sout сработал
        System.out.println(num.toString());
        num.doubleValue();// sout молчит-
        num.doubleValue();// sout молчит
        Thread.sleep(1000);
        num.doubleValue();// sout сработал
        Thread.sleep(5000);
        num.doubleValue();// sout молчит);
        num.setNum(9);
        num.doubleValue();// sout молчит);
    }
}
