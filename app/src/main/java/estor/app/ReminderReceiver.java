package estor.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.telephony.SmsManager;

import androidx.core.content.ContextCompat;

public class ReminderReceiver extends BroadcastReceiver {

    // =========================================================
    // CONSTANTS
    // =========================================================

    private static final String PREF_NAME =
            "EstorSettings";

    private static final String KEY_REMINDERS =
            "reminders_enabled";

    private static final String KEY_FREQUENCY =
            "reminder_frequency";

    private static final int REQUEST_CODE = 500;


    // =========================================================
    // ON RECEIVE
    // =========================================================

    @Override
    public void onReceive(
            Context context,
            Intent intent
    ) {

        SharedPreferences preferences =
                context.getSharedPreferences(
                        PREF_NAME,
                        Context.MODE_PRIVATE
                );


        // =====================================================
        // CHECK IF REMINDERS ARE ENABLED
        // =====================================================

        boolean enabled =
                preferences.getBoolean(
                        KEY_REMINDERS,
                        true
                );


        if (!enabled) {

            return;
        }


        // =====================================================
        // CHECK SMS PERMISSION
        // =====================================================

        if (
                ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.SEND_SMS
                )
                        != PackageManager.PERMISSION_GRANTED
        ) {

            return;
        }


        // =====================================================
        // GET DATABASE
        // =====================================================

        DatabaseHelper database =
                new DatabaseHelper(context);


        Cursor cursor =
                database.getAllCustomers();


        try {

            // =================================================
            // LOOP THROUGH CUSTOMERS
            // =================================================

            while (cursor.moveToNext()) {

                int id =
                        cursor.getInt(
                                cursor.getColumnIndexOrThrow(
                                        DatabaseHelper.COLUMN_ID
                                )
                        );


                String name =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        DatabaseHelper.COLUMN_NAME
                                )
                        );


                String phone =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        DatabaseHelper.COLUMN_PHONE
                                )
                        );


                // =================================================
                // GET CUSTOMER'S REMAINING DEBT
                // =================================================

                double debt =
                        getCustomerRemainingDebt(
                                database,
                                id
                        );


                // Only notify customers who still owe money
                if (debt > 0) {

                    sendReminder(
                            phone,
                            name,
                            debt
                    );
                }
            }

        } finally {

            cursor.close();

            database.close();
        }


        // =====================================================
        // SCHEDULE NEXT REMINDER
        // =====================================================

        scheduleReminder(context);
    }


    // =========================================================
    // GET CUSTOMER REMAINING DEBT
    // =========================================================

    private double getCustomerRemainingDebt(
            DatabaseHelper database,
            int customerId
    ) {

        android.database.sqlite.SQLiteDatabase db =
                database.getReadableDatabase();


        Cursor cursor =
                db.rawQuery(
                        "SELECT SUM(" +
                                DatabaseHelper.COLUMN_DEBT_AMOUNT +
                                " - " +
                                DatabaseHelper.COLUMN_DEBT_PAID +
                                ") " +
                                "FROM " +
                                DatabaseHelper.TABLE_DEBTS +
                                " WHERE " +
                                DatabaseHelper.COLUMN_DEBT_CUSTOMER_ID +
                                " = ?",
                        new String[]{
                                String.valueOf(customerId)
                        }
                );


        double debt = 0;


        if (cursor.moveToFirst()) {

            if (!cursor.isNull(0)) {

                debt =
                        cursor.getDouble(0);
            }
        }


        cursor.close();


        return debt;
    }


    // =========================================================
    // SEND SMS
    // =========================================================

    private void sendReminder(
            String phone,
            String name,
            double debt
    ) {

        String message =
                "Hello " +
                        name +
                        ", this is a payment reminder from Estor. " +
                        "Your remaining debt is ₱" +
                        String.format(
                                java.util.Locale.getDefault(),
                                "%.2f",
                                debt
                        ) +
                        ". Please settle your balance. Thank you.";


        SmsManager smsManager =
                SmsManager.getDefault();


        smsManager.sendTextMessage(
                phone,
                null,
                message,
                null,
                null
        );
    }


    // =========================================================
    // SCHEDULE REMINDER
    // =========================================================

    public static void scheduleReminder(
            Context context
    ) {

        SharedPreferences preferences =
                context.getSharedPreferences(
                        PREF_NAME,
                        Context.MODE_PRIVATE
                );


        boolean enabled =
                preferences.getBoolean(
                        KEY_REMINDERS,
                        true
                );


        if (!enabled) {

            return;
        }


        String frequency =
                preferences.getString(
                        KEY_FREQUENCY,
                        "Daily"
                );


        long interval =
                getInterval(frequency);


        AlarmManager alarmManager =
                (AlarmManager)
                        context.getSystemService(
                                Context.ALARM_SERVICE
                        );


        Intent intent =
                new Intent(
                        context,
                        ReminderReceiver.class
                );


        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        REQUEST_CODE,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                );


        long triggerTime =
                System.currentTimeMillis() +
                        interval;


        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.M) {

            alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
            );

        } else {

            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
            );
        }
    }


    // =========================================================
    // GET FREQUENCY INTERVAL
    // =========================================================

    private static long getInterval(
            String frequency
    ) {

        long day =
                24 * 60 * 60 * 1000L;


        if (frequency.equals("Daily")) {

            return day;
        }


        if (frequency.equals("Twice a week")) {

            return 3 * day;
        }


        if (frequency.equals("Thrice a week")) {

            return 2 * day;
        }


        if (frequency.equals("4 times a week")) {

            return 36 * 60 * 60 * 1000L;
        }


        if (frequency.equals("5 times a week")) {

            return 29 * 60 * 60 * 1000L;
        }


        // Every weekend
        return 7 * day;
    }


    // =========================================================
    // CANCEL REMINDER
    // =========================================================

    public static void cancelReminder(
            Context context
    ) {

        AlarmManager alarmManager =
                (AlarmManager)
                        context.getSystemService(
                                Context.ALARM_SERVICE
                        );


        Intent intent =
                new Intent(
                        context,
                        ReminderReceiver.class
                );


        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        REQUEST_CODE,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                );


        alarmManager.cancel(
                pendingIntent
        );
    }
}
