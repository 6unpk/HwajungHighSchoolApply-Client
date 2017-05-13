package parkjunu.apply.com.hwajunghighschoolapply;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Entity;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import com.cengalabs.flatui.views.FlatEditText;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {

    static final String HOST_ADDRESS = "http://122.37.102.82:5000";
    static final String HOST_GET_NAME_ADDRESS ="http://122.37.102.82:5000/get_name";
    FlatEditText User_ID;
    FlatEditText Password;
    Button submitButton;
    Button pwFindButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // startActivity(new Intent(getApplicationContext(), Splash.class));
        startActivity(new Intent(getApplicationContext(), ApplyTable.class));

        User_ID = (FlatEditText) findViewById(R.id.user_id);
        Password = (FlatEditText) findViewById(R.id.password);
        submitButton = (Button) findViewById(R.id.submit);
        pwFindButton = (Button) findViewById(R.id.password_find);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkConnection()) {
                    Toast.makeText(getApplicationContext(), "Check the Network", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!User_ID.getText().toString().equals("") && !Password.getText().toString().equals("")) {
                    // 로그인 실행
                        new SendPost(User_ID.getText().toString(), Password.getText().toString()).execute();
                        User_ID.setText("");
                        Password.setText("");
                } else
                    Toast.makeText(getApplicationContext(), "아이디와 패스워드를 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        pwFindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://hwajung.schm.co.kr/find_password.php"));
                startActivity(intent);
            }
        });


    }

    private class SendPost extends AsyncTask<Void, Void, Void>{
        private String id, password;
        private String response;
        URL url;
        ProgressDialog dialog = new ProgressDialog(LoginActivity.this);

        public SendPost(String id, String password){
            this.id = id;
            this.password = password;
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("로그인 중입니다.");
            dialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                url = new URL(HOST_ADDRESS);
                HttpClient httpClient = new DefaultHttpClient();
                // HTTP 커넥션 TIMEOUT: 15초 제한
                httpClient.getParams().setParameter("http.socket.timeout",15000);
                httpClient.getParams().setParameter("http.connection.timeout",150000);
                HttpPost httpPost = new HttpPost();
                httpPost.setURI(url.toURI());

                ArrayList<NameValuePair> post = new ArrayList<>(2);
                post.add(new BasicNameValuePair("id", id));
                post.add(new BasicNameValuePair("pw", password));

                httpPost.setEntity(new UrlEncodedFormEntity(post));

                HttpResponse httpResponse = httpClient.execute(httpPost);
                //여기서 response 는 서버로 부터 받는 로그인 성공 여부 값
                response = EntityUtils.toString(httpResponse.getEntity(), HTTP.UTF_8);
                String args[] = response.split(":");


                // arg[0] 은 로그인 결과, arg[1]은 할당받은 driver 번호
                if(args[0].equals("Connected")){
                    // 로그인 성공시
                    url = new URL(HOST_GET_NAME_ADDRESS);
                    httpClient = new DefaultHttpClient();
                    httpPost = new HttpPost();
                    httpPost.setURI(url.toURI());

                    post.clear();
                    post.add(new BasicNameValuePair("driver_num", args[1]));
                    httpPost.setEntity(new UrlEncodedFormEntity(post));

                    httpResponse = httpClient.execute(httpPost);
                    response = EntityUtils.toString(httpResponse.getEntity(), HTTP.UTF_8);

                    dialog.cancel();
                    dialog.dismiss();

                    // 여기서 response 는 서버로 부터 받는 유저 이름값
                    startActivity(new Intent(getApplicationContext(), MainSelection.class).putExtra("user_name",response).putExtra("driver_num", args[1]));
                }
                else{
                    // 로그인 실패시
                    dialog.cancel();
                    dialog.dismiss();
                    Toast fail = new Toast(getApplicationContext());
                    fail.setText("로그인에 실패했습니다.");
                    fail.setDuration(Toast.LENGTH_LONG);
                    fail.setGravity(Gravity.CENTER, 0, 300);
                    fail.show();
                }

            }catch (Exception e){
                e.printStackTrace();
                Log.e("error",""+e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

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
