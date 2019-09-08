package src;

/**
 * Created by dewet on 7/25/17.
 */
public class pattern {

    private String patternStr;
    private int count;

    public pattern(){
        this.count=0;


    }

    public String getPattern() {
        return patternStr;
    }

    public void setPattern(String pattern) {
        this.patternStr = pattern;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String toString(){

        return  "["+patternStr + " - " +count+"]";

    }
}
