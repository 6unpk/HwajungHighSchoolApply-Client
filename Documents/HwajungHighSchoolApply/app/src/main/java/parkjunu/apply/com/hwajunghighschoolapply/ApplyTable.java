package parkjunu.apply.com.hwajunghighschoolapply;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Text;

import java.net.URL;
import java.util.ArrayList;

public class ApplyTable extends AppCompatActivity {

    static final String HOST_ADDRESS_SUBMIT ="http://45.77.31.234:5000/submit";

    TableLayout.LayoutParams tableParams;
    TableRow.LayoutParams rowParams;
    TableLayout tableRight;
    TableLayout tableLeft;
    static int driverNum;
    String source;
    Button sendChecked;
    ArrayList<CheckboxValue> checkBoxes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_table);
        driverNum = getIntent().getExtras().getInt("driver_num");
        source = getIntent().getExtras().getString("source");
        tableRight = (TableLayout)findViewById(R.id.table);
        // 각 레이아웃의 width 와 height 값을 Param 값에 담아 설정
        tableParams = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        // tablelayout 을 linearlayout 처럼 사용
        ConvertSource(getIntent().getExtras().getString("source"));
        sendChecked = (Button)findViewById(R.id.submit);

        sendChecked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SendValue().execute();
            }
        });
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

    public class CheckboxValue{
        public CheckBox checkBox;
        public int value;

        public CheckboxValue(CheckBox box, int v){
            checkBox = box;
            value = v;
        }

    }


    public void ConvertSource(String source) {
        try {
            JSONObject jsonObject = new JSONObject(source);
            ArrayList<JSONObject> first = new ArrayList<>();
            ArrayList<JSONObject> second = new ArrayList<>();
            int len;

            String firstColumn = jsonObject.getString("first_column");
            String secondColumn = jsonObject.getString("second_column");

            // 어느 행에 속하는지 분류
            for(int i = 0; i < jsonObject.length(); ++i){
                JSONObject obj = jsonObject.getJSONObject(""+(i+1));
                if(obj.getString("column").equals(firstColumn))
                    first.add(obj);
                else
                    second.add(obj);
            }
            // 두개의 행 가운데 요소값이 더 적은 행의 요소 개수를 가져옴
            len = first.size() > second.size() ? second.size() : first.size();

            for (int i = 0; i < len; ++i ){
                String text1 = first.get(i).getString("title");
                String text2 = first.get(i).getString("title");
                Boolean check1 = first.get(i).getBoolean("check_box");
                Boolean check2 = first.get(i).getBoolean("check_box");
                String value1 = first.get(i).getString("value");
                String value2 = first.get(i).getString("value");
                CreateRow(text1, text2, check1, check2, Integer.parseInt(value1), Integer.parseInt(value2));

            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public class SendValue extends AsyncTask<Void, Void, Void> {
        JSONObject jsonObject = new JSONObject();
        ProgressDialog dialog;
        URL url;

        public SendValue(){
            dialog = new ProgressDialog(ApplyTable.this);
            dialog.setMessage("저장 중입니다.");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            dialog.show();
            try {
                int count = 1;
                for (CheckboxValue checkBox : checkBoxes) {
                    JSONObject obj = new JSONObject();
                    if (checkBox.checkBox.isChecked()) {
                        obj.put("value", true);
                        obj.put("num", checkBox.value);
                        jsonObject.put(""+count,obj);
                    }
                    else{
                        obj.put("value", false);
                        obj.put("num", checkBox.value);
                        jsonObject.put(""+count,obj);
                    }
                    count++;
                }
            }catch (Exception e){
                e.printStackTrace();
                Log.e("error", e+"");
            }
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try{
                url = new URL(HOST_ADDRESS_SUBMIT);
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost();
                httpPost.setURI(url.toURI());

                ArrayList<NameValuePair> post = new ArrayList<>();
                post.add(new BasicNameValuePair("values",jsonObject.toString()));

                httpPost.setEntity(new UrlEncodedFormEntity(post));

                HttpResponse httpResponse = httpClient.execute(httpPost);
                String response = EntityUtils.toString(httpResponse.getEntity(), HTTP.UTF_8);

                if(response.equals("Error")){

                }

            }catch (Exception e){
                e.printStackTrace();
                Log.e("error", ""+e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            dialog.dismiss();
            super.onPostExecute(aVoid);
        }
    }


    public void CreateRow(String text1, String text2, Boolean isCheckExist1, Boolean isCheckExist2, int value1, int value2){
        TableRow tableRow = new TableRow(ApplyTable.this);
        tableRow.setLayoutParams(tableParams);
        tableRow.setBackgroundColor(getResources().getColor(R.color.white));
        tableRow.setDividerPadding(1);

        TextView column1 = new TextView(ApplyTable.this);
        TextView column2 = new TextView(ApplyTable.this);
        CheckBox checkBox1 = new CheckBox(ApplyTable.this);
        CheckBox checkBox2 = new CheckBox(ApplyTable.this);
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

        if(!isCheckExist1)
            checkBox1.setVisibility(View.INVISIBLE);
        if(!isCheckExist2)
            checkBox2.setVisibility(View.INVISIBLE);

        tableRow.addView(column1);
        tableRow.addView(checkBox1);
        tableRow.addView(column2);
        tableRow.addView(checkBox2);
        tableRight.addView(tableRow);

    }

 }
