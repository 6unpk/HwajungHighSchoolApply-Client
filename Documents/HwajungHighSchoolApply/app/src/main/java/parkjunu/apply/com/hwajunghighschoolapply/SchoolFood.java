package parkjunu.apply.com.hwajunghighschoolapply;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static java.lang.System.currentTimeMillis;

// 급식을 가져오는 Service Component
public class SchoolFood extends Service implements Runnable{
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Calendar calendar;
    String source;
    static final String HOST_ADDRESS_SCHOOL_FOOD = "http://45.32.52.41:5000/get_school_food";

    @Override
    public void onCreate() {
        pref = PreferenceManager.getDefaultSharedPreferences(SchoolFood.this);
        editor= pref.edit();
        calendar = Calendar.getInstance();
        if (NetworkConnection())
            new GetSchoolFood().execute();
        Thread thread = new Thread(this);
        thread.start();
        Thread date = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try{
                        calendar = Calendar.getInstance();
                        int date = calendar.get(Calendar.DAY_OF_MONTH);
                        if(date == 1 && NetworkConnection())
                            new GetSchoolFood().execute();
                        Thread.sleep(3600*6000); // 6시간에 한번
                    }catch (Exception e){
                        e.printStackTrace();
                        Log.e("error", ""+e);
                    }
                }
            }
        });
       date.start();

    }

    // 매 시간을 확인해 급식을 가져온다.


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void run() {
        while(true) {
                    try {
                        // 1초 마다 시간을 확인하고 사용자가 설정한 시간에 맞춰 급식표를 가져온다.
                        calendar = new GregorianCalendar();

                        int second = calendar.get(Calendar.SECOND);
                        int minute = calendar.get(Calendar.MINUTE);
                        int hour =  calendar.get(Calendar.HOUR_OF_DAY);
                        int userSetSec = pref.getInt("sec", 0);
                        int userSetMin = pref.getInt("min", 0);
                        int userSetHour = pref.getInt("hour", 11);

                        if(second == userSetSec && minute == userSetMin && userSetHour == hour){
                            getTodaySchoolFood();
                            Thread.sleep(1000);
                        }

                        Thread.sleep(1000);
                        calendar.clear();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("error", "" + e);
                        break;
                    }
                }
    }



    public void CallNotification(String content){
        // 안드로이드 notification 관련 code
        Resources resources = getResources();
        int today = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        Intent notification = new Intent(this, LoginActivity.class);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 ,notification, PendingIntent.FLAG_UPDATE_CURRENT);

        String foods[] = content.split(":");
        content = "";
        for (String food: foods)
            content += food + "\n";
        // 자동 급식 알리미는 급식이 없는 날의 경우 알림을 띄우지 않는다.
        if (foods.length < 2)
            return;

        String title =""+ month +"월 "+ today +"일 급식 알리미";
        builder.setContentTitle(title).
                setTicker(title)
                .setSmallIcon(R.drawable.small_logo)
                .setLargeIcon(BitmapFactory.decodeResource(resources,R.drawable.logo__))
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setContentText(content)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            builder.setCategory(Notification.CATEGORY_MESSAGE)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
        stopSelf();

    }


    public void getTodaySchoolFood(){
        try{
            int date = calendar.get(Calendar.DAY_OF_MONTH);
            File file = new File(""+getExternalFilesDir(null) +"/SchoolFood.txt");

            // SchoolFood.txt 파일이 존재 하지 않는 경우
            if(!file.exists()) {
                Log.e("error", "SchoolFood.txt does not exist!");
                if (NetworkConnection())
                    new GetSchoolFood().execute().get();
                else {
                    Toast.makeText(SchoolFood.this, "네트워크에 연결되어있지않아 \n 급식정보를 가져오지 못했습니다.", Toast.LENGTH_LONG).show();
                    return;
                }
            }

            // SchoolFood.txt 를 읽어 온다.
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String line;
            String source = "";
            while((line = reader.readLine()) != null)
                source += line;

            Log.d("tag", "Reading SchoolFood.txt was completed");

            JSONObject jsonObject = new JSONObject(source);
            String content = "오늘의 급식은 없습니다.";
            for(int i = 1; i < 36; ++i) {
                JSONObject obj = jsonObject.getJSONObject("" + i);
                String str = obj.getString("content");
                // json 파일 content 요소의 날짜값이 시스템의 날짜와 동일 할 때 까지 순차적으로 탐색
                if(str.split(":")[0].equals(" "+date)) {
                    content = str;
                    break;
                }
            }

            CallNotification(content);

        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(SchoolFood.this, "급식을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
            Log.e("error", "" +e);
        }

    }

    private class GetSchoolFood extends AsyncTask<Void, Void, Void>{
        URL url;
        public GetSchoolFood(){
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try{
                url = new URL(HOST_ADDRESS_SCHOOL_FOOD);
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost();
                httpPost.setURI(url.toURI());

                ArrayList<NameValuePair> post = new ArrayList<>();
                post.add(new BasicNameValuePair("request","hwajung"));
                httpPost.setEntity(new UrlEncodedFormEntity(post));
                HttpResponse response = httpClient.execute(httpPost);
                source = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);

            }catch (Exception e){
                e.printStackTrace();
                Log.e("error", ""+e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            File file = new File(""+getExternalFilesDir(null));
            FileOutputStream outputStream;
            try{
                outputStream = new FileOutputStream(file.getPath()+"/SchoolFood.txt");
                outputStream.write(source.getBytes());
                outputStream.close();
            }catch (Exception e){
                e.printStackTrace();
                Log.e("error", ""+e);
            }

        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
