package parkjunu.apply.com.hwajunghighschoolapply;

import android.graphics.drawable.Drawable;


public class CardProfileItem {
    private Drawable drawable;
    private String sub;

    CardProfileItem(Drawable image, String sub){
        drawable = image;
        this.sub =sub;
    }

    public Drawable getGrade() {
        return drawable;
    }
    public String getSub() {
        return sub;
    }


}