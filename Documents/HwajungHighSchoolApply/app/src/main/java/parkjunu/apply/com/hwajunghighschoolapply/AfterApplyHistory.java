package parkjunu.apply.com.hwajunghighschoolapply;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

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

public class AfterApplyHistory extends AppCompatActivity {
    ListView listView;
    ListViewAdapter listViewAdapter;
    ArrayList<SimpleListItem> items = new ArrayList<>();
    static final String HOST_ADDRESS_AFTER_APPLY_HISTORY = "http://45.32.52.41:5000/class_apply_history";
    static final String HOST_ADDRESS_AFTER_APPLY_HISTORY_TABLE = "http://45.32.52.41:5000/class_apply_history_table";
    static String driverNum;
    String source;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_apply_history);
        listView = (ListView) findViewById(R.id.list_view_apply_history);
        listViewAdapter = new ListViewAdapter(AfterApplyHistory.this, items);
        listView.setAdapter(listViewAdapter);

        driverNum = getIntent().getExtras().getString("driver_num");
        items.add(new SimpleListItem("대상  |  제목",null));
        RequestApplyHistory request = new RequestApplyHistory();
        request.execute();
        listViewAdapter.notifyDataSetChanged();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(((SimpleListItem)parent.getAdapter().getItem(position)).getLink() == null || !NetworkConnection())
                    return;
                new GetTable(((SimpleListItem)parent.getAdapter().getItem(position)).getLink()).execute();
            }
        });
    }

    private class GetTable extends AsyncTask<Void, Void, Void> {
        URL url;
        String link;
        ProgressDialog dialog;

        public GetTable(String select){
            dialog = new ProgressDialog(AfterApplyHistory.this);
            link = select;
            dialog.setMessage("수강신청 정보를 가져오는중 입니다.");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                url = new URL(HOST_ADDRESS_AFTER_APPLY_HISTORY_TABLE);
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost();
                httpPost.setURI(url.toURI());

                ArrayList<NameValuePair> post = new ArrayList<>();
                post.add(new BasicNameValuePair("driver_num", driverNum));
                post.add(new BasicNameValuePair("url",link));

                httpPost.setEntity(new UrlEncodedFormEntity(post));

                HttpResponse response = httpClient.execute(httpPost);
                source = EntityUtils.toString(response.getEntity());
                Log.d("tag",""+source);
                if(source.equals("Error"))
                    source = null;

            }catch (Exception e){
                e.printStackTrace();
                Log.e("error",""+e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            dialog.dismiss();
            Intent intent = new Intent(getApplicationContext(), AfterApplyHistoryTable.class);
            intent.putExtra("driver_num",driverNum);
            intent.putExtra("source",source);
            startActivity(intent);

            super.onPostExecute(aVoid);
        }

    }


    private class RequestApplyHistory extends AsyncTask<Void, Void, Void>{
        URL url;
        String response;
        ProgressDialog dialog = new ProgressDialog(AfterApplyHistory.this);

        public RequestApplyHistory(){

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("수강 신청 목록을 가져오는 중입니다.");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                url = new URL(HOST_ADDRESS_AFTER_APPLY_HISTORY);
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost();
                httpPost.setURI(url.toURI());

                ArrayList<NameValuePair> post = new ArrayList<>();
                post.add(new BasicNameValuePair("driver_num",driverNum));

                httpPost.setEntity(new UrlEncodedFormEntity(post));

                HttpResponse httpResponse = httpClient.execute(httpPost);
                response = EntityUtils.toString(httpResponse.getEntity(), HTTP.UTF_8);
                Log.d("tag", ""+response);


            }catch (Exception e){
                e.printStackTrace();
                Log.e("error",""+e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                JSONObject jsonObject =new JSONObject(response);
                for(int i = 0; i < jsonObject.length(); ++i){
                    String link = jsonObject.getJSONObject(""+(i+1)).getString("link");
                    String term = jsonObject.getJSONObject(""+(i+1)).getString("term");
                    String target = jsonObject.getJSONObject(""+(i+1)).getString("target");
                    items.add(new SimpleListItem(target+" "+term, link));

                }
                listViewAdapter.notifyDataSetChanged();
            }catch (Exception e){
                e.printStackTrace();
            }
            dialog.dismiss();
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
