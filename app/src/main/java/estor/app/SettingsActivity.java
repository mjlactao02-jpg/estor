package estor.app;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


/**
 * =============================================================
 * SETTINGS ACTIVITY
 * =============================================================
 *
 * This activity handles:
 *
 * 1. Change Recovery PIN
 * 2. Payment Reminders ON/OFF
 * 3. Reminder Frequency
 *
 * Settings are stored using SharedPreferences.
 *
 * =============================================================
 */
public class SettingsActivity extends AppCompatActivity {


    // =========================================================
    // SHARED PREFERENCES
    // =========================================================

    /*
     * Name of the SharedPreferences file.
     */
    private static final String SETTINGS_PREFS =
            "EstorSettings";


    /*
     * Key used to store whether reminders
     * are enabled or disabled.
     */
    private static final String KEY_REMINDERS =
            "reminders_enabled";


    /*
     * Key used to store reminder frequency.
     */
    private static final String KEY_FREQUENCY =
            "reminder_frequency";


    // =========================================================
    // PIN PREFERENCES
    // =========================================================

    /*
     * This must match the preference name
     * used by PinActivity.
     */
    private static final String PIN_PREFS =
            "EstorPinPrefs";


    /*
     * Key used to store the recovery PIN.
     */
    private static final String KEY_RECOVERY_PIN =
            "recovery_pin";


    // =========================================================
    // SMS PERMISSION
    // =========================================================

    /*
     * Request code used when asking Android
     * for SEND_SMS permission.
     */
    private static final int SMS_PERMISSION_REQUEST =
            100;


    // =========================================================
    // UI ELEMENTS
    // =========================================================

    /*
     * Change Recovery PIN card.
     */
    private View cardChangePin;


    /*
     * Payment reminder switch.
     */
    private Switch switchReminders;


    /*
     * Radio button group.
     */
    private RadioGroup radioGroupFrequency;


    /*
     * Frequency radio buttons.
     */
    private RadioButton radioDaily;
    private RadioButton radioTwiceWeek;
    private RadioButton radioThriceWeek;
    private RadioButton radioFourTimesWeek;
    private RadioButton radioFiveTimesWeek;
    private RadioButton radioEveryWeekend;


    // =========================================================
    // SHARED PREFERENCES OBJECT
    // =========================================================

    private SharedPreferences settingsPreferences;


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        // -----------------------------------------------------
        // LOAD SETTINGS XML
        // -----------------------------------------------------

        setContentView(
                R.layout.settings_main
        );


        // -----------------------------------------------------
        // INITIALIZE SHARED PREFERENCES
        // -----------------------------------------------------

        settingsPreferences =
                getSharedPreferences(
                        SETTINGS_PREFS,
                        MODE_PRIVATE
                );


        // -----------------------------------------------------
        // INITIALIZE UI
        // -----------------------------------------------------

        initializeViews();


        // -----------------------------------------------------
        // LOAD SAVED SETTINGS
        // -----------------------------------------------------

        loadSavedSettings();


        // -----------------------------------------------------
        // BACK BUTTON
        // -----------------------------------------------------

        setupBackButton();


        // -----------------------------------------------------
        // CHANGE RECOVERY PIN
        // -----------------------------------------------------

        setupChangeRecoveryPin();


        // -----------------------------------------------------
        // PAYMENT REMINDERS
        // -----------------------------------------------------

        setupReminderSwitch();


        // -----------------------------------------------------
        // FREQUENCY
        // -----------------------------------------------------

