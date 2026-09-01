package estor.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PinActivity extends AppCompatActivity {

    // =========================================================
    // SHARED PREFERENCES
    // =========================================================

    private static final String PREF_NAME = "EstorPinPrefs";

    // Main PIN
    private static final String KEY_PIN = "saved_pin";

    // Recovery PIN
    private static final String KEY_RECOVERY_PIN = "recovery_pin";


    // =========================================================
    // UI
    // =========================================================

    private TextView tvTitle;
    private TextView tvSubtitle;

    private View dot1;
    private View dot2;
    private View dot3;
    private View dot4;

    private TextView key0;
    private TextView key1;
    private TextView key2;
    private TextView key3;
    private TextView key4;
    private TextView key5;
    private TextView key6;
    private TextView key7;
    private TextView key8;
    private TextView key9;

    private TextView keyOk;

    private ImageButton keyBackspace;

    private TextView tvForgotPin;


    // =========================================================
    // PIN VARIABLES
    // =========================================================

    // Current PIN being typed
    private String enteredPin = "";

    // Temporary PIN used for confirmation
    private String firstPin = "";


    // =========================================================
    // SCREEN MODES
    // =========================================================

    /*
     * MODE_LOGIN
     * Existing user enters normal PIN.
     */
    private static final int MODE_LOGIN = 0;


    /*
     * MODE_SET_PIN
     * First time user creates PIN.
     */
    private static final int MODE_SET_PIN = 1;


    /*
     * MODE_CONFIRM_PIN
     * Confirm first PIN.
     */
    private static final int MODE_CONFIRM_PIN = 2;


    /*
     * MODE_SET_RECOVERY
     * User creates recovery PIN.
     */
    private static final int MODE_SET_RECOVERY = 3;


    /*
     * MODE_CONFIRM_RECOVERY
     * User confirms recovery PIN.
     */
    private static final int MODE_CONFIRM_RECOVERY = 4;


    /*
     * MODE_RECOVERY_LOGIN
     * User enters recovery PIN after
     * clicking Forgot PIN.
     */
    private static final int MODE_RECOVERY_LOGIN = 5;


    /*
     * MODE_NEW_PIN
     * User creates a new PIN after
     * successful recovery verification.
     */
    private static final int MODE_NEW_PIN = 6;


    /*
     * MODE_CONFIRM_NEW_PIN
     * User confirms the new PIN.
     */
    private static final int MODE_CONFIRM_NEW_PIN = 7;


    // Current mode
    private int currentMode = MODE_LOGIN;


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.pin_main);

        initializeViews();

        checkFirstLaunch();

        setupKeypad();

        setupForgotPin();
    }


    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private void initializeViews() {

        tvTitle = findViewById(R.id.tv_title);

        tvSubtitle = findViewById(R.id.tv_subtitle);


        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);
        dot4 = findViewById(R.id.dot4);


        key0 = findViewById(R.id.key0);
        key1 = findViewById(R.id.key1);
        key2 = findViewById(R.id.key2);
        key3 = findViewById(R.id.key3);
        key4 = findViewById(R.id.key4);
        key5 = findViewById(R.id.key5);
        key6 = findViewById(R.id.key6);
        key7 = findViewById(R.id.key7);
        key8 = findViewById(R.id.key8);
        key9 = findViewById(R.id.key9);


        keyOk = findViewById(R.id.keyOk);

        keyBackspace = findViewById(R.id.keyBackspace);

        tvForgotPin = findViewById(R.id.tv_forgot_pin);
    }


    // =========================================================
    // CHECK FIRST LAUNCH
    // =========================================================

    private void checkFirstLaunch() {

        SharedPreferences preferences =
                getSharedPreferences(
                        PREF_NAME,
                        MODE_PRIVATE
                );


        String savedPin =
                preferences.getString(
                        KEY_PIN,
                        null
                );


        // =====================================================
        // FIRST TIME
        // =====================================================

        if (savedPin == null) {

            currentMode = MODE_SET_PIN;

            tvTitle.setText("Set PIN");

            tvSubtitle.setText(
                    "Create a 4-digit PIN"
            );


            // Hide Forgot PIN
            tvForgotPin.setVisibility(
                    View.GONE
            );

        }


        // =====================================================
        // EXISTING USER
        // =====================================================

        else {

            currentMode = MODE_LOGIN;

            tvTitle.setText("Enter PIN");

            tvSubtitle.setText(
                    "Enter your 4-digit PIN"
            );


            // Show Forgot PIN
            tvForgotPin.setVisibility(
                    View.VISIBLE
            );
        }
    }


    // =========================================================
    // KEYPAD
    // =========================================================

    private void setupKeypad() {

        key0.setOnClickListener(
                v -> addNumber("0")
        );

        key1.setOnClickListener(
                v -> addNumber("1")
        );

        key2.setOnClickListener(
                v -> addNumber("2")
        );

        key3.setOnClickListener(
                v -> addNumber("3")
        );

        key4.setOnClickListener(
                v -> addNumber("4")
        );

        key5.setOnClickListener(
                v -> addNumber("5")
        );

        key6.setOnClickListener(
                v -> addNumber("6")
        );

        key7.setOnClickListener(
                v -> addNumber("7")
        );

        key8.setOnClickListener(
                v -> addNumber("8")
        );

        key9.setOnClickListener(
                v -> addNumber("9")
        );


        // Backspace
        keyBackspace.setOnClickListener(
                v -> removeNumber()
        );


        // OK
        keyOk.setOnClickListener(
                v -> processInput()
        );
    }


    // =========================================================
    // FORGOT PIN
    // =========================================================

    private void setupForgotPin() {

        tvForgotPin.setOnClickListener(
                v -> startRecovery()
        );
    }


    // =========================================================
    // ADD NUMBER
    // =========================================================

    private void addNumber(String number) {

        // Only allow 4 digits
        if (enteredPin.length() >= 4) {
            return;
        }


        enteredPin += number;

        updateDots();


        // Automatically continue when 4 digits are entered
        if (enteredPin.length() == 4) {

            keyOk.postDelayed(
                    () -> processInput(),
                    150
            );
        }
    }


    // =========================================================
    // REMOVE NUMBER
    // =========================================================

    private void removeNumber() {

        if (enteredPin.length() > 0) {

            enteredPin =
                    enteredPin.substring(
                            0,
                            enteredPin.length() - 1
                    );

            updateDots();
        }
    }


    // =========================================================
    // UPDATE DOTS
    // =========================================================

    private void updateDots() {

        int length =
                enteredPin.length();


        // First dot
        dot1.setBackgroundResource(
                length >= 1
                        ? R.drawable.dot_filled
                        : R.drawable.dot_empty
        );


        // Second dot
        dot2.setBackgroundResource(
                length >= 2
                        ? R.drawable.dot_filled
                        : R.drawable.dot_empty
        );


        // Third dot
        dot3.setBackgroundResource(
                length >= 3
                        ? R.drawable.dot_filled
                        : R.drawable.dot_empty
        );


        // Fourth dot
        dot4.setBackgroundResource(
                length >= 4
                        ? R.drawable.dot_filled
                        : R.drawable.dot_empty
        );
    }


    // =========================================================
    // PROCESS INPUT
    // =========================================================

    private void processInput() {

        // Must contain 4 digits
        if (enteredPin.length() != 4) {

            Toast.makeText(
                    this,
                    "Please enter 4 digits.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // =====================================================
        // SET FIRST PIN
        // =====================================================

        if (currentMode == MODE_SET_PIN) {

            firstPin = enteredPin;

            enteredPin = "";

            updateDots();

            currentMode = MODE_CONFIRM_PIN;


            tvTitle.setText(
                    "Confirm PIN"
            );

            tvSubtitle.setText(
                    "Enter your PIN again"
            );

            return;
        }


        // =====================================================
        // CONFIRM FIRST PIN
        // =====================================================

        if (currentMode == MODE_CONFIRM_PIN) {

            if (enteredPin.equals(firstPin)) {

                // PIN is correct
                savePin(firstPin);

                enteredPin = "";

                firstPin = "";

                updateDots();


                // Move to recovery setup
                currentMode =
                        MODE_SET_RECOVERY;


                tvTitle.setText(
                        "Set Recovery PIN"
                );

                tvSubtitle.setText(
                        "Create a 4-digit recovery PIN"
                );

            } else {

                // PIN doesn't match
                Toast.makeText(
                        this,
                        "PINs do not match.",
                        Toast.LENGTH_SHORT
                ).show();


                enteredPin = "";

                firstPin = "";

                updateDots();


                currentMode =
                        MODE_SET_PIN;


                tvTitle.setText(
                        "Set PIN"
                );

                tvSubtitle.setText(
                        "Create a 4-digit PIN"
                );
            }

            return;
        }


        // =====================================================
        // SET RECOVERY PIN
        // =====================================================

        if (currentMode == MODE_SET_RECOVERY) {

            firstPin = enteredPin;

            enteredPin = "";

            updateDots();


            currentMode =
                    MODE_CONFIRM_RECOVERY;


            tvTitle.setText(
                    "Confirm Recovery PIN"
            );

            tvSubtitle.setText(
                    "Enter your recovery PIN again"
            );

            return;
        }


        // =====================================================
        // CONFIRM RECOVERY PIN
        // =====================================================

        if (currentMode == MODE_CONFIRM_RECOVERY) {

            if (enteredPin.equals(firstPin)) {

                saveRecoveryPin(firstPin);

                enteredPin = "";

                firstPin = "";

                updateDots();


                Toast.makeText(
                        this,
                        "PIN setup completed!",
                        Toast.LENGTH_SHORT
                ).show();


                openMainActivity();

            } else {

                Toast.makeText(
                        this,
                        "Recovery PINs do not match.",
                        Toast.LENGTH_SHORT
                ).show();


                enteredPin = "";

                firstPin = "";

                updateDots();


                currentMode =
                        MODE_SET_RECOVERY;


                tvTitle.setText(
                        "Set Recovery PIN"
                );

                tvSubtitle.setText(
                        "Create a 4-digit recovery PIN"
                );
            }

            return;
        }


        // =====================================================
        // NORMAL LOGIN
        // =====================================================

        if (currentMode == MODE_LOGIN) {

            SharedPreferences preferences =
                    getSharedPreferences(
                            PREF_NAME,
                            MODE_PRIVATE
                    );


            String savedPin =
                    preferences.getString(
                            KEY_PIN,
                            ""
                    );


            if (enteredPin.equals(savedPin)) {

                openMainActivity();

            } else {

                Toast.makeText(
                        this,
                        "Incorrect PIN.",
                        Toast.LENGTH_SHORT
                ).show();


                enteredPin = "";

                updateDots();
            }

            return;
        }


        // =====================================================
        // RECOVERY LOGIN
        // =====================================================

        if (currentMode == MODE_RECOVERY_LOGIN) {

            SharedPreferences preferences =
                    getSharedPreferences(
                            PREF_NAME,
                            MODE_PRIVATE
                    );


            String savedRecoveryPin =
                    preferences.getString(
                            KEY_RECOVERY_PIN,
                            ""
                    );


            if (enteredPin.equals(savedRecoveryPin)) {

                Toast.makeText(
                        this,
                        "Recovery successful.",
                        Toast.LENGTH_SHORT
                ).show();


                enteredPin = "";

                firstPin = "";

                updateDots();


                currentMode =
                        MODE_NEW_PIN;


                tvTitle.setText(
                        "Create New PIN"
                );

                tvSubtitle.setText(
                        "Enter a new 4-digit PIN"
                );

            } else {

                Toast.makeText(
                        this,
                        "Incorrect recovery PIN.",
                        Toast.LENGTH_SHORT
                ).show();


                enteredPin = "";

                updateDots();
            }

            return;
        }


        // =====================================================
        // SET NEW PIN
        // =====================================================

        if (currentMode == MODE_NEW_PIN) {

            firstPin = enteredPin;

            enteredPin = "";

            updateDots();


            currentMode =
                    MODE_CONFIRM_NEW_PIN;


            tvTitle.setText(
                    "Confirm New PIN"
            );

            tvSubtitle.setText(
                    "Enter your new PIN again"
            );

            return;
        }


        // =====================================================
        // CONFIRM NEW PIN
        // =====================================================

        if (currentMode == MODE_CONFIRM_NEW_PIN) {

            if (enteredPin.equals(firstPin)) {

                savePin(firstPin);


                enteredPin = "";

                firstPin = "";

                updateDots();


                Toast.makeText(
                        this,
                        "PIN changed successfully!",
                        Toast.LENGTH_SHORT
                ).show();


                openMainActivity();

            } else {

                Toast.makeText(
                        this,
                        "PINs do not match.",
                        Toast.LENGTH_SHORT
                ).show();


                enteredPin = "";

                firstPin = "";

                updateDots();


                currentMode =
                        MODE_NEW_PIN;


                tvTitle.setText(
                        "Create New PIN"
                );

                tvSubtitle.setText(
                        "Enter a new 4-digit PIN"
                );
            }
        }
    }


    // =========================================================
    // START RECOVERY
    // =========================================================

    private void startRecovery() {

        SharedPreferences preferences =
                getSharedPreferences(
                        PREF_NAME,
                        MODE_PRIVATE
                );


        String recoveryPin =
                preferences.getString(
                        KEY_RECOVERY_PIN,
                        null
                );


        // =====================================================
        // NO RECOVERY PIN
        // =====================================================

        if (recoveryPin == null) {

            Toast.makeText(
                    this,
                    "No recovery PIN has been configured.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        // =====================================================
        // RECOVERY SCREEN
        // =====================================================

        enteredPin = "";

        firstPin = "";

        updateDots();


        currentMode =
                MODE_RECOVERY_LOGIN;


        tvTitle.setText(
                "Recovery PIN"
        );

        tvSubtitle.setText(
                "Enter your 4-digit recovery PIN"
        );


        // Hide Forgot PIN
        tvForgotPin.setVisibility(
                View.GONE
        );
    }


    // =========================================================
    // SAVE MAIN PIN
    // =========================================================

    private void savePin(String pin) {

        SharedPreferences preferences =
                getSharedPreferences(
                        PREF_NAME,
                        MODE_PRIVATE
                );


        preferences
                .edit()
                .putString(
                        KEY_PIN,
                        pin
                )
                .apply();
    }


    // =========================================================
    // SAVE RECOVERY PIN
    // =========================================================

    private void saveRecoveryPin(String pin) {

        SharedPreferences preferences =
                getSharedPreferences(
                        PREF_NAME,
                        MODE_PRIVATE
                );


        preferences
                .edit()
                .putString(
                        KEY_RECOVERY_PIN,
                        pin
                )
                .apply();
    }


    // =========================================================
    // OPEN MAIN ACTIVITY
    // =========================================================

    private void openMainActivity() {

        Intent intent =
                new Intent(
                        PinActivity.this,
                        MainActivity.class
                );


        startActivity(intent);


        // Prevent returning to PIN screen
        finish();
    }


    // =========================================================
    // BACK BUTTON
    // =========================================================

    @Override
    public void onBackPressed() {

        /*
         * If the user is currently in recovery mode,
         * return to normal PIN login.
         */

        if (currentMode == MODE_RECOVERY_LOGIN) {

            enteredPin = "";

            updateDots();

            currentMode = MODE_LOGIN;


            tvTitle.setText(
                    "Enter PIN"
            );

            tvSubtitle.setText(
                    "Enter your 4-digit PIN"
            );


            tvForgotPin.setVisibility(
                    View.VISIBLE
            );

            return;
        }


        /*
         * If creating a new PIN after recovery,
         * return to recovery PIN.
         */

        if (currentMode == MODE_NEW_PIN ||
                currentMode == MODE_CONFIRM_NEW_PIN) {

            enteredPin = "";

            firstPin = "";

            updateDots();

            currentMode =
                    MODE_RECOVERY_LOGIN;


            tvTitle.setText(
                    "Recovery PIN"
            );

            tvSubtitle.setText(
                    "Enter your 4-digit recovery PIN"
            );


            tvForgotPin.setVisibility(
                    View.GONE
            );

            return;
        }


        super.onBackPressed();
    }
}
