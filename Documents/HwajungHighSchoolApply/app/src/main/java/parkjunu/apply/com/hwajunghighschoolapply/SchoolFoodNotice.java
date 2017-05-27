package parkjunu.apply.com.hwajunghighschoolapply;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class SchoolFoodNotice extends Service {

    static final String HOST_ADDRESS_SCHOOL_FOOD = "http://45.32.52.41:5000/get_school_food";
    Calendar calendar;
    String source;

    public SchoolFoodNotice(){
    }

    @Override
    public void onCreate() {
        super.onCreate();
        calendar = Calendar.getInstance();
        if (!NetworkConnection())
            Toast.makeText(getApplicationContext(),"네트워크에 연결되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
        else{
            getTodaySchoolFood();
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
        if (foods.length < 2)
            content = "오늘은 급식이 없거나 학교를 안가겠죠?";

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


    public String getTodaySchoolFood(){
        try{
            int month = calendar.get(Calendar.MONTH) + 1;
            int date = calendar.get(Calendar.DAY_OF_MONTH);
            File file = new File(""+getExternalFilesDir(null) +"/SchoolFood.txt");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            // SchoolFood.txt 파일이 존재 하지 않는 경우

            if(!file.exists()){
                Log.e("error", "SchoolFood.txt does not exist!");
                if (NetworkConnection())
                    new GetSchoolFood().execute();
                else
                    Toast.makeText(SchoolFoodNotice.this,"네트워크에 연결되어있지않아 \n 급식정보를 가져오지 못했습니다.", Toast.LENGTH_LONG).show();
                return null;
            }


            // SchoolFood.txt 를 읽어 온다.
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
            Log.e("error", "" +e);
        }

        return  null;
    }

    private class GetSchoolFood extends AsyncTask<Void, Void, Void> {
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


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
