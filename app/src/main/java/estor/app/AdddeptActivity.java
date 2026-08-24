package estor.app;


// ================= IMPORTS =================

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;


// ================= ACTIVITY =================

public class AdddeptActivity extends AppCompatActivity {


    // ================= VARIABLES =================

    ListView listCustomers;

    TextView txtCustomerCount;

    Button btnAddDebt;

    ImageButton btnBack;

    DatabaseHelper databaseHelper;

    ArrayList<HashMap<String, String>> customerList;

    CustomerAdapter adapter;

    // NEW: Error message shown above the Add Debt bottom sheet inputs.
    TextView txtError;


    // SMS permission code
    private static final int SMS_PERMISSION_CODE = 1;

    // NEW: Handles auto-dismissing the error message after a delay.
    private final Handler errorHandler = new Handler(Looper.getMainLooper());

    // NEW: Hides the error message when it runs.
    private Runnable hideErrorRunnable;

    // NEW: How long the error message stays visible.
    private static final long ERROR_DISPLAY_DURATION_MS = 3000;


    // ================= ON CREATE =================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Open the Add Debt screen
        setContentView(R.layout.adddept_main);


        // ================= DATABASE =================

        databaseHelper = new DatabaseHelper(this);


        // ================= FIND VIEWS =================

        listCustomers =
                findViewById(R.id.listCustomers);

        txtCustomerCount =
                findViewById(R.id.txtCustomerCount);

        btnAddDebt =
                findViewById(R.id.btnAddDebt);

        btnBack =
                findViewById(R.id.btnBack);


        // Bottom sheet
        View overlayDim =
                findViewById(R.id.overlayDim);

        View addDebtSheet =
                findViewById(R.id.includeAddDebtSheet);


        // Inputs inside bottom sheet
        EditText etName =
                addDebtSheet.findViewById(R.id.etName);

        EditText etPhone =
                addDebtSheet.findViewById(R.id.etPhone);

        Button btnConfirm =
                addDebtSheet.findViewById(R.id.btnConfirm);

        // NEW: Error message shown above the inputs.
        txtError =
                addDebtSheet.findViewById(R.id.txtError);


        // ================= CUSTOMER LIST =================

        customerList =
                new ArrayList<>();


        adapter =
                new CustomerAdapter();


        listCustomers.setAdapter(adapter);


        // Load customers from database
        loadCustomers();


        // Show customer count
        updateCustomerCount();


        // ================= CLICK CUSTOMER =================

        listCustomers.setOnItemClickListener(
                (parent, view, position, id) -> {

                    // Get selected customer
                    HashMap<String, String> customer =
                            customerList.get(position);


                    // Get name
                    String name =
                            customer.get("name");


                    // Get phone
                    String phone =
                            customer.get("phone");


                    // Open Adddept2Activity
                    Intent intent =
                            new Intent(
                                    AdddeptActivity.this,
                                    Adddept2Activity.class
                            );


                    // Send name
                    intent.putExtra(
                            "customer_name",
                            name
                    );


                    // Send phone
                    intent.putExtra(
                            "customer_phone",
                            phone
                    );


                    // Open next screen
                    startActivity(intent);
                }
        );


        // ================= ADD DEBT BUTTON =================

        btnAddDebt.setOnClickListener(v -> {

            // Show dark background
            overlayDim.setVisibility(
                    View.VISIBLE
            );


            // Show bottom sheet
            addDebtSheet.setVisibility(
                    View.VISIBLE
            );

            // NEW: Hide any leftover error message from a previous attempt.
            hideError();
        });


        // ================= CLOSE BOTTOM SHEET =================

        overlayDim.setOnClickListener(v -> {

            // Hide dark background
            overlayDim.setVisibility(
                    View.GONE
            );


            // Hide bottom sheet
            addDebtSheet.setVisibility(
                    View.GONE
            );

            // NEW: Hide the error message so it doesn't linger next time.
            hideError();
        });


        // ================= SMS PERMISSION =================

        checkSmsPermission();


        // ================= CONFIRM BUTTON =================

