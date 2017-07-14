package parkjunu.apply.com.hwajunghighschoolapply;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.ShowableListMenu;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

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

import java.net.URL;
import java.util.ArrayList;

public class AfterApplyTable extends AppCompatActivity {

    static final String HOST_ADDRESS_SUBMIT ="http://45.32.52.41:5000/submit";

    TableLayout.LayoutParams tableParams;
    TableRow.LayoutParams rowParams;
    TableLayout table;
    static String driverNum;
    String source;
    Button sendChecked;

    // 현재 체크 박스 저장 값
    ArrayList<CheckboxValue> checkBoxes = new ArrayList<>();

    // 초기 체크 박스 저장 값
    ArrayList<Boolean> firstValue = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_apply_table);

        driverNum = getIntent().getExtras().getString("driver_num");
        source = getIntent().getExtras().getString("source");
        table = (TableLayout)findViewById(R.id.table);
        // 각 레이아웃의 width 와 height 값을 Param 값에 담아 설정
        tableParams = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        // tablelayout 을 linearlayout 처럼 사용
        ConvertSource(source);
        sendChecked = (Button)findViewById(R.id.submit);
        sendChecked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject obj = new JSONObject();
                    int i = 0;
                    for (CheckboxValue check: checkBoxes){
                        if(check.checkBox.isChecked())
                            obj.put("" + i, true);
                        else
                            obj.put(""+ i, false);
                        ++i;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Log.e("error",""+e);
                }

                if (NetworkConnection())
                    new SendValue().execute();


            }
        });
    }


    public class SendValue extends AsyncTask<Void, Void, Void> {
        JSONObject jsonObject = new JSONObject();
        ProgressDialog dialog;
        URL url;
        String response;

        public SendValue(){
            dialog = new ProgressDialog(AfterApplyTable.this);
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
                        // switch 는 초기엔 체크박스가 True 값이였으나 False 로 바뀌었다면 -> true
                        // 그 이외의 경우 - > false
                        obj.put("switch", false);
                        jsonObject.put(""+count,obj);
                    }
                    else{
                        obj.put("value", false);
                        obj.put("num", checkBox.value);

                        // 처음에는 True 였으나 False 로 바뀐 경우
                        if(firstValue.get(count-1))
                            obj.put("switch", true);
                        else
                            obj.put("switch", false);

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
                post.add(new BasicNameValuePair("driver_num", driverNum));
                post.add(new BasicNameValuePair("values",jsonObject.toString()));
                // Type 2 는 방과후 수강 신청 타입
                post.add(new BasicNameValuePair("type","2"));

                httpPost.setEntity(new UrlEncodedFormEntity(post));

                HttpResponse httpResponse = httpClient.execute(httpPost);
                response = EntityUtils.toString(httpResponse.getEntity(), HTTP.UTF_8);

            }catch (Exception e){
                e.printStackTrace();
                Log.e("error", ""+e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            dialog.dismiss();
            if(response.equals("Error"))
                Toast.makeText(getApplicationContext(), "저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getApplicationContext(), "저장에 성공했습니다.", Toast.LENGTH_SHORT).show();


            super.onPostExecute(aVoid);
        }
    }


    public void ConvertSource(String source) {
        try{
            JSONObject jsonObject = new JSONObject(source);
            ArrayList<JSONObject> row = new ArrayList<>();

            for(int i = 0; i < jsonObject.length(); ++i){
                JSONObject obj = jsonObject.getJSONObject(""+(i+1));
                row.clear();
                for(int j = 0; j < obj.length(); ++j)
                    row.add(obj.getJSONObject(""+(j+1)));
                CreateRow(row);

            }
        }catch (Exception e){
            e.printStackTrace();
            Log.e("error", ""+e);
        }

    }

    public class CheckboxValue{
        CheckBox checkBox;
        String value;

        CheckboxValue(CheckBox box, String v){
            checkBox = box;
            value = v;
        }

    }

    public void CreateRow(ArrayList<JSONObject> row){
        TableRow tableRow = new TableRow(this);
        tableRow.setLayoutParams(tableParams);
        tableRow.setDividerPadding(1);

        try {
            for (JSONObject obj : row) {
                TextView column1 = new TextView(this);
                CheckBox checkBox1 = new CheckBox(this);
                checkBoxes.add(new CheckboxValue(checkBox1, obj.getString("check_box_value")));

                column1.setTextSize(18);
                column1.setText(obj.getString("subject"));
                column1.setLayoutParams(rowParams);
                checkBox1.setLayoutParams(rowParams);

                tableRow.addView(column1);
                if (obj.getBoolean("is_checked")) {
                    checkBox1.setChecked(true);
                    firstValue.add(true);
                }else
                    firstValue.add(false);
                if(obj.getBoolean("check_box"))
                    tableRow.addView(checkBox1);
            }
            table.addView(tableRow);
        }
        catch (Exception e){
            e.printStackTrace();
            Log.e("error","");
        }

    }

    public boolean NetworkConnection() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean isMobileAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isAvailable();
        boolean isWifiAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isAvailable();

        boolean isMobileConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected();
        boolean isWifiConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();

        if ((isMobileAvailable && isMobileConnect) || (isWifiAvailable && isWifiConnect))
            return true;
        return false;

    }
}
