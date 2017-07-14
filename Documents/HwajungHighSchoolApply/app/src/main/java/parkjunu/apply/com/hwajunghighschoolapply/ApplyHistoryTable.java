package parkjunu.apply.com.hwajunghighschoolapply;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;

public class ApplyHistoryTable extends AppCompatActivity {
    TableLayout.LayoutParams tableParams;
    TableRow.LayoutParams rowParams;
    TableLayout table;
    ArrayList<CheckboxValue> checkBoxes = new ArrayList<>();


    static String driverNum;
    String source;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_history_table);
        driverNum = getIntent().getExtras().getString("driver_num");
        source = getIntent().getExtras().getString("source");
        table = (TableLayout)findViewById(R.id.table);
        // 각 레이아웃의 width 와 height 값을 Param 값에 담아 설정
        tableParams = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        // tablelayout 을 linearlayout 처럼 사용
        ConvertSource(getIntent().getExtras().getString("source"));

    }

    public class CheckboxValue{
        CheckBox checkBox;
        int value;

        CheckboxValue(CheckBox box, int v){
            checkBox = box;
            value = v;
        }

    }
    public void ConvertSource(String source) {
        try {
            JSONObject jsonObject = new JSONObject(source);
            for (int i = 0; i < jsonObject.length(); ++i){
                JSONObject obj1 = jsonObject.getJSONObject(""+(i+1));
                JSONObject obj2 = null;

                String text1 = obj1.getString("subject");
                Boolean check1 = obj1.getBoolean("check_box");
                Boolean isCheck1 = obj1.getBoolean("is_checked");
                String value1 = obj1.getString("check_box_value");


                String text2 = "";
                Boolean check2 = false;
                Boolean isCheck2 = false;
                String value2 = "";

                if ((++i) < jsonObject.length()) {
                    obj2 = jsonObject.getJSONObject("" + (i + 1));
                    text2 = obj2.getString("subject");
                    check2 = obj2.getBoolean("check_box");
                    isCheck2 = obj2.getBoolean("is_checked");
                    value2 = obj2.getString("check_box_value");

                }

                CreateRow(text1, text2, check1, check2, isCheck1, isCheck2, Integer.parseInt(value1), Integer.parseInt(value2));

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void CreateRow(String text1, String text2, Boolean isCheckExist1, Boolean isCheckExist2, Boolean isCheck1, Boolean isCheck2,int value1, int value2){
        TableRow tableRow = new TableRow(ApplyHistoryTable.this);
        tableRow.setLayoutParams(tableParams);
        tableRow.setDividerPadding(1);

        TextView column1 = new TextView(ApplyHistoryTable.this);
        TextView column2 = new TextView(ApplyHistoryTable.this);
        CheckBox checkBox1 = new CheckBox(ApplyHistoryTable.this);
        CheckBox checkBox2 = new CheckBox(ApplyHistoryTable.this);
        checkBoxes.add(new CheckboxValue(checkBox1, value1));
        checkBoxes.add(new CheckboxValue(checkBox2, value2));

        column1.setTextSize(16);
        column1.setText(text1);
        column1.setLayoutParams(rowParams);
        checkBox1.setLayoutParams(rowParams);

        column2.setTextSize(16);
        column2.setText(text2);
        column2.setLayoutParams(rowParams);
        checkBox2.setLayoutParams(rowParams);

        if(!isCheckExist1) {
            checkBox1.setVisibility(View.INVISIBLE);
            if(isCheck1)
                checkBox1.setChecked(true);
        }
        if(!isCheckExist2) {
            checkBox2.setVisibility(View.INVISIBLE);
            if(isCheck2)
                checkBox2.setChecked(true);
        }
        tableRow.addView(column1);
        tableRow.addView(checkBox1);
        tableRow.addView(column2);
        tableRow.addView(checkBox2);
        table.addView(tableRow);

    }

}
