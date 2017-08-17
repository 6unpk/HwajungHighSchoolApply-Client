package parkjunu.apply.com.hwajunghighschoolapply;

import android.graphics.drawable.Drawable;
import android.view.View;


public class CardProfileItem {
    private Drawable drawable;
    private String sub;
    private View.OnClickListener listener;

    CardProfileItem(Drawable image, String sub, View.OnClickListener listener){
        drawable = image;
        this.sub =sub;
        this.listener = listener;
    }

    public Drawable getGrade() {
        return drawable;
    }
    public String getSub() {
        return sub;
    }
    public View.OnClickListener getListener() { return listener;}

}