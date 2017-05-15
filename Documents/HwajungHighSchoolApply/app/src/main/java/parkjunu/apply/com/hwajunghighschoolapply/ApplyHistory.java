package parkjunu.apply.com.hwajunghighschoolapply;

import android.app.ProgressDialog;
import android.content.Intent;
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

public class ApplyHistory extends AppCompatActivity {
    ListView listView;
    ListViewAdapter listViewAdapter;
    ArrayList<SimpleListItem> items = new ArrayList<>();
    static final String HOST_ADDRESS_APPLY_HISTORY = "http://122.37.102.82:5000/apply_history";
    static final String HOST_ADDRESS_APPLY_HISTORY_TABLE = "http://122.37.102.82:5000/apply_history_table";
    static int driverNum;
    String source;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_history);

        listView = (ListView) findViewById(R.id.list_view_apply_history);
        listViewAdapter = new ListViewAdapter(ApplyHistory.this, items);
        listView.setAdapter(listViewAdapter);

        driverNum = getIntent().getExtras().getInt("driver_num");
        items.add(new SimpleListItem("제목",null));
        RequestApplyHistory request = new RequestApplyHistory();
        request.execute();
        listViewAdapter.notifyDataSetChanged();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(((SimpleListItem)parent.getAdapter().getItem(position)).getLink() == null)
                    return;
                new GetTable(((SimpleListItem)parent.getAdapter().getItem(position)).getLink()).execute();
                Intent intent = new Intent(getApplicationContext(), ApplyHistoryTable.class);
                intent.putExtra("driver_num",driverNum);
                intent.putExtra("source",source);
                startActivity(intent);
            }
        });

    }

    private class GetTable extends AsyncTask<Void, Void, Void>{
        URL url;
        String link;
        ProgressDialog progressDialog;

        public GetTable(String select){
            progressDialog = new ProgressDialog(getApplicationContext());
            link = select;
            progressDialog.setMessage("수강신청 정보를 가져오는중 입니다.");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                url = new URL(HOST_ADDRESS_APPLY_HISTORY_TABLE);
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost();
                httpPost.setURI(url.toURI());

                ArrayList<NameValuePair> post = new ArrayList<>();
                post.add(new BasicNameValuePair("driver_num", ""+driverNum));
                post.add(new BasicNameValuePair("url",link));

                httpPost.setEntity(new UrlEncodedFormEntity(post));

                HttpResponse response = httpClient.execute(httpPost);
                source = EntityUtils.toString(response.getEntity());

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
            progressDialog.dismiss();
            super.onPostExecute(aVoid);
        }

    }


    private class RequestApplyHistory extends AsyncTask<Void, Void, Void>{
        URL url;
        String response;
        ProgressDialog dialog = new ProgressDialog(ApplyHistory.this);

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
                Log.d("tag", "fuck");
                url = new URL(HOST_ADDRESS_APPLY_HISTORY);
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost();
                httpPost.setURI(url.toURI());

                ArrayList<NameValuePair> post = new ArrayList<>();
                post.add(new BasicNameValuePair("driver_num",Integer.toString(driverNum)));

                httpPost.setEntity(new UrlEncodedFormEntity(post));

                HttpResponse httpResponse = httpClient.execute(httpPost);
                response = EntityUtils.toString(httpResponse.getEntity(),HTTP.UTF_8);
                Log.d("tag", ""+response);
                JSONObject jsonObject =new JSONObject(response);
                for(int i = 0; i < jsonObject.length(); ++i){
                    String link = jsonObject.getJSONObject(""+i).getString("link");
                    String title = jsonObject.getJSONObject(""+i).getString("title");

                    items.add(new SimpleListItem(title, link));
                }
                listViewAdapter.notifyDataSetChanged();

            }catch (Exception e){
                e.printStackTrace();
                Log.e("error",""+e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
        }
    }



}