        btnConfirm.setOnClickListener(v -> {

            // Get name
            String name =
                    etName.getText()
                            .toString()
                            .trim();


            // Get phone
            String phone =
                    etPhone.getText()
                            .toString()
                            .trim();


            // ================= CHECK EMPTY =================

            if (name.isEmpty() ||
                    phone.isEmpty()) {

                showError("Please fill in all fields");

                return;
            }


            // ================= CHECK NAME =================

            if (name.length() > 20) {

                showError("Name must not exceed 20 characters");

                return;
            }


            // ================= CHECK PHONE =================

            // Philippine number example:
            // 09123456789

            if (!phone.matches(
                    "^09\\d{9}$"
            )) {

                showError("Enter a valid phone number");

                return;
            }


            // ================= SAVE CUSTOMER =================

            long result =
                    databaseHelper.addCustomer(
                            name,
                            phone
                    );


            // ================= CHECK RESULT =================

            if (result != -1) {

                // Customer saved
                Toast.makeText(
                        AdddeptActivity.this,
                        "Customer added successfully",
                        Toast.LENGTH_SHORT
                ).show();


                // ================= SEND SMS =================

                sendSms(
                        phone,
                        "Hello " + name +
                                ", your customer account has been created in estor."
                );


                // ================= CLEAR INPUTS =================

                etName.setText("");

                etPhone.setText("");


                // ================= REFRESH LIST =================

                loadCustomers();

                updateCustomerCount();


                // NEW: Hide the error message.
                hideError();


                // ================= CLOSE SHEET =================

                overlayDim.setVisibility(
                        View.GONE
                );

                addDebtSheet.setVisibility(
                        View.GONE
                );

            } else {

                // Database failed
                showError("Failed to save customer");
            }

        });


        // ================= BACK BUTTON =================

