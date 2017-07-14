package parkjunu.apply.com.hwajunghighschoolapply;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Entity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.IntegerRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cengalabs.flatui.views.FlatEditText;
import com.melnykov.fab.FloatingActionButton;
import com.rengwuxian.materialedittext.MaterialEditText;

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

    static final String HOST_ADDRESS = "http://45.32.52.41:5000";
    static final String HOST_GET_NAME_ADDRESS ="http://45.32.52.41:5000/get_name";

    MaterialEditText User_ID;
    MaterialEditText Password;
    FloatingActionButton actionButton;
    Button submitButton;
    Button pwFindButton;
    CheckBox saveLogin;
    ImageView Hwajung;
    TextView Developer;
    boolean isLoginSuc = false;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        startService(new Intent(getApplicationContext(), SchoolFood.class));
        startActivity(new Intent(getApplicationContext(), Splash.class));
        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = pref.edit();

        // 앱을 처음 설치해서 실행 시킬때
        if( pref.getBoolean("is_first", true)){
            editor.putBoolean("is_first", false);
            editor.commit();
            new AlertDialog.Builder(LoginActivity.this).
                    setTitle("주의 사항").
                    setMessage("본 어플은 PC 버전 수강신청 사이트보다 다소 느릴 수 있습니다.").
                    setCancelable(false).
                    setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            PermissionRequest();
                        }
                    }).show();
        }

        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade);
        animation.setFillAfter(true);

        User_ID = (MaterialEditText) findViewById(R.id.user_id);
        Password = (MaterialEditText) findViewById(R.id.password);
        submitButton = (Button) findViewById(R.id.submit);
        pwFindButton = (Button) findViewById(R.id.password_find);
        saveLogin = (CheckBox) findViewById(R.id.save_login);
        Hwajung = (ImageView) findViewById(R.id.hwajung);
        Developer = (TextView) findViewById(R.id.developer);
        actionButton = (FloatingActionButton) findViewById(R.id.setting);

        Hwajung.startAnimation(animation);
        User_ID.startAnimation(animation);
        Password.startAnimation(animation);
        submitButton.startAnimation(animation);
        pwFindButton.startAnimation(animation);
        saveLogin.startAnimation(animation);
        Developer.startAnimation(animation);
        actionButton.startAnimation(animation);

        // 로그인 정보 저장, 체크 되어 있는 경우
        if(pref.getBoolean("save_login",false)){
            saveLogin.setChecked(true);
            User_ID.setText(pref.getString("id",""));
            Password.setText(pref.getString("pw",""));
        }



        Password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId){
                    case EditorInfo.IME_ACTION_GO:
                        executeLogin();
                        break;
                }
                return true;
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeLogin();
            }
        });

        // 비번 찾기 버튼
        pwFindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://hwajung.schm.co.kr/find_password.php"));
                startActivity(intent);
            }
        });

        // 설정 버튼
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SettingActivity.class));
            }
        });

    }

    private void executeLogin(){
        if (!NetworkConnection()) {
            Toast.makeText(getApplicationContext(), "인터넷 연결을 확인해주세요.", Toast.LENGTH_SHORT).show();
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

    // 로그인 정보 값을 서버로 전송
    private class SendPost extends AsyncTask<Void, Void, Void>{
        private String id, password;
        private String response;
        private String reponse2;
        Boolean isCheck;
        URL url;
        URL url2;
        ProgressDialog dialog = new ProgressDialog(LoginActivity.this);

        public SendPost(String id, String password){
            this.id = id;
            this.password = password;
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("로그인 중입니다.");
            dialog.setCancelable(false);
            isCheck = saveLogin.isChecked();
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
                // HTTP 커넥션 TIMEOUT: 35초 제한
                httpClient.getParams().setParameter("http.socket.timeout",350000);
                httpClient.getParams().setParameter("http.connection.timeout",350000);
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

                    isLoginSuc = true;
                    dialog.cancel();
                    dialog.dismiss();
                    // 여기서 response 는 서버로 부터 받는 유저 이름값
                    startActivity(new Intent(getApplicationContext(), MainSelection.class).putExtra("user_name",response).putExtra("driver_num", args[1]));
                    if (isCheck) {
                        editor.putString("id", id);
                        editor.putString("pw", password);
                        editor.putBoolean("save_login", true);
                        editor.commit();
                    }
                }
                else{
                    // 로그인 실패시
                    dialog.cancel();
                    dialog.dismiss();
                   isLoginSuc = false;
                }

            }catch (Exception e){
                e.printStackTrace();
                Log.e("error",""+e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(!isLoginSuc)
               Toast.makeText(getApplicationContext(), "로그인에 실패했습니다.", Toast.LENGTH_LONG).show();
            isLoginSuc = false;
            super.onPostExecute(aVoid);
        }
    }



    // 초기 앱 실행시 권한 요청
    public void PermissionRequest(){
        int permissionWrite = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissionWrite == PackageManager.PERMISSION_DENIED && Build.VERSION.SDK_INT >= 23){
            new AlertDialog.Builder(LoginActivity.this)
                    .setTitle("권한 설정")
                    .setCancelable(false)
                    .setMessage("급식 알리미를 사용하기 위해서는 권한을 반드시 체크해주세요!")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(Build.VERSION.SDK_INT >= 23) // 부팅 알림 권한, 저장소 쓰기/읽기 권한 요청
                                requestPermissions(new String[]{Manifest.permission.RECEIVE_BOOT_COMPLETED, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
                        }
                    })
                    .show();

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode){
            case 100:
                if(pref.getBoolean("save_login", false)) {
                    User_ID.setText(pref.getString("id", ""));
                    Password.setText(pref.getString("pw", ""));
                }

                break;
            default:

                break;
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
