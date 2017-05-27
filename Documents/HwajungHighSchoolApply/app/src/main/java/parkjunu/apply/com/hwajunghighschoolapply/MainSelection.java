package parkjunu.apply.com.hwajunghighschoolapply;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainSelection extends AppCompatActivity {
    static String name;
    static String driverNum;
    Boolean isLogOutFinished = false;
    TextView userName;
    Button logOut;
    Button infoEdit;

    GridView gridView;
    GridAdapter gridAdapter;
    List<String> gridTitle;
    List<Drawable> gridImage;
    ProgressDialog dialog;

    // TODO: 2017-05-07 HTTP 통신 전에 반드시 네트워크 체크 과정 넣기 
    // TODO: 2017-05-07 서버와 통신이 안되는 경우 Connection Time Out 설정 하기 
    // TODO: 2017-05-13      

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_selection);
        userName = (TextView)findViewById(R.id.user_name);
        logOut = (Button)findViewById(R.id.logout);
        infoEdit = (Button)findViewById(R.id.info_edit);
        name = "이름:"+getIntent().getExtras().getString("user_name");
        driverNum = getIntent().getExtras().getString("driver_num");
        dialog = new ProgressDialog(MainSelection.this);
        userName.setText(name);

        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    while (new LogOut().execute().get()) {
                        dialog.dismiss();
                        finish();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Log.e("error",""+e);
                }
            }
        });

        gridTitle = new ArrayList<>();
        gridImage = new ArrayList<>();
        gridTitle.add("수강신청");
        gridTitle.add("방과후 수강신청");
        gridTitle.add("수강신청 내역");
        gridTitle.add("방과후 수강신청 내역");
        gridTitle.add("공지사항");
        gridTitle.add("설정");

        gridImage.add(getResources().getDrawable(R.drawable.apply));
        gridImage.add(getResources().getDrawable(R.drawable.apply));
        gridImage.add(getResources().getDrawable(R.drawable.apply_history));
        gridImage.add(getResources().getDrawable(R.drawable.apply_history));
        gridImage.add(getResources().getDrawable(R.drawable.notice));
        gridImage.add(getResources().getDrawable(R.drawable.setting));


        gridView = (GridView)findViewById(R.id.grid);
        gridAdapter = new GridAdapter(this);
        gridView.setAdapter(gridAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!NetworkConnection()){
                    Toast.makeText(getApplicationContext(), "인터넷 연결을 확인해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                switch (position){
                    case 0:
                        // 수강신청
                        Intent apply = new Intent(getApplicationContext(), Apply.class);
                        apply.putExtra("driver_num", driverNum);
                        startActivity(apply);
                        break;
                    case 1:
                        // 방과후 수강신청
                        Intent afterApply = new Intent(getApplicationContext(), AfterApply.class);
                        afterApply.putExtra("driver_num", driverNum);
                        startActivity(afterApply);
                        break;
                    case 2:
                        // 수강신청 내역
                        Intent applyHis = new Intent(getApplicationContext(), ApplyHistory.class);
                        applyHis.putExtra("driver_num", driverNum);
                        startActivity(applyHis);
                        break;
                    case 3:
                        // 방과후 수강신청 내역
                        Intent afterApplyHis = new Intent(getApplicationContext(), AfterApplyHistory.class);
                        afterApplyHis.putExtra("driver_num", driverNum);
                        startActivity(afterApplyHis);
                        break;
                    case 4:
                        // 공지사항
                        break;
                    case 5:
                        // 설정
                        Intent setting = new Intent(getApplicationContext(), SettingActivity.class);
                        startActivity(setting);
                        break;
                    default:
                        break;
                }
            }
        });

    }

    private class LogOut extends AsyncTask<Boolean, Void, Boolean>{
        final String HOST_LOGOUT_ADDRESS ="http://45.32.52.41:5000/logout";
        URL url;

        public LogOut(){
            dialog.setMessage("로그 아웃 중입니다.");
            dialog.setCancelable(false);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(Boolean... booleen ){
            try {
                url = new URL(HOST_LOGOUT_ADDRESS);
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost();
                httpPost.setURI(url.toURI());

                ArrayList<NameValuePair> post = new ArrayList<>(1);
                post.add(new BasicNameValuePair("driver_num", driverNum));

                httpPost.setEntity(new UrlEncodedFormEntity(post));

                HttpResponse httpResponse = httpClient.execute(httpPost);
                String response = EntityUtils.toString(httpResponse.getEntity(), HTTP.UTF_8);
                if(response.equals("Logout")) {
                    isLogOutFinished = true;
                    return true;
                }

            }catch (Exception e){
                e.printStackTrace();
                Log.e("error", "" +e );
                dialog.dismiss();
                return false;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aVoid) {
            Toast.makeText(getApplicationContext(), "로그 아웃 되었습니다.", Toast.LENGTH_LONG).show();
            super.onPostExecute(aVoid);
        }
    }

    @Override
    public void onBackPressed() {
        // 뒤로 가기 눌렀을시
        super.onBackPressed();
        new AlertDialog.Builder(MainSelection.this)
                .setTitle("로그아웃")
                .setMessage("로그아웃 하시겠습니까?")
                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                })
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            while (new LogOut().execute().get()) {
                                dialog.dismiss();
                                finish();
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                            Log.e("error",""+e);
                        }
                    }
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        // 종료 되기 직전에
        new LogOut().execute();
        super.onDestroy();
    }

    public class GridAdapter extends BaseAdapter {
        LayoutInflater inflater;

        public GridAdapter(Context context){
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if(convertView == null)
                convertView = inflater.inflate(R.layout.grid_item, parent, false);

            ImageView imageView = (ImageView)convertView.findViewById(R.id.grid_image);
            TextView textView = (TextView)convertView.findViewById(R.id.grid_text);
            imageView.setImageDrawable(gridImage.get(position));
            textView.setText(gridTitle.get(position));

            imageView.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!NetworkConnection()){
                        Toast.makeText(getApplicationContext(), "인터넷 연결을 확인해주세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    switch (position){
                        case 0:
                            // 수강신청
                            Intent apply = new Intent(getApplicationContext(), Apply.class);
                            apply.putExtra("driver_num", driverNum);
                            startActivity(apply);
                            break;
                        case 1:
                            // 방과후 수강신청
                            Intent afterApply = new Intent(getApplicationContext(), AfterApply.class);
                            afterApply.putExtra("driver_num", driverNum);
                            startActivity(afterApply);
                            break;
                        case 2:
                            // 수강신청 내역
                            Intent applyHis = new Intent(getApplicationContext(), ApplyHistory.class);
                            applyHis.putExtra("driver_num", driverNum);
                            startActivity(applyHis);
                            break;
                        case 3:
                            // 방과후 수강신청 내역
                            Intent afterApplyHis = new Intent(getApplicationContext(), AfterApplyHistory.class);
                            afterApplyHis.putExtra("driver_num", driverNum);
                            startActivity(afterApplyHis);
                            break;
                        case 4:
                            // 공지사항
                            break;
                        case 5:
                            // 설정
                            Intent setting = new Intent(getApplicationContext(), SettingActivity.class);
                            startActivity(setting);
                            break;
                        default:
                            break;
                    }
                }
            });

            return convertView;
        }

        @Override
        public Object getItem(int position) {
            return gridTitle.get(position);
        }

        @Override
        public int getCount() {
            return gridTitle.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
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
