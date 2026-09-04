package estor.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {

            SharedPreferences prefs =
                    context.getSharedPreferences(
                            "EstorSettings",
                            Context.MODE_PRIVATE
                    );

            boolean remindersEnabled =
                    prefs.getBoolean(
                            "reminders_enabled",
                            false
                    );

            if (remindersEnabled) {

                ReminderReceiver.scheduleReminder(context);

                Log.d(
                        "BootReceiver",
                        "Payment reminder rescheduled after phone restart."
                );
            }
        }
    }
}