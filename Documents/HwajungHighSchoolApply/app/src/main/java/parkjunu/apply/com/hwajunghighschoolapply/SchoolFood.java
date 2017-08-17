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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
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

        String foods[] = content.split(" ");
        StringBuffer buffer = new StringBuffer();

        // 개행문자 추가로 급식 정보 정렬
        for (String food: foods)
            buffer.append(food + "\n");

        // 급식이 없거나 학교를 안가는날
        if (foods.length < 2)
            return;
        else
            content = buffer.toString();

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

            JSONObject obj = new JSONObject(source);
            String content = "";

            // json 파일의 날짜값(KEY)을 통해 해당 날짜에 있는 급식 정보를 불러온다
            content = obj.getString(Integer.toString(date));
            CallNotification(content);

            CallNotification(content);

        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(SchoolFood.this, "급식을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
            Log.e("error", "" +e);
        }

    }


    private class GetSchoolFood extends AsyncTask<Void, Void, Void> {
        boolean isError = false;
        public GetSchoolFood(){
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("tag","ok");
        }

        @Override
        protected Void doInBackground(Void... params) {
            try{
                // 경기도 교육청 급식표 가져오기, 학교코드 J100000869 (쿠키값)
                StringBuffer url = new StringBuffer("http://stu.goe.go.kr/sts_sci_md00_001.do");
                url.append("?");
                url.append("schulCode=J100000869&");
                url.append("schulCrseScCode=4&");
                url.append("schulKndScCode=04&");
                url.append("schYm="+calendar.get(Calendar.YEAR)+String.format("%02d", (calendar.get(Calendar.MONTH) + 1))+"&");

                String content = getContentFromUrl(new URL(url.toString()), "<tbody>", "</tbody>");
                Log.d("tag", content);
                parseToJSOSN(content);

            }catch (Exception e){
                e.printStackTrace();
                Log.e("error", ""+e);
                isError = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // 급식표를 불러오지 못했을경우 SchoolFood.txt에 error로 파일을 저장
            if (isError){
                try {
                    File file = new File(""+getExternalFilesDir(null));
                    FileOutputStream outputStream;
                    outputStream = new FileOutputStream(file.getPath()+"/SchoolFood.txt");
                    String error = "error";
                    outputStream.write(error.getBytes());
                    outputStream.close();
                }catch (Exception e){

                }
            }
        }
    }

    public String getContentFromUrl(URL url, String readAfter, String readBefore) {

        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

            StringBuffer buffer = new StringBuffer();
            String inputLine;

            boolean reading = false;


            while((inputLine = reader.readLine()) != null){
                if (reading){
                    if (inputLine.contains(readBefore))
                        break;
                    buffer.append(inputLine);
                } else {
                    if ( inputLine.contains(readAfter))
                        reading = true;
                }
            }
            reader.close();
            return buffer.toString();
        }catch (Exception e){
            e.printStackTrace();
            Log.d("tag", ""+e);
            return "";
        }
    }

    // 다운받은 급식 정보를 JSON으로 변환후 저장
    public void parseToJSOSN(String content){
        try {
            Document jsoup = Jsoup.parse(content);
            Elements tds = jsoup.select("div");
            JSONObject jsonObject = new JSONObject();

            int date = 1;
            for(int i = 0; i < tds.size(); ++i){
                if(!tds.get(i).text().equals("")){
                    jsonObject.put(Integer.toString(date), tds.get(i).text());
                    ++date;
                }
            }
            File file = new File(""+getExternalFilesDir(null));
            FileOutputStream outputStream;
            outputStream = new FileOutputStream(file.getPath()+"/SchoolFood.txt");
            outputStream.write(jsonObject.toString().getBytes());
            outputStream.close();

        }catch (Exception e){
            e.printStackTrace();
            Log.d("error", "" + e);
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
