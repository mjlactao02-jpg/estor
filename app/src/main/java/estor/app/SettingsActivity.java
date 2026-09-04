package estor.app;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class SettingsActivity extends AppCompatActivity {


    // ============================================================
    // PREFERENCES
    // ============================================================

    private static final String SETTINGS_PREFS =
            "EstorSettings";

    private static final String KEY_STORE_NAME =
            "store_name";

    private static final String KEY_REMINDERS =
            "reminders_enabled";

    private static final String KEY_FREQUENCY =
            "reminder_frequency";


    // ============================================================
    // PIN PREFERENCES
    // ============================================================

    private static final String PIN_PREFS =
            "EstorPinPrefs";

    private static final String KEY_RECOVERY_PIN =
            "recovery_pin";


    // ============================================================
    // SMS PERMISSION
    // ============================================================

    private static final int SMS_PERMISSION_REQUEST_CODE =
            100;


    // ============================================================
    // VIEW VARIABLES
    // ============================================================

    private EditText editStoreName;

    private View cardChangePin;

    private Switch switchReminders;

    private RadioGroup radioGroupFrequency;

    private RadioButton radioDaily;

    private RadioButton radioTwiceWeek;

    private RadioButton radioThriceWeek;

    private RadioButton radioFourTimesWeek;

    private RadioButton radioFiveTimesWeek;

    private RadioButton radioEveryWeekend;


    // ============================================================
    // SHARED PREFERENCES
    // ============================================================

    private SharedPreferences settingsPrefs;


    // ============================================================
    // ON CREATE
    // ============================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_main);


        // ========================================================
        // GET PREFERENCES
        // ========================================================

        settingsPrefs =
                getSharedPreferences(
                        SETTINGS_PREFS,
                        MODE_PRIVATE
                );


        // ========================================================
        // INITIALIZE
        // ========================================================

        initializeViews();

        setupStoreName();

        setupBackButton();

        setupChangeRecoveryPin();

        setupReminderSwitch();

        setupFrequency();

        loadSavedSettings();
    }


    // ============================================================
    // INITIALIZE VIEWS
    // ============================================================

    private void initializeViews() {

        editStoreName =
                findViewById(R.id.editStoreName);

        cardChangePin =
                findViewById(R.id.cardChangePin);

        switchReminders =
                findViewById(R.id.switchReminders);

        radioGroupFrequency =
                findViewById(R.id.radioGroupFrequency);

        radioDaily =
                findViewById(R.id.radioDaily);

        radioTwiceWeek =
                findViewById(R.id.radioTwiceWeek);

        radioThriceWeek =
                findViewById(R.id.radioThriceWeek);

        radioFourTimesWeek =
                findViewById(R.id.radioFourTimesWeek);

        radioFiveTimesWeek =
                findViewById(R.id.radioFiveTimesWeek);

        radioEveryWeekend =
                findViewById(R.id.radioEveryWeekend);
    }


    // ============================================================
    // STORE NAME
    // ============================================================

    private void setupStoreName() {

        if (editStoreName == null) {
            return;
        }


        editStoreName.setInputType(
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        );


        editStoreName.setSingleLine(true);


        editStoreName.setImeOptions(
                EditorInfo.IME_ACTION_DONE
        );


        editStoreName.setOnEditorActionListener(
                (v, actionId, event) -> {

                    if (actionId ==
                            EditorInfo.IME_ACTION_DONE) {

                        saveStoreName();

                        return false;
                    }

                    return false;
                }
        );


        editStoreName.setOnFocusChangeListener(
                (v, hasFocus) -> {

                    if (!hasFocus) {

                        saveStoreName();
                    }
                }
        );
    }


    // ============================================================
    // SAVE STORE NAME
    // ============================================================

    private void saveStoreName() {

        if (editStoreName == null) {
            return;
        }


        String storeName =
                editStoreName
                        .getText()
                        .toString()
                        .trim();


        settingsPrefs
                .edit()
                .putString(
                        KEY_STORE_NAME,
                        storeName
                )
                .apply();
    }


    // ============================================================
    // GET STORE NAME
    // ============================================================

    public static String getStoreName(
            android.content.Context context) {

        SharedPreferences prefs =
                context.getSharedPreferences(
                        SETTINGS_PREFS,
                        android.content.Context.MODE_PRIVATE
                );


        return prefs.getString(
                KEY_STORE_NAME,
                ""
        );
    }


    // ============================================================
    // BACK BUTTON
    // ============================================================

    private void setupBackButton() {

        View backButton =
                findViewById(R.id.btnBack);

        if (backButton != null) {

            backButton.setOnClickListener(
                    v -> {

                        saveStoreName();

                        finish();
                    }
            );
        }
    }


    // ============================================================
    // CHANGE RECOVERY PIN
    // ============================================================

    private void setupChangeRecoveryPin() {

        if (cardChangePin == null) {
            return;
        }


        cardChangePin.setOnClickListener(
                v -> {

                    showChangePinDialog();
                }
        );
    }


    // ============================================================
    // CHANGE PIN DIALOG
    //
    // THIS USES YOUR XML RECOVERY PIN UI
    // ============================================================

    private void showChangePinDialog() {

        // ========================================================
        // INFLATE YOUR CUSTOM XML
        // ========================================================

        View dialogView =
                getLayoutInflater().inflate(
                        R.layout.dialog_change_recovery_pin,
                        null
                );


        // ========================================================
        // GET VIEWS FROM YOUR XML
        // ========================================================

        TextView txtDialogTitle =
                dialogView.findViewById(
                        R.id.txtDialogTitle
                );

        EditText editPreviousPin =
                dialogView.findViewById(
                        R.id.editPreviousPin
                );

        EditText editNewPin =
                dialogView.findViewById(
                        R.id.editNewPin
                );

        EditText editConfirmPin =
                dialogView.findViewById(
                        R.id.editConfirmPin
                );

        AppCompatButton btnSaveRecoveryPin =
                dialogView.findViewById(
                        R.id.btnSaveRecoveryPin
                );


        // ========================================================
        // TITLE
        // ========================================================

        if (txtDialogTitle != null) {

            txtDialogTitle.setText(
                    "Change Recovery PIN"
            );
        }


        // ========================================================
        // CREATE DIALOG
        // ========================================================

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setView(dialogView)
                        .create();


        // ========================================================
        // SAVE BUTTON
        // ========================================================

        btnSaveRecoveryPin.setOnClickListener(
                view -> {

                    String previousPin =
                            editPreviousPin
                                    .getText()
                                    .toString()
                                    .trim();

                    String newPin =
                            editNewPin
                                    .getText()
                                    .toString()
                                    .trim();

                    String confirmPin =
                            editConfirmPin
                                    .getText()
                                    .toString()
                                    .trim();


                    // ====================================================
                    // CHECK PREVIOUS PIN
                    // ====================================================

                    if (previousPin.length() != 4) {

                        editPreviousPin.setError(
                                "Enter your 4-digit PIN"
                        );

                        editPreviousPin.requestFocus();

                        return;
                    }


                    // ====================================================
                    // GET SAVED RECOVERY PIN
                    // ====================================================

                    SharedPreferences pinPrefs =
                            getSharedPreferences(
                                    PIN_PREFS,
                                    MODE_PRIVATE
                            );

                    String savedRecoveryPin =
                            pinPrefs.getString(
                                    KEY_RECOVERY_PIN,
                                    ""
                            );


                    // ====================================================
                    // CHECK PREVIOUS PIN
                    // ====================================================

                    if (savedRecoveryPin.isEmpty()) {

                        Toast.makeText(
                                SettingsActivity.this,
                                "No recovery PIN has been set.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }


                    if (!previousPin.equals(
                            savedRecoveryPin)) {

                        editPreviousPin.setError(
                                "Incorrect previous PIN"
                        );

                        editPreviousPin.requestFocus();

                        return;
                    }


                    // ====================================================
                    // CHECK NEW PIN
                    // ====================================================

                    if (newPin.length() != 4) {

                        editNewPin.setError(
                                "PIN must be 4 digits"
                        );

                        editNewPin.requestFocus();

                        return;
                    }


                    // ====================================================
                    // CHECK CONFIRM PIN
                    // ====================================================

                    if (!confirmPin.equals(newPin)) {

                        editConfirmPin.setError(
                                "PINs do not match"
                        );

                        editConfirmPin.requestFocus();

                        return;
                    }


                    // ====================================================
                    // SAVE NEW RECOVERY PIN
                    // ====================================================

                    pinPrefs
                            .edit()
                            .putString(
                                    KEY_RECOVERY_PIN,
                                    newPin
                            )
                            .apply();


                    // ====================================================
                    // SUCCESS MESSAGE
                    // ====================================================

                    Toast.makeText(
                            SettingsActivity.this,
                            "Recovery PIN updated.",
                            Toast.LENGTH_SHORT
                    ).show();


                    // ====================================================
                    // CLOSE DIALOG
                    // ====================================================

                    dialog.dismiss();
                }
        );


        // ========================================================
        // SHOW DIALOG
        // ========================================================

        dialog.show();


        // ========================================================
        // MAKE DIALOG TRANSPARENT AROUND YOUR XML
        // ========================================================

        Window window =
                dialog.getWindow();

        if (window != null) {

            window.setBackgroundDrawableResource(
                    android.R.color.transparent
            );


            // ====================================================
            // DIALOG WIDTH
            // ====================================================

            int screenWidth =
                    getResources()
                            .getDisplayMetrics()
                            .widthPixels;


            int dialogWidth =
                    (int) (screenWidth * 0.90f);


            window.setLayout(
                    dialogWidth,
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }
    }


    // ============================================================
    // REMINDER SWITCH
    // ============================================================

    private void setupReminderSwitch() {

        if (switchReminders == null) {
            return;
        }


        switchReminders.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    settingsPrefs
                            .edit()
                            .putBoolean(
                                    KEY_REMINDERS,
                                    isChecked
                            )
                            .apply();


                    if (isChecked) {

                        enableReminderSystem();

                    } else {

                        ReminderReceiver
                                .cancelReminder(
                                        SettingsActivity.this
                                );


                        setFrequencyEnabled(false);


                        Toast.makeText(
                                SettingsActivity.this,
                                "Payment reminders disabled.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }


    // ============================================================
    // ENABLE REMINDER SYSTEM
    // ============================================================

    private void enableReminderSystem() {

        setFrequencyEnabled(true);


        // ========================================================
        // SMS PERMISSION
        // ========================================================

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(
                    Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {


                requestPermissions(
                        new String[]{
                                Manifest.permission.SEND_SMS
                        },
                        SMS_PERMISSION_REQUEST_CODE
                );


                Toast.makeText(
                        this,
                        "SMS permission is required for automatic reminders.",
                        Toast.LENGTH_LONG
                ).show();


                return;
            }
        }


        // ========================================================
        // EXACT ALARM
        // ========================================================

        requestExactAlarmPermissionIfNeeded();


        // ========================================================
        // SCHEDULE
        // ========================================================

        ReminderReceiver.scheduleReminder(
                SettingsActivity.this
        );


        Toast.makeText(
                this,
                "Payment reminders enabled.",
                Toast.LENGTH_SHORT
        ).show();
    }


    // ============================================================
    // FREQUENCY
    // ============================================================

    private void setupFrequency() {

        if (radioGroupFrequency == null) {
            return;
        }


        radioGroupFrequency.setOnCheckedChangeListener(
                (group, checkedId) -> {

                    String frequency =
                            getFrequencyFromId(
                                    checkedId
                            );


                    if (frequency == null) {
                        return;
                    }


                    // ============================================
                    // SAVE FREQUENCY
                    // ============================================

                    settingsPrefs
                            .edit()
                            .putString(
                                    KEY_FREQUENCY,
                                    frequency
                            )
                            .apply();


                    // ============================================
                    // RESCHEDULE
                    // ============================================

                    boolean remindersEnabled =
                            settingsPrefs.getBoolean(
                                    KEY_REMINDERS,
                                    false
                            );


                    if (remindersEnabled) {

                        if (hasSmsPermission()) {

                            requestExactAlarmPermissionIfNeeded();

                            ReminderReceiver
                                    .scheduleReminder(
                                            SettingsActivity.this
                                    );


                            Toast.makeText(
                                    SettingsActivity.this,
                                    "Reminder frequency changed to " +
                                            frequency,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                }
        );
    }


    // ============================================================
    // GET FREQUENCY FROM RADIO BUTTON
    // ============================================================

    private String getFrequencyFromId(
            int checkedId) {

        if (checkedId ==
                R.id.radioDaily) {

            return "Daily";
        }


        if (checkedId ==
                R.id.radioTwiceWeek) {

            return "Twice a week";
        }


        if (checkedId ==
                R.id.radioThriceWeek) {

            return "Thrice a week";
        }


        if (checkedId ==
                R.id.radioFourTimesWeek) {

            return "4 times a week";
        }


        if (checkedId ==
                R.id.radioFiveTimesWeek) {

            return "5 times a week";
        }


        if (checkedId ==
                R.id.radioEveryWeekend) {

            return "Every weekend";
        }


        return null;
    }


    // ============================================================
    // ENABLE / DISABLE FREQUENCY OPTIONS
    // ============================================================

    private void setFrequencyEnabled(
            boolean enabled) {

        if (radioDaily != null) {
            radioDaily.setEnabled(enabled);
        }

        if (radioTwiceWeek != null) {
            radioTwiceWeek.setEnabled(enabled);
        }

        if (radioThriceWeek != null) {
            radioThriceWeek.setEnabled(enabled);
        }

        if (radioFourTimesWeek != null) {
            radioFourTimesWeek.setEnabled(enabled);
        }

        if (radioFiveTimesWeek != null) {
            radioFiveTimesWeek.setEnabled(enabled);
        }

        if (radioEveryWeekend != null) {
            radioEveryWeekend.setEnabled(enabled);
        }
    }


    // ============================================================
    // LOAD SAVED SETTINGS
    // ============================================================

    private void loadSavedSettings() {

        boolean remindersEnabled =
                settingsPrefs.getBoolean(
                        KEY_REMINDERS,
                        false
                );


        String frequency =
                settingsPrefs.getString(
                        KEY_FREQUENCY,
                        "Daily"
                );


        // ========================================================
        // STORE NAME
        // ========================================================

        String storeName =
                settingsPrefs.getString(
                        KEY_STORE_NAME,
                        ""
                );


        if (editStoreName != null) {

            editStoreName.setText(
                    storeName
            );
        }


        // ========================================================
        // REMINDER SWITCH
        // ========================================================

        if (switchReminders != null) {

            switchReminders.setChecked(
                    remindersEnabled
            );
        }


        // ========================================================
        // FREQUENCY ENABLE/DISABLE
        // ========================================================

        setFrequencyEnabled(
                remindersEnabled
        );


        // ========================================================
        // SELECT FREQUENCY
        // ========================================================

        if (radioGroupFrequency != null) {

            if (frequency.equals("Daily")) {

                radioGroupFrequency.check(
                        R.id.radioDaily
                );

            } else if (
                    frequency.equals(
                            "Twice a week"
                    )) {

                radioGroupFrequency.check(
                        R.id.radioTwiceWeek
                );

            } else if (
                    frequency.equals(
                            "Thrice a week"
                    )) {

                radioGroupFrequency.check(
                        R.id.radioThriceWeek
                );

            } else if (
                    frequency.equals(
                            "4 times a week"
                    )) {

                radioGroupFrequency.check(
                        R.id.radioFourTimesWeek
                );

            } else if (
                    frequency.equals(
                            "5 times a week"
                    )) {

                radioGroupFrequency.check(
                        R.id.radioFiveTimesWeek
                );

            } else if (
                    frequency.equals(
                            "Every weekend"
                    )) {

                radioGroupFrequency.check(
                        R.id.radioEveryWeekend
                );
            }
        }


        // ========================================================
        // RESCHEDULE WHEN SETTINGS ARE LOADED
        // ========================================================

        if (remindersEnabled &&
                hasSmsPermission()) {

            requestExactAlarmPermissionIfNeeded();

            ReminderReceiver.scheduleReminder(
                    SettingsActivity.this
            );
        }
    }


    // ============================================================
    // CHECK SMS PERMISSION
    // ============================================================

    private boolean hasSmsPermission() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {

            return true;
        }


        return checkSelfPermission(
                Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED;
    }


    // ============================================================
    // EXACT ALARM PERMISSION
    // ============================================================

    private void requestExactAlarmPermissionIfNeeded() {

        if (Build.VERSION.SDK_INT <
                Build.VERSION_CODES.S) {

            return;
        }


        AlarmManager alarmManager =
                (AlarmManager)
                        getSystemService(
                                ALARM_SERVICE
                        );


        if (alarmManager == null) {
            return;
        }


        if (alarmManager.canScheduleExactAlarms()) {

            return;
        }


        try {

            Intent intent =
                    new Intent(
                            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    );


            intent.setData(
                    Uri.parse(
                            "package:" +
                                    getPackageName()
                    )
            );


            startActivity(intent);

        } catch (Exception e) {

            e.printStackTrace();

            Toast.makeText(
                    this,
                    "Please allow Estor to set alarms and reminders in Android Settings.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }


    // ============================================================
    // SMS PERMISSION RESULT
    // ============================================================

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );


        if (requestCode ==
                SMS_PERMISSION_REQUEST_CODE) {


            if (grantResults.length > 0 &&
                    grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED) {


                Toast.makeText(
                        this,
                        "SMS permission granted.",
                        Toast.LENGTH_SHORT
                ).show();


                // ================================================
                // NOW SCHEDULE THE REMINDER
                // ================================================

                requestExactAlarmPermissionIfNeeded();


                ReminderReceiver.scheduleReminder(
                        SettingsActivity.this
                );


            } else {

                Toast.makeText(
                        this,
                        "SMS permission is required for payment reminders.",
                        Toast.LENGTH_LONG
                ).show();


                if (switchReminders != null) {

                    switchReminders.setChecked(
                            false
                    );
                }


                settingsPrefs
                        .edit()
                        .putBoolean(
                                KEY_REMINDERS,
                                false
                        )
                        .apply();


                ReminderReceiver.cancelReminder(
                        SettingsActivity.this
                );


                setFrequencyEnabled(false);
            }
        }
    }


    // ============================================================
    // ON PAUSE
    // ============================================================

    @Override
    protected void onPause() {

        super.onPause();

        saveStoreName();
    }


    // ============================================================
    // ON RESUME
    // ============================================================

    @Override
    protected void onResume() {

        super.onResume();


        // ========================================================
        // Re-check reminder after returning from Android's
        // "Alarms & reminders" settings.
        // ========================================================

        if (settingsPrefs == null) {
            return;
        }


        boolean remindersEnabled =
                settingsPrefs.getBoolean(
                        KEY_REMINDERS,
                        false
                );


        if (remindersEnabled &&
                hasSmsPermission()) {

            ReminderReceiver.scheduleReminder(
                    SettingsActivity.this
            );
        }
    }


    // ============================================================
    // BACK PRESSED
    // ============================================================

    @Override
    public void onBackPressed() {

        saveStoreName();

        super.onBackPressed();
    }
}