        btnBack.setOnClickListener(v -> {

            // Open MainActivity
            Intent intent =
                    new Intent(
                            AdddeptActivity.this,
                            MainActivity.class
                    );

            startActivity(intent);


            // Close this Activity
            finish();
        });
    }


    // =====================================================
    // NEW: SHOW ERROR MESSAGE WITH AUTO-DISMISS TIMER
    // =====================================================

    private void showError(String message) {

        // Cancel any previously scheduled hide so timers don't stack.
        if (hideErrorRunnable != null) {
            errorHandler.removeCallbacks(hideErrorRunnable);
        }

        txtError.setText(message);
        txtError.setVisibility(View.VISIBLE);

        // Schedule the message to hide itself after the delay.
        hideErrorRunnable = () -> txtError.setVisibility(View.GONE);
        errorHandler.postDelayed(hideErrorRunnable, ERROR_DISPLAY_DURATION_MS);
    }


    // =====================================================
    // NEW: HIDE ERROR MESSAGE IMMEDIATELY
    // =====================================================

    private void hideError() {

        // Cancel any pending auto-hide since we're hiding it now.
        if (hideErrorRunnable != null) {
            errorHandler.removeCallbacks(hideErrorRunnable);
        }

        txtError.setVisibility(View.GONE);
    }


    // =====================================================
    // CHECK SMS PERMISSION
    // =====================================================

    private void checkSmsPermission() {

        if (checkSelfPermission(
                Manifest.permission.SEND_SMS
        ) != PackageManager.PERMISSION_GRANTED) {


            requestPermissions(
                    new String[]{
                            Manifest.permission.SEND_SMS
                    },

                    SMS_PERMISSION_CODE
            );
        }
    }


    // =====================================================
    // SEND SMS
    // =====================================================

    private void sendSms(
            String phoneNumber,
            String message
    ) {

        // Check permission
        if (checkSelfPermission(
                Manifest.permission.SEND_SMS
        ) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(
                    AdddeptActivity.this,
                    "SMS permission not granted",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        try {

            // Get SMS manager
            SmsManager smsManager =
                    SmsManager.getDefault();


            // Send SMS
            smsManager.sendTextMessage(
                    phoneNumber,
                    null,
                    message,
                    null,
                    null
            );


            // Show message
            Toast.makeText(
                    AdddeptActivity.this,
                    "SMS Sent!",
                    Toast.LENGTH_SHORT
            ).show();


        } catch (Exception e) {

            Toast.makeText(
                    AdddeptActivity.this,
                    "Failed to send SMS",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }


    // =====================================================
    // LOAD CUSTOMERS
    // =====================================================

    private void loadCustomers() {

        // Remove old list data
        customerList.clear();


        // Get database data
        Cursor cursor =
                databaseHelper.getAllCustomers();


        // Make sure cursor exists
        if (cursor != null) {


            // Get column positions

            int nameIndex =
                    cursor.getColumnIndex(
                            DatabaseHelper.COLUMN_NAME
                    );


            int phoneIndex =
                    cursor.getColumnIndex(
                            DatabaseHelper.COLUMN_PHONE
                    );


            int dateIndex =
                    cursor.getColumnIndex(
                            DatabaseHelper.COLUMN_DATE
                    );


            int timeIndex =
                    cursor.getColumnIndex(
                            DatabaseHelper.COLUMN_TIME
                    );


            // Read every customer
            while (cursor.moveToNext()) {


                // Get name
                String name =
                        cursor.getString(
                                nameIndex
                        );


                // Get phone
                String phone =
                        cursor.getString(
                                phoneIndex
                        );


                // Get date
                String date = "";

                if (dateIndex != -1 &&
                        !cursor.isNull(dateIndex)) {

                    date =
                            cursor.getString(
                                    dateIndex
                            );
                }


                // Get time
                String time = "";

                if (timeIndex != -1 &&
                        !cursor.isNull(timeIndex)) {

                    time =
                            cursor.getString(
                                    timeIndex
                            );
                }


                // Create customer
                HashMap<String, String> customer =
                        new HashMap<>();


                // Save values
                customer.put(
                        "name",
                        name
                );

                customer.put(
                        "phone",
                        phone
                );

                customer.put(
                        "date",
                        date
                );

                customer.put(
                        "time",
                        time
                );


                // Add customer to list
                customerList.add(
                        customer
                );
            }


            // Close cursor
            cursor.close();
        }


        // Refresh ListView
        adapter.notifyDataSetChanged();
    }


    // =====================================================
    // UPDATE CUSTOMER COUNT
    // =====================================================

    private void updateCustomerCount() {

        // Get number of customers
        int count =
                databaseHelper.getCustomerCount();


        // Display number
        txtCustomerCount.setText(
                String.valueOf(count)
        );
    }


    // =====================================================
    // NEW: CLEAN UP TIMER ON DESTROY
    // =====================================================

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Cancel any pending auto-hide callback to avoid leaking the Activity.
        if (hideErrorRunnable != null) {
            errorHandler.removeCallbacks(hideErrorRunnable);
        }
    }


    // =====================================================
    // CUSTOMER ADAPTER
    // =====================================================

    private class CustomerAdapter
            extends ArrayAdapter<HashMap<String, String>> {


        // ================= CONSTRUCTOR =================

        CustomerAdapter() {

            super(
                    AdddeptActivity.this,

                    // XML row
                    R.layout.item_customer,

                    // Customer data
                    customerList
            );
        }


        // ================= GET VIEW =================

        @Override
        public View getView(
                int position,
                View convertView,
                ViewGroup parent
        ) {


            // Create row
            if (convertView == null) {

                convertView =
                        getLayoutInflater()
                                .inflate(
                                        R.layout.item_customer,
                                        parent,
                                        false
                                );
            }


            // ================= FIND TEXTVIEWS =================

            TextView txtName =
                    convertView.findViewById(
                            R.id.txtCustomerName
                    );


            TextView txtPhone =
                    convertView.findViewById(
                            R.id.txtCustomerPhone
                    );


            TextView txtDate =
                    convertView.findViewById(
                            R.id.txtDate
                    );


            TextView txtTime =
                    convertView.findViewById(
                            R.id.txtTime
                    );


            // ================= GET CUSTOMER =================

            HashMap<String, String> customer =
                    customerList.get(position);


            // ================= SHOW DATA =================

            txtName.setText(
                    customer.get("name")
            );


            txtPhone.setText(
                    customer.get("phone")
            );


            txtDate.setText(
                    customer.get("date")
            );


            txtTime.setText(
                    customer.get("time")
            );


            // Return row
            return convertView;
        }
    }
}