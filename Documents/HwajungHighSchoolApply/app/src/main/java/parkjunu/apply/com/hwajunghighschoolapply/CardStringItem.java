package parkjunu.apply.com.hwajunghighschoolapply;


public class CardStringItem{
	private String title;
	private String sub;
	private int count;
	private int titleColor;
	private int subColor;

	public CardStringItem(String title, String sub, int color1, int color2){
		this.title = title;
		this.sub = sub;
		this.count = -1;
		titleColor = color1;
		subColor = color2;
	}

	public CardStringItem(String title, String sub, int count ,int color1, int color2){
		this.title = title;
		this.sub = sub;
		this.count = count;
		titleColor = color1;
		subColor = color2;
	}
	public String getTitle(){
		return title;
	}

	public String getSub(){
		return sub;
	}

	public int getCount() {return count;}
}