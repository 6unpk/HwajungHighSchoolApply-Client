package parkjunu.apply.com.hwajunghighschoolapply;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Text;

public class ApplyTable extends AppCompatActivity {

    TableLayout.LayoutParams tableParams;
    TableRow.LayoutParams rowParams;
    TableLayout tableRight;
    TableLayout tableLeft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_table);
        tableRight = (TableLayout)findViewById(R.id.right_table);
        tableLeft = (TableLayout)findViewById(R.id.left_table) ;
        // 각 레이아웃의 width 와 height 값을 Param 값에 담아 설정
        tableParams = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        // tablelayout 을 linearlayout 처럼 사용

        CreateRow("국어","수학");
        //ConvertSource(getIntent().getExtras().getString("source"));
    }
    public void InitTable(String text1, String text2, String text3){
        TableRow rowRight = new TableRow(ApplyTable.this);
        rowRight.setLayoutParams(tableParams);

        TableRow rowLeft = new TableRow(ApplyTable.this);
        rowLeft.setLayoutParams(tableParams);

        TextView column1 = new TextView(ApplyTable.this);
        TextView column2 = new TextView(ApplyTable.this);
        TextView column3 = new TextView(ApplyTable.this);

        column1.setTextSize(16);
        column2.setTextSize(16);
        column3.setTextSize(16);

        column1.setText(text1);
        column2.setText(text2);
        column3.setText(text3);
        column1.setLayoutParams(rowParams);
        column2.setLayoutParams(rowParams);
        column3.setLayoutParams(rowParams);

        rowLeft.addView(column1);
        rowRight.addView(column2);
        rowRight.addView(column3);


    }

    public void ConvertSource(String source){

    }

    public void CreateKind(String text){


    }

    public void CreateRow(String text1, String text2){
        TableRow tableRow = new TableRow(ApplyTable.this);
        tableRow.setLayoutParams(tableParams);

        TextView column1 = new TextView(ApplyTable.this);
        TextView column2 = new TextView(ApplyTable.this);
        CheckBox checkBox1 = new CheckBox(ApplyTable.this);
        CheckBox checkBox2 = new CheckBox(ApplyTable.this);

        column1.setTextSize(16);
        column1.setText(text1);
        column1.setLayoutParams(rowParams);
        checkBox1.setLayoutParams(rowParams);

        column2.setTextSize(16);
        column2.setText(text2);
        column2.setLayoutParams(rowParams);
        checkBox2.setLayoutParams(rowParams);

        tableRow.addView(column1);
        tableRow.addView(checkBox1);
        tableRow.addView(column2);
        tableRow.addView(checkBox2);
        tableRight.addView(tableRow);

    }

 }