        setupFrequency();
    }


    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private void initializeViews() {

        /*
         * Change Recovery PIN card.
         */
        cardChangePin =
                findViewById(
                        R.id.cardChangePin
                );


        /*
         * Payment reminder switch.
         */
        switchReminders =
                findViewById(
                        R.id.switchReminders
                );


        /*
         * Frequency group.
         */
        radioGroupFrequency =
                findViewById(
                        R.id.radioGroupFrequency
                );


        /*
         * Frequency choices.
         */
        radioDaily =
                findViewById(
                        R.id.radioDaily
                );


        radioTwiceWeek =
                findViewById(
                        R.id.radioTwiceWeek
                );


        radioThriceWeek =
                findViewById(
                        R.id.radioThriceWeek
                );


        radioFourTimesWeek =
                findViewById(
                        R.id.radioFourTimesWeek
                );


        radioFiveTimesWeek =
                findViewById(
                        R.id.radioFiveTimesWeek
                );


        radioEveryWeekend =
                findViewById(
                        R.id.radioEveryWeekend
                );
    }


    // =========================================================
    // BACK BUTTON
    // =========================================================

    private void setupBackButton() {

        View btnBack =
                findViewById(
                        R.id.btnBack
                );


        btnBack.setOnClickListener(
                v -> finish()
        );
    }


    // =========================================================
    // LOAD SAVED SETTINGS
    // =========================================================

    private void loadSavedSettings() {

        /*
         * Get saved reminder status.
         *
         * Default is TRUE because your XML
         * currently has android:checked="true".
         */
        boolean remindersEnabled =
                settingsPreferences.getBoolean(
                        KEY_REMINDERS,
                        true
                );


        /*
         * Set switch state.
         */
        switchReminders.setChecked(
                remindersEnabled
        );


        /*
         * Get saved frequency.
         *
         * Default = Daily.
         */
        String savedFrequency =
                settingsPreferences.getString(
                        KEY_FREQUENCY,
                        "Daily"
                );


        /*
         * Select the saved radio button.
         */
        selectFrequency(
                savedFrequency
        );


        /*
         * Enable or disable frequency choices.
         */
        setFrequencyEnabled(
                remindersEnabled
        );
    }


    // =========================================================
    // CHANGE RECOVERY PIN
    // =========================================================

    private void setupChangeRecoveryPin() {

        /*
         * When the user clicks the
         * Change Recovery PIN card,
         * show the custom popup.
         */
        cardChangePin.setOnClickListener(
                v -> showChangeRecoveryPinDialog()
        );
    }


    // =========================================================
    // CHANGE RECOVERY PIN DIALOG
    // =========================================================

    private void showChangeRecoveryPinDialog() {

        // -----------------------------------------------------
        // CREATE ALERT DIALOG
        // -----------------------------------------------------

        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);


        // -----------------------------------------------------
        // LOAD CUSTOM XML
        // -----------------------------------------------------

        View dialogView =
                getLayoutInflater().inflate(
                        R.layout.dialog_change_recovery_pin,
                        null
                );


        // Put custom layout inside dialog.
        builder.setView(
                dialogView
        );


        // Create dialog.
        AlertDialog dialog =
                builder.create();


        // -----------------------------------------------------
        // GET INPUT FIELDS
        // -----------------------------------------------------

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


        Button btnSave =
                dialogView.findViewById(
                        R.id.btnSaveRecoveryPin
                );


        // -----------------------------------------------------
        // SAVE BUTTON
        // -----------------------------------------------------

        btnSave.setOnClickListener(
                v -> {

                    /*
                     * Get previous PIN.
                     */
                    String previousPin =
                            editPreviousPin
                                    .getText()
                                    .toString()
                                    .trim();


                    /*
                     * Get new PIN.
                     */
                    String newPin =
                            editNewPin
                                    .getText()
                                    .toString()
                                    .trim();


                    /*
                     * Get confirmation PIN.
                     */
                    String confirmPin =
                            editConfirmPin
                                    .getText()
                                    .toString()
                                    .trim();


                    // =================================================
                    // GET SAVED RECOVERY PIN
                    // =================================================

                    SharedPreferences pinPreferences =
                            getSharedPreferences(
                                    PIN_PREFS,
                                    MODE_PRIVATE
                            );


                    String savedRecoveryPin =
                            pinPreferences.getString(
                                    KEY_RECOVERY_PIN,
                                    ""
                            );


                    // =================================================
                    // CHECK PREVIOUS PIN
                    // =================================================

                    if (previousPin.isEmpty()) {

                        editPreviousPin.setError(
                                "Enter your previous recovery PIN"
                        );

                        editPreviousPin.requestFocus();

                        return;
                    }


                    // =================================================
                    // VERIFY PREVIOUS PIN
                    // =================================================

                    if (!previousPin.equals(
                            savedRecoveryPin
                    )) {

                        editPreviousPin.setError(
                                "Incorrect recovery PIN"
                        );

                        editPreviousPin.requestFocus();

                        return;
                    }


                    // =================================================
                    // CHECK NEW PIN
                    // =================================================

                    if (newPin.isEmpty()) {

                        editNewPin.setError(
                                "Enter a new PIN"
                        );

                        editNewPin.requestFocus();

                        return;
                    }


                    // New PIN must be exactly 4 digits.
                    if (newPin.length() != 4) {

                        editNewPin.setError(
                                "PIN must contain 4 digits"
                        );

                        editNewPin.requestFocus();

                        return;
                    }


                    // =================================================
                    // CHECK CONFIRM PIN
                    // =================================================

                    if (confirmPin.isEmpty()) {

                        editConfirmPin.setError(
                                "Confirm your new PIN"
                        );

                        editConfirmPin.requestFocus();

                        return;
                    }


                    // =================================================
                    // COMPARE NEW PIN
                    // =================================================

                    if (!newPin.equals(
                            confirmPin
                    )) {

                        editConfirmPin.setError(
                                "PINs do not match"
                        );

                        editConfirmPin.requestFocus();

                        return;
                    }


                    // =================================================
                    // SAVE NEW RECOVERY PIN
                    // =================================================

                    pinPreferences
                            .edit()
                            .putString(
                                    KEY_RECOVERY_PIN,
                                    newPin
                            )
                            .apply();


                    // =================================================
                    // SUCCESS MESSAGE
                    // =================================================

                    Toast.makeText(
                            SettingsActivity.this,
                            "Recovery PIN changed successfully.",
                            Toast.LENGTH_SHORT
                    ).show();


                    // Close dialog.
                    dialog.dismiss();
                }
        );


        // =========================================================
        // SHOW DIALOG
        // =========================================================

        dialog.show();


        // =========================================================
        // DIALOG SIZE
        // =========================================================

        Window window =
                dialog.getWindow();


        if (window != null) {

            /*
             * Make default dialog background transparent.
             */
            window.setBackgroundDrawableResource(
                    android.R.color.transparent
            );


            /*
             * Set dialog width.
             *
             * 90% of the screen width gives
             * a similar appearance to your screenshot.
             */
            WindowManager.LayoutParams params =
                    window.getAttributes();


            params.width =
                    (int) (
                            getResources()
                                    .getDisplayMetrics()
                                    .widthPixels
                                    * 0.90
                    );


            params.height =
                    WindowManager.LayoutParams.WRAP_CONTENT;


            window.setAttributes(
                    params
            );
        }
    }


    // =========================================================
    // PAYMENT REMINDER SWITCH
    // =========================================================

    private void setupReminderSwitch() {

        switchReminders.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    // =================================================
                    // SAVE SWITCH STATUS
                    // =================================================

                    settingsPreferences
                            .edit()
                            .putBoolean(
                                    KEY_REMINDERS,
                                    isChecked
                            )
                            .apply();


                    // =================================================
                    // ENABLE / DISABLE FREQUENCY
                    // =================================================

                    setFrequencyEnabled(
                            isChecked
                    );


                    // =================================================
                    // REMINDERS ENABLED
                    // =================================================

                    if (isChecked) {

                        /*
                         * Check SMS permission.
                         */
                        if (
                                ContextCompat.checkSelfPermission(
                                        SettingsActivity.this,
                                        Manifest.permission.SEND_SMS
                                )
                                        != PackageManager.PERMISSION_GRANTED
                        ) {

                            /*
                             * Ask Android for SMS permission.
                             */
                            ActivityCompat.requestPermissions(
                                    SettingsActivity.this,
                                    new String[]{
                                            Manifest.permission.SEND_SMS
                                    },
                                    SMS_PERMISSION_REQUEST
                            );

                        } else {

                            /*
                             * Permission already exists.
                             */
                            Toast.makeText(
                                    SettingsActivity.this,
                                    "Payment reminders enabled.",
                                    Toast.LENGTH_SHORT
                            ).show();


                            /*
                             * Schedule reminder.
                             */
                            scheduleReminder();
                        }


                    }

                    // =================================================
                    // REMINDERS DISABLED
                    // =================================================

                    else {

                        /*
                         * Cancel scheduled reminders.
                         */
                        ReminderReceiver.cancelReminder(
                                SettingsActivity.this
                        );


                        Toast.makeText(
                                SettingsActivity.this,
                                "Payment reminders disabled.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }


    // =========================================================
    // FREQUENCY
    // =========================================================

    private void setupFrequency() {

        radioGroupFrequency.setOnCheckedChangeListener(
                (group, checkedId) -> {

                    String frequency;


                    // -------------------------------------------------
                    // DAILY
                    // -------------------------------------------------

                    if (checkedId == R.id.radioDaily) {

                        frequency =
                                "Daily";
                    }


                    // -------------------------------------------------
                    // TWICE A WEEK
                    // -------------------------------------------------

                    else if (
                            checkedId ==
                                    R.id.radioTwiceWeek
                    ) {

                        frequency =
                                "Twice a week";
                    }


                    // -------------------------------------------------
                    // THRICE A WEEK
                    // -------------------------------------------------

                    else if (
                            checkedId ==
                                    R.id.radioThriceWeek
                    ) {

                        frequency =
                                "Thrice a week";
                    }


                    // -------------------------------------------------
                    // FOUR TIMES A WEEK
                    // -------------------------------------------------

                    else if (
                            checkedId ==
                                    R.id.radioFourTimesWeek
                    ) {

                        frequency =
                                "4 times a week";
                    }


                    // -------------------------------------------------
                    // FIVE TIMES A WEEK
                    // -------------------------------------------------

                    else if (
                            checkedId ==
                                    R.id.radioFiveTimesWeek
                    ) {

                        frequency =
                                "5 times a week";
                    }


                    // -------------------------------------------------
                    // EVERY WEEKEND
                    // -------------------------------------------------

                    else {

                        frequency =
                                "Every weekend";
                    }


                    // =================================================
                    // SAVE FREQUENCY
                    // =================================================

                    settingsPreferences
                            .edit()
                            .putString(
                                    KEY_FREQUENCY,
                                    frequency
                            )
                            .apply();


                    // =================================================
                    // RESCHEDULE
                    // =================================================

                    if (
                            switchReminders.isChecked()
                    ) {

                        scheduleReminder();
                    }
                }
        );
    }


    // =========================================================
    // SELECT FREQUENCY
    // =========================================================

    private void selectFrequency(
            String frequency
    ) {

        if (
                frequency.equals("Daily")
        ) {

            radioDaily.setChecked(
                    true
            );

        } else if (
                frequency.equals("Twice a week")
        ) {

            radioTwiceWeek.setChecked(
                    true
            );

        } else if (
                frequency.equals("Thrice a week")
        ) {

            radioThriceWeek.setChecked(
                    true
            );

        } else if (
                frequency.equals("4 times a week")
        ) {

            radioFourTimesWeek.setChecked(
                    true
            );

        } else if (
                frequency.equals("5 times a week")
        ) {

            radioFiveTimesWeek.setChecked(
                    true
            );

        } else {

            radioEveryWeekend.setChecked(
                    true
            );
        }
    }


    // =========================================================
    // ENABLE / DISABLE FREQUENCY
    // =========================================================

    private void setFrequencyEnabled(
            boolean enabled
    ) {

        /*
         * Enable or disable every
         * frequency radio button.
         */

        radioDaily.setEnabled(
                enabled
        );


        radioTwiceWeek.setEnabled(
                enabled
        );


        radioThriceWeek.setEnabled(
                enabled
        );


        radioFourTimesWeek.setEnabled(
                enabled
        );


        radioFiveTimesWeek.setEnabled(
                enabled
        );


        radioEveryWeekend.setEnabled(
                enabled
        );
    }


    // =========================================================
    // SCHEDULE REMINDER
    // =========================================================

    private void scheduleReminder() {

        /*
         * Call ReminderReceiver.
         *
         * ReminderReceiver is responsible for
         * checking the database and sending
         * payment reminder SMS.
         */

        ReminderReceiver.scheduleReminder(
                SettingsActivity.this
        );
    }


    // =========================================================
    // SMS PERMISSION RESULT
    // =========================================================

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );


        // =====================================================
        // CHECK OUR REQUEST
        // =====================================================

        if (
                requestCode ==
                        SMS_PERMISSION_REQUEST
        ) {


            // =================================================
            // PERMISSION GRANTED
            // =================================================

            if (
                    grantResults.length > 0 &&
                            grantResults[0] ==
                                    PackageManager.PERMISSION_GRANTED
            ) {

                /*
                 * Make sure switch stays ON.
                 */
                switchReminders.setChecked(
                        true
                );


                /*
                 * Schedule reminder.
                 */
                scheduleReminder();


                Toast.makeText(
                        SettingsActivity.this,
                        "Payment reminders enabled.",
                        Toast.LENGTH_SHORT
                ).show();
            }


            // =================================================
            // PERMISSION DENIED
            // =================================================

            else {

                /*
                 * Turn switch OFF because
                 * SMS permission was denied.
                 */
                switchReminders.setChecked(
                        false
                );


                /*
                 * Disable frequency options.
                 */
                setFrequencyEnabled(
                        false
                );


                /*
                 * Save OFF state.
                 */
                settingsPreferences
                        .edit()
                        .putBoolean(
                                KEY_REMINDERS,
                                false
                        )
                        .apply();


                Toast.makeText(
                        SettingsActivity.this,
                        "SMS permission is required for payment reminders.",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }


    // =========================================================
    // DP TO PX
    // =========================================================

    private int dpToPx(
            int dp
    ) {

        /*
         * Convert dp to pixels.
         */
        float density =
                getResources()
                        .getDisplayMetrics()
                        .density;


        return (int) (
                dp * density + 0.5f
        );
    }
}