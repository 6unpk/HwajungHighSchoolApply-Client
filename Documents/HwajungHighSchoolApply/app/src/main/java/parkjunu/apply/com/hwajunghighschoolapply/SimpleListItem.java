package parkjunu.apply.com.hwajunghighschoolapply;

/**
 * Created by pkjoh on 2017-05-10.
 */

public class SimpleListItem {
    private String column1, link;

    public SimpleListItem(String column1, String link){
        this.column1 = column1;
        this.link = link;
    }

    public String getColumn1(){
        return column1;
    }

    public String getLink(){return link;}
}
