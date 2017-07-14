package parkjunu.apply.com.hwajunghighschoolapply;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootingSensor extends BroadcastReceiver {
    public BootingSensor() {
    }

    // 폰이 처음 부팅 되면 SchoolFood Service 를 시작한다
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startSchoolFoodReceiver = new Intent(context, SchoolFood.class);
        context.startService(startSchoolFoodReceiver);
    }
}
