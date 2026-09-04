package estor.app;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.util.Log;

import android.telephony.SmsManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String TAG = "ReminderReceiver";

    public static final String ACTION_REMINDER =
            "estor.app.PAYMENT_REMINDER";

    private static final String PREFS_NAME =
            "EstorSettings";

    private static final String KEY_REMINDERS =
            "reminders_enabled";

    private static final String KEY_FREQUENCY =
            "reminder_frequency";

    private static final int REQUEST_CODE = 500;


    // ============================================================
    // RECEIVER
    // ============================================================

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "====================================");
        Log.d(TAG, "PAYMENT REMINDER RECEIVER STARTED");
        Log.d(TAG, "====================================");

        SharedPreferences prefs =
                context.getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        boolean remindersEnabled =
                prefs.getBoolean(KEY_REMINDERS, false);

        if (!remindersEnabled) {

            Log.d(TAG,
                    "Reminders are disabled. Nothing to send.");

            cancelReminder(context);

            return;
        }


        // ========================================================
        // CHECK SMS PERMISSION
        // ========================================================

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (context.checkSelfPermission(
                    Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {

                Log.e(TAG,
                        "SEND_SMS permission is not granted.");

                // Schedule the next reminder anyway.
                scheduleReminder(context);

                return;
            }
        }


        DatabaseHelper dbHelper = null;
        Cursor cursor = null;

        try {

            dbHelper = new DatabaseHelper(context);

            cursor = dbHelper.getAllCustomers();

            if (cursor == null) {

                Log.e(TAG,
                        "Customer cursor is null.");

                scheduleReminder(context);

                return;
            }


            String storeName =
                    SettingsActivity.getStoreName(context);

            if (storeName == null ||
                    storeName.trim().isEmpty()) {

                storeName = "estor";
            }


            int customerCount = 0;

            while (cursor.moveToNext()) {

                customerCount++;

                int customerId =
                        cursor.getInt(
                                cursor.getColumnIndexOrThrow("id")
                        );

                String customerName =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow("name")
                        );

                String customerPhone =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow("phone")
                        );


                Log.d(TAG,
                        "Customer: " + customerName);

                Log.d(TAG,
                        "Phone: " + customerPhone);


                // =================================================
                // CHECK PHONE NUMBER
                // =================================================

                if (customerPhone == null ||
                        customerPhone.trim().isEmpty()) {

                    Log.d(TAG,
                            "Skipping customer because phone is empty.");

                    continue;
                }


                // =================================================
                // GET REMAINING DEBT
                // =================================================

                double remainingDebt =
                        dbHelper.getCustomerTotalDebt(
                                customerId
                        );


                Log.d(TAG,
                        "Remaining debt: ₱" +
                                remainingDebt);


                // =================================================
                // DO NOT SEND IF DEBT IS ZERO
                // =================================================

                if (remainingDebt <= 0) {

                    Log.d(TAG,
                            "Skipping customer because debt is ₱0.");

                    continue;
                }


                // =================================================
                // FORMAT DEBT
                // =================================================

                String formattedDebt =
                        String.format(
                                Locale.US,
                                "%,.2f",
                                remainingDebt
                        );


                // =================================================
                // SMS MESSAGE
                // =================================================

                String message =
                        "Hello " +
                                customerName +
                                ", this is a payment reminder from " +
                                storeName +
                                ". Your remaining debt is ₱" +
                                formattedDebt +
                                ". Please settle your balance. Thank you.";


                // =================================================
                // SEND SMS
                // =================================================

                try {

                    SmsManager smsManager =
                            SmsManager.getDefault();

                    smsManager.sendTextMessage(
                            customerPhone.trim(),
                            null,
                            message,
                            null,
                            null
                    );

                    Log.d(TAG,
                            "SMS SENT successfully to " +
                                    customerName);

                } catch (Exception e) {

                    Log.e(TAG,
                            "Failed to send SMS to " +
                                    customerName,
                            e);
                }
            }


            Log.d(TAG,
                    "Total customers checked: " +
                            customerCount);


        } catch (Exception e) {

            Log.e(TAG,
                    "Error while processing reminders.",
                    e);

        } finally {

            if (cursor != null) {
                cursor.close();
            }

            if (dbHelper != null) {
                dbHelper.close();
            }
        }


        // ========================================================
        // SCHEDULE THE NEXT REMINDER
        // ========================================================

        scheduleReminder(context);

        Log.d(TAG,
                "Next payment reminder scheduled.");

        Log.d(TAG,
                "====================================");
    }


    // ============================================================
    // SCHEDULE REMINDER
    // ============================================================

    public static void scheduleReminder(Context context) {

        SharedPreferences prefs =
                context.getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        boolean remindersEnabled =
                prefs.getBoolean(KEY_REMINDERS, false);

        if (!remindersEnabled) {

            Log.d(TAG,
                    "Reminder scheduling skipped because disabled.");

            cancelReminder(context);

            return;
        }


        String frequency =
                prefs.getString(
                        KEY_FREQUENCY,
                        "Daily"
                );


        AlarmManager alarmManager =
                (AlarmManager)
                        context.getSystemService(
                                Context.ALARM_SERVICE
                        );

        if (alarmManager == null) {

            Log.e(TAG,
                    "AlarmManager is unavailable.");

            return;
        }


        Intent intent =
                new Intent(
                        context,
                        ReminderReceiver.class
                );

        intent.setAction(ACTION_REMINDER);


        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        REQUEST_CODE,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                );


        // ========================================================
        // CANCEL OLD ALARM FIRST
        // ========================================================

        alarmManager.cancel(pendingIntent);


        // ========================================================
        // CALCULATE NEXT REMINDER
        // ========================================================

        Calendar nextReminder =
                getNextReminderTime(frequency);


        long triggerTime =
                nextReminder.getTimeInMillis();


        Log.d(TAG,
                "Frequency: " + frequency);

        Log.d(TAG,
                "Next reminder: " +
                        new SimpleDateFormat(
                                "yyyy-MM-dd HH:mm:ss",
                                Locale.getDefault()
                        ).format(
                                new Date(triggerTime)
                        ));


        // ========================================================
        // ANDROID 12+ EXACT ALARM
        // ========================================================

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            if (alarmManager.canScheduleExactAlarms()) {

                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                );

                Log.d(TAG,
                        "Exact alarm scheduled.");

            } else {

                // Exact alarm permission is not available.
                // Use inexact alarm instead.

                alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                );

                Log.d(TAG,
                        "Inexact alarm scheduled because " +
                                "exact alarm permission is unavailable.");
            }

        }

        // ========================================================
        // ANDROID 6 - ANDROID 11
        // ========================================================

        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
            );

            Log.d(TAG,
                    "Exact alarm scheduled for Android M+.");

        }

        // ========================================================
        // OLDER ANDROID
        // ========================================================

        else {

            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
            );

            Log.d(TAG,
                    "Standard alarm scheduled.");
        }
    }


    // ============================================================
    // CALCULATE NEXT REMINDER DATE
    // ============================================================

    private static Calendar getNextReminderTime(
            String frequency) {

        Calendar now =
                Calendar.getInstance();


        // ========================================================
        // SET THE REMINDER TIME
        //
        // Change these if you want a specific time.
        //
        // Currently:
        // 8:00 AM
        // ========================================================

        int reminderHour = 8;
        int reminderMinute = 0;


        Calendar next =
                Calendar.getInstance();

        next.set(
                Calendar.HOUR_OF_DAY,
                reminderHour
        );

        next.set(
                Calendar.MINUTE,
                reminderMinute
        );

        next.set(
                Calendar.SECOND,
                0
        );

        next.set(
                Calendar.MILLISECOND,
                0
        );


        // ========================================================
        // DAILY
        // ========================================================

        if (frequency.equals("Daily")) {

            if (!next.after(now)) {

                next.add(
                        Calendar.DAY_OF_YEAR,
                        1
                );
            }

            return next;
        }


        // ========================================================
        // TWICE A WEEK
        //
        // Monday + Thursday
        // ========================================================

        if (frequency.equals("Twice a week")) {

            int[] days = {
                    Calendar.MONDAY,
                    Calendar.THURSDAY
            };

            return getNextWeeklyDay(
                    now,
                    next,
                    days
            );
        }


        // ========================================================
        // THRICE A WEEK
        //
        // Monday + Wednesday + Friday
        // ========================================================

        if (frequency.equals("Thrice a week")) {

            int[] days = {
                    Calendar.MONDAY,
                    Calendar.WEDNESDAY,
                    Calendar.FRIDAY
            };

            return getNextWeeklyDay(
                    now,
                    next,
                    days
            );
        }


        // ========================================================
        // FOUR TIMES A WEEK
        //
        // Monday + Wednesday + Friday + Sunday
        // ========================================================

        if (frequency.equals("4 times a week")) {

            int[] days = {
                    Calendar.MONDAY,
                    Calendar.WEDNESDAY,
                    Calendar.FRIDAY,
                    Calendar.SUNDAY
            };

            return getNextWeeklyDay(
                    now,
                    next,
                    days
            );
        }


        // ========================================================
        // FIVE TIMES A WEEK
        //
        // Monday - Friday
        // ========================================================

        if (frequency.equals("5 times a week")) {

            int[] days = {
                    Calendar.MONDAY,
                    Calendar.TUESDAY,
                    Calendar.WEDNESDAY,
                    Calendar.THURSDAY,
                    Calendar.FRIDAY
            };

            return getNextWeeklyDay(
                    now,
                    next,
                    days
            );
        }


        // ========================================================
        // EVERY WEEKEND
        //
        // Saturday + Sunday
        // ========================================================

        if (frequency.equals("Every weekend")) {

            int[] days = {
                    Calendar.SATURDAY,
                    Calendar.SUNDAY
            };

            return getNextWeeklyDay(
                    now,
                    next,
                    days
            );
        }


        // ========================================================
        // DEFAULT = DAILY
        // ========================================================

        if (!next.after(now)) {

            next.add(
                    Calendar.DAY_OF_YEAR,
                    1
            );
        }

        return next;
    }


    // ============================================================
    // FIND NEXT SELECTED WEEKDAY
    // ============================================================

    private static Calendar getNextWeeklyDay(
            Calendar now,
            Calendar base,
            int[] allowedDays) {


        Calendar candidate =
                (Calendar) base.clone();


        // Search the next 7 days.
        for (int i = 0; i <= 7; i++) {

            candidate =
                    (Calendar) base.clone();

            candidate.add(
                    Calendar.DAY_OF_YEAR,
                    i
            );


            int day =
                    candidate.get(
                            Calendar.DAY_OF_WEEK
                    );


            boolean allowed = false;

            for (int allowedDay :
                    allowedDays) {

                if (day == allowedDay) {

                    allowed = true;
                    break;
                }
            }


            if (allowed &&
                    candidate.after(now)) {

                return candidate;
            }
        }


        // Safety fallback.
        candidate =
                (Calendar) base.clone();

        candidate.add(
                Calendar.DAY_OF_YEAR,
                7
        );

        return candidate;
    }


    // ============================================================
    // CANCEL REMINDER
    // ============================================================

    public static void cancelReminder(
            Context context) {

        AlarmManager alarmManager =
                (AlarmManager)
                        context.getSystemService(
                                Context.ALARM_SERVICE
                        );

        if (alarmManager == null) {
            return;
        }


        Intent intent =
                new Intent(
                        context,
                        ReminderReceiver.class
                );

        intent.setAction(ACTION_REMINDER);


        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        REQUEST_CODE,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                );


        alarmManager.cancel(pendingIntent);

        pendingIntent.cancel();


        Log.d(TAG,
                "Payment reminder cancelled.");
    }
}