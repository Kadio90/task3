package stepup;

import stepup.annotations.Cache;
import stepup.annotations.Mutator;

public class Fraction implements Fractionable {
    private int num;
    private int denum;

    public Fraction(int num, int denum) {
        this.num = num;
        this.denum = denum;
    }

    @Mutator
    public void setNum(int num) {
        System.out.println("invoke setNum");
        this.num = num;
    }

    @Mutator
    public void setDenum(int denum) {
        System.out.println("invoke setDenum");
        this.denum = denum;
    }

    @Override
    @Cache(lifeTime = 1000)
    public double doubleValue() {
        System.out.println("invoke double value");
        return (double) num/denum;
    }

    @Override
    @Cache(lifeTime = 0)
    public String toString() {
        return num + "/" + denum;
    }
}
