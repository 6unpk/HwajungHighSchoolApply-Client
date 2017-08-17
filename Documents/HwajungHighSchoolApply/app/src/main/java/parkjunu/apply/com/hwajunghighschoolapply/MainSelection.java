package parkjunu.apply.com.hwajunghighschoolapply;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import android.widget.Button;
import android.widget.LinearLayout;
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
import java.util.Random;

public class MainSelection extends AppCompatActivity {
    static String name;
    static String driverNum;
    static int logOutCount = 0;

    static final String HOST_LOGOUT_ADDRESS ="http://45.32.52.41:5000/logout";
    static final String HOST_GET_APPLY_COUNT ="http://45.32.52.41:5000/get_apply_count";


    int colors[] = new int[5];
    int background[] = new int[7];
    Boolean isLogOutFinished = false;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerAdapter adapter;
    RecyclerAdapter.MyClickListener listener;
    View.OnClickListener logOutListener;
    ArrayList<Object> cardItems = new ArrayList<>();
    TextView title;
    TextView sub;
    int applyCount = 0;
    static int clickCount = 0;
    long startTime = 0;

    Thread logOutThread;
    Thread getCountThread;

    ProgressDialog progressDialog;

    // TODO: 2017-05-07 서버와 통신이 안되는 경우 Connection Time Out 설정 하기

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_selection);

        driverNum = getIntent().getExtras().getString("driver_num");
        Typeface font= Typeface.createFromAsset(getAssets(),"aritta.ttf");
        recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        adapter = new RecyclerAdapter(cardItems, MainSelection.this);
        layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        // 카드뷰 클릭 리스너
        listener = new RecyclerAdapter.MyClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                switch(position){
                    // case0: 프로필
                    case 0:

                        break;
                    // case1: 수강신청
                    case 1:
                        Intent intent1 =new Intent(getApplicationContext(), Apply.class);
                        intent1.putExtra("driver_num", driverNum);
                        startActivity(intent1);
                        break;
                    // case2: 방과후 수강신청
                    case 2:
                        Intent intent2 =new Intent(getApplicationContext(), AfterApply.class);
                        intent2.putExtra("driver_num", driverNum);
                        startActivity(intent2);
                        break;
                    // 현재 진행중인 수강신청의 개수 표시
                    case 3:

                        break;
                    // 현재 앱의 버전 표시
                    case 4:

                        break;
                    // case5: 수강신청 기록
                    case 5:
                        Intent intent3 =new Intent(getApplicationContext(), ApplyHistory.class);
                        intent3.putExtra("driver_num", driverNum);
                        startActivity(intent3);
                        break;
                    // case6: 방과후 수강신청 기록
                    case 6:
                        Intent intent4 =new Intent(getApplicationContext(), AfterApplyHistory.class);
                        intent4.putExtra("driver_num", driverNum);
                        startActivity(intent4);
                        break;
                    // 설정창
                    case 7:
                    Intent intent5 =new Intent(getApplicationContext(), SettingActivity.class);
                        startActivity(intent5);
                        break;

                    default:
                        break;
                }
            }
        };

        adapter.setOnItemClickListener(listener);

        colors[0] = getResources().getColor(R.color.card1);
        colors[1] = getResources().getColor(R.color.card2);
        colors[2] = getResources().getColor(R.color.card3);
        colors[3] = getResources().getColor(R.color.card4);
        colors[4] = getResources().getColor(R.color.card5);
        Random random = new Random();
        for(int i =0; i < 5; ++i)
            background[i] = colors[random.nextInt(5)];
        for(int i =0; i < 4; ++i)
            for(int j =i+1; j < 5; ++j){
                while (background[i] == background[j])
                    background[j] = colors[random.nextInt(5)];
            }

        title = (TextView) findViewById(R.id.title);
        sub = (TextView) findViewById(R.id.sub);
        title.setTypeface(font);
        sub.setTypeface(font);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.fade);
        title.startAnimation(animation);
        sub.startAnimation(animation);

        name = getIntent().getExtras().getString("user_name");
        applyCount = 0;

        getCountThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    URL url;
                    url = new URL(HOST_GET_APPLY_COUNT);
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost();
                    httpPost.setURI(url.toURI());

                    ArrayList<BasicNameValuePair> post = new ArrayList<>();

                    post.add(new BasicNameValuePair("driver_num",driverNum));

                    httpPost.setEntity(new UrlEncodedFormEntity(post, HTTP.UTF_8));

                    HttpResponse httpResponse = httpClient.execute(httpPost);

                    String response = EntityUtils.toString(httpResponse.getEntity(), HTTP.UTF_8);
                    applyCount = Integer.parseInt(response);

                }catch (Exception e){
                    e.printStackTrace();
                    applyCount = 0;
                }
            }
        });

        try {
            getCountThread.start();
            getCountThread.join();
        }catch (Exception e){
            e.printStackTrace();
        }

        // 로그아웃 버튼 리스너 구현부
        logOutListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickCount == 0){
                    Toast.makeText(getApplicationContext(), "한 번더 누르면 로그아웃 됩니다.", Toast.LENGTH_SHORT).show();
                    clickCount++;
                    startTime = System.currentTimeMillis();
                    return;
                }
                if(NetworkConnection() && (System.currentTimeMillis() - startTime) < 2000) {
                    try {
                        progressDialog.show();
                        logOutThread.start();
                        logOutThread.join();
                        if (!isLogOutFinished) {
                            progressDialog.dismiss();
                            return;
                        }
                        progressDialog.dismiss();
                        setResult(100);
                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("error", "" + e);
                        progressDialog.dismiss();
                    }
                }else if (!NetworkConnection()) {
                    Toast.makeText(getApplicationContext(), "인터넷 연결을 확인해 주세요.", Toast.LENGTH_SHORT).show();
                    clickCount = 0;
                }
                else
                    clickCount = 0;
            }
        };

        cardItems.add(new CardProfileItem(getResources().getDrawable(R.drawable.apply), name, logOutListener));
        cardItems.add(new CardViewItem(getResources().getDrawable(R.drawable.apply),"수강 신청", background[0]));
        cardItems.add(new CardViewItem(getResources().getDrawable(R.drawable.after_apply),"방과후 수강 신청", background[1]));
        cardItems.add(new CardStringItem("수강신청 1.0","",background[0], background[3]));
        cardItems.add(new CardStringItem("진행중인 수강신청",applyCount+"개",applyCount,background[0], background[3]));
        cardItems.add(new CardViewItem(getResources().getDrawable(R.drawable.apply_history),"수강 신청 목록", background[2]));
        cardItems.add(new CardViewItem(getResources().getDrawable(R.drawable.after_apply_history),"방과후 수강 신청 목록", background[3]));
        cardItems.add(new CardViewItem(getResources().getDrawable(R.drawable.setting2),"설정", background[4]));
        cardItems.add(new CardViewItem(getResources().getDrawable(R.drawable.info2),"공지 사항", background[0]));

        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        progressDialog = new ProgressDialog(MainSelection.this);
        //userName.setText(name);
        progressDialog.setMessage("로그 아웃 중입니다.");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // 로그 아웃을 진행하는 스레드
        logOutThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url;
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
                    }

                }catch (Exception e){
                    e.printStackTrace();
                    Log.e("error", "" +e );
                    progressDialog.dismiss();
                }
            }
        });


    }




    @Override
    public void onBackPressed() {
        // 뒤로 가기 눌렀을시
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
                        if(NetworkConnection()) {
                            try {
                                progressDialog.show();
                                logOutThread.start();
                                logOutThread.join();
                                if (!isLogOutFinished) {
                                    progressDialog.dismiss();
                                    return;
                                }
                                progressDialog.dismiss();
                                setResult(100);
                                finish();
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.e("error", "" + e);
                                progressDialog.dismiss();
                            }
                        }else
                            Toast.makeText(getApplicationContext(), "인터넷 연결을 확인해 주세요.", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();

    }

    @Override
    protected void onDestroy() {
        // TODO: 2017-06-07 종료되기 직전에 로그아웃 구현
        super.onDestroy();
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
