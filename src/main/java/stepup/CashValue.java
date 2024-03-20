package stepup;

import lombok.Getter;

public class CashValue {
    @Getter
    private Object value;
    private boolean deathless = false;
    private long lifeTime;

    CashValue(Object value, int lifeTime){
        setValue(value, lifeTime);
    }

    public void setLifeTime(int interval) {
        if (interval == 0) deathless = true;
        else this.lifeTime = System.currentTimeMillis() + interval;
    }

    public boolean isValid(){
        return deathless || this.lifeTime >= System.currentTimeMillis();
    }

    public void setValue(Object value, int lifeTime){
        this.value = value;
        setLifeTime(lifeTime);
    }
}
