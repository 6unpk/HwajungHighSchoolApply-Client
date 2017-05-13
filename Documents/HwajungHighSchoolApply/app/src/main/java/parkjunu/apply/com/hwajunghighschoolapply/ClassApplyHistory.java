package parkjunu.apply.com.hwajunghighschoolapply;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

public class ClassApplyHistory extends AppCompatActivity {
    ListView listView;
    ListViewAdapter listViewAdapter;
    ArrayList<ListItem> items = new ArrayList<>();
    static final String HOST_ADDRESS_APPLY_HISTORY = "http://10.24.36.199:5000/class_apply_history";
    static int driverNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_apply_history);

        listView = (ListView) findViewById(R.id.list_view_class_apply_history);
        listViewAdapter = new ListViewAdapter(ClassApplyHistory.this, items);
        listView.setAdapter(listViewAdapter);

        driverNum = getIntent().getExtras().getInt("driver_num");
        items.add(new ListItem("제목","대상","신청 기간","기간",""));
        RequestApplyHistory request = new RequestApplyHistory();
        request.execute();
        listViewAdapter.notifyDataSetChanged();
    }


    private class RequestApplyHistory extends AsyncTask<Void, Void, Void> {
        URL url;
        String response;
        ProgressDialog dialog = new ProgressDialog(ClassApplyHistory.this);

        public RequestApplyHistory(){

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("정보를 가져오는 중입니다...");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                url = new URL(HOST_ADDRESS_APPLY_HISTORY);
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost();
                httpPost.setURI(url.toURI());

                ArrayList<NameValuePair> post = new ArrayList<>(1);
                post.add(new BasicNameValuePair("driver_num",Integer.toString(driverNum)));

                httpPost.setEntity(new UrlEncodedFormEntity(post));

                HttpResponse httpResponse = httpClient.execute(httpPost);
                response = EntityUtils.toString(httpResponse.getEntity(), HTTP.UTF_8);
                Log.d("tag", ""+response);

                JSONObject jsonObject = new JSONObject(response);
                for (int i = 0; i < jsonObject.length(); ++i){
                    String link = jsonObject.getJSONObject(""+(i+1)).getString("link");
                    String title = jsonObject.getJSONObject(""+(i+1)).getString("title");
                    String target = jsonObject.getJSONObject(""+(i+1)).getString("target");
                    String term = jsonObject.getJSONObject(""+(i+1)).getString("term");
                    String apply_term = jsonObject.getJSONObject(""+(i+1)).getString("apply_term");

                    items.add(new ListItem(title,target,apply_term,term,link));
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
