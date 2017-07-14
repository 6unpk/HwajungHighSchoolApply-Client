package parkjunu.apply.com.hwajunghighschoolapply;

import android.graphics.drawable.Drawable;


public class CardViewItem {
    private Drawable drawable;
    private String sub;
    private int color;

    CardViewItem(Drawable image, String sub, int color){
        drawable = image;
        this.sub =sub;
        this.color = color;
    }

    public Drawable getGrade() {
        return drawable;
    }
    public String getSub() {
        return sub;
    }
    public int getColor() {return color;}


}