package parkjunu.apply.com.hwajunghighschoolapply;


public class ListItem {
    private String column1, column2, column3, column4, link;

    public ListItem(String column1, String column2, String column3, String column4, String link){
        this.column1 = column1;
        this.column2 = column2;
        this.column3 = column3;
        this.column4 = column4;
        this.link = link;
    }

    public String getColumn1(){
        return column1;
    }

    public String getColumn2(){
        return column2;
    }

    public String getColumn3(){
        return column3;
    }

    public String getColumn4(){
        return column4;
    }

    public String getLink(){return link;}
}
