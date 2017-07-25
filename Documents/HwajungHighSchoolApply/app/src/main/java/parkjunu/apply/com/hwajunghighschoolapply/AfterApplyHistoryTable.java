package parkjunu.apply.com.hwajunghighschoolapply;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;

public class AfterApplyHistoryTable extends AppCompatActivity {

    TableLayout.LayoutParams tableParams;
    TableRow.LayoutParams rowParams;
    TableLayout table;

    static String driverNum;
    String source;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_apply_history_table);
        driverNum = getIntent().getExtras().getString("driver_num");
        source = getIntent().getExtras().getString("source");
        table = (TableLayout)findViewById(R.id.table);
        // 각 레이아웃의 width 와 height 값을 Param 값에 담아 설정
        tableParams = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        // tablelayout 을 linearlayout 처럼 사용
        ConvertSource(getIntent().getExtras().getString("source"));

    }


    public void ConvertSource(String source){
        try{
            JSONObject jsonObject = new JSONObject(source);
            ArrayList<String> row = new ArrayList<>();
            for(int i = 0; i < jsonObject.length(); ++i){
                JSONObject obj = jsonObject.getJSONObject(""+(i+1));
                row.clear();
                for(int j = 0; j < obj.length(); ++j)
                    row.add(obj.getString(""+(j+1)));
                CreateRow(row);
            }
        }catch (Exception e){
            e.printStackTrace();
            Log.e("error", ""+e);
        }

    }


    public void CreateRow(ArrayList<String> row){
        TableRow tableRow = new TableRow(AfterApplyHistoryTable.this);
        tableRow.setLayoutParams(rowParams);

        for(String str: row){
            TextView column1 = new TextView(AfterApplyHistoryTable.this);
            column1.setTextSize(18);
            column1.setLayoutParams(rowParams);
            column1.setPadding(16, 16, 16, 16);
            column1.setBackground(getResources().getDrawable(R.drawable.border));
            column1.setText(str);
            tableRow.addView(column1);
        }
        table.addView(tableRow);
    }




}
