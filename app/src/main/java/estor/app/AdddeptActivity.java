package estor.app;


// ============================================================
// IMPORTS
// ============================================================

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


// ============================================================
// ACTIVITY
// ============================================================

public class AdddeptActivity extends AppCompatActivity {


    // ========================================================
    // VARIABLES
    // ========================================================

    // Customer ListView
    ListView listCustomers;

    // Displays number of customers
    TextView txtCustomerCount;

    // Main Add Customer button
    Button btnAddDebt;

    // Back button
    ImageButton btnBack;

    // Database
    DatabaseHelper databaseHelper;

    // Customer data
    ArrayList<HashMap<String, String>> customerList;

    // List adapter
    CustomerAdapter adapter;

    // Error message inside bottom sheet
    TextView txtError;


    // ========================================================
    // SMS
    // ========================================================

    private static final int SMS_PERMISSION_CODE = 1;


    // ========================================================
    // ERROR HANDLER
    // ========================================================

    private final Handler errorHandler =
            new Handler(Looper.getMainLooper());

    private Runnable hideErrorRunnable;

    private static final long ERROR_DISPLAY_DURATION_MS = 3000;


    // ========================================================
    // ON CREATE
    // ========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        // Open Add Debt screen
        setContentView(R.layout.adddept_main);


        // ====================================================
        // DATABASE
        // ====================================================

        databaseHelper =
                new DatabaseHelper(this);


        // ====================================================
        // FIND MAIN VIEWS
        // ====================================================

        listCustomers =
                findViewById(R.id.listCustomers);

        txtCustomerCount =
                findViewById(R.id.txtCustomerCount);

        btnAddDebt =
                findViewById(R.id.btnAddDebt);

        btnBack =
                findViewById(R.id.btnBack);


        // ====================================================
        // BOTTOM SHEET
        // ====================================================

        View overlayDim =
                findViewById(R.id.overlayDim);

        View addDebtSheet =
                findViewById(R.id.includeAddDebtSheet);


        // ====================================================
        // BOTTOM SHEET INPUTS
        // ====================================================

        EditText etName =
                addDebtSheet.findViewById(
                        R.id.etName
                );

        EditText etPhone =
                addDebtSheet.findViewById(
                        R.id.etPhone
                );

        Button btnConfirm =
                addDebtSheet.findViewById(
                        R.id.btnConfirm
                );

        txtError =
                addDebtSheet.findViewById(
                        R.id.txtError
                );


        // ====================================================
        // CUSTOMER LIST
        // ====================================================

        customerList =
                new ArrayList<>();


        // Create adapter
        adapter =
                new CustomerAdapter();


        // Connect adapter
        listCustomers.setAdapter(adapter);


        // Load customers
        loadCustomers();


        // Update customer count
        updateCustomerCount();


        // ====================================================
        // CUSTOMER CLICK
        // ====================================================

        listCustomers.setOnItemClickListener(
                (parent, view, position, id) -> {


                    // Get selected customer
                    HashMap<String, String> customer =
                            customerList.get(position);


                    // =================================================
                    // GET CUSTOMER ID
                    // =================================================

                    String customerId =
                            customer.get("id");


                    // =================================================
                    // GET CUSTOMER NAME
                    // =================================================

                    String name =
                            customer.get("name");


                    // =================================================
                    // GET CUSTOMER PHONE
                    // =================================================

                    String phone =
                            customer.get("phone");


                    // =================================================
                    // OPEN ADDDEPT2ACTIVITY
                    // =================================================

                    Intent intent =
                            new Intent(
                                    AdddeptActivity.this,
                                    Adddept2Activity.class
                            );


                    // Send customer ID
                    intent.putExtra(
                            "customer_id",
                            customerId
                    );


                    // Send customer name
                    intent.putExtra(
                            "customer_name",
                            name
                    );


                    // Send customer phone
                    intent.putExtra(
                            "customer_phone",
                            phone
                    );


                    // Open screen
                    startActivity(intent);
                }
        );


        // ====================================================
        // ADD CUSTOMER BUTTON
        // ====================================================

        btnAddDebt.setOnClickListener(v -> {


            // Show dark background
            overlayDim.setVisibility(
                    View.VISIBLE
            );


            // Show bottom sheet
            addDebtSheet.setVisibility(
                    View.VISIBLE
            );


            // Hide previous error
            hideError();


            // Focus name
            etName.requestFocus();
        });


        // ====================================================
        // CLOSE BOTTOM SHEET
        // ====================================================

        overlayDim.setOnClickListener(v -> {


            // Hide background
            overlayDim.setVisibility(
                    View.GONE
            );


            // Hide sheet
            addDebtSheet.setVisibility(
                    View.GONE
            );


            // Hide error
            hideError();
        });


        // ====================================================
        // SMS PERMISSION
        // ====================================================

        checkSmsPermission();


        // ====================================================
        // CONFIRM CUSTOMER
        // ====================================================

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


            // =================================================
            // EMPTY CHECK
            // =================================================

            if (name.isEmpty() ||
                    phone.isEmpty()) {

                showError(
                        "Please fill in all fields"
                );

                return;
            }


            // =================================================
            // NAME LENGTH
            // =================================================

            if (name.length() > 20) {

                showError(
                        "Name must not exceed 20 characters"
                );

                return;
            }


            // =================================================
            // PHONE VALIDATION
            // =================================================

            // Philippine format:
            //
            // 09123456789

            if (!phone.matches(
                    "^09\\d{9}$"
            )) {

                showError(
                        "Enter a valid phone number"
                );

                return;
            }


            // =================================================
            // SAVE CUSTOMER
            // =================================================

            long result =
                    databaseHelper.addCustomer(
                            name,
                            phone
                    );


            // =================================================
            // CHECK RESULT
            // =================================================

            if (result != -1) {


                // Customer successfully saved
                Toast.makeText(
                        AdddeptActivity.this,
                        "Customer added successfully",
                        Toast.LENGTH_SHORT
                ).show();


                // =================================================
                // SEND SMS
                // =================================================

                sendSms(
                        phone,

                        "Hello " +
                                name +
                                ", your customer account has been created in estor."
                );


                // =================================================
                // CLEAR INPUTS
                // =================================================

                etName.setText("");

                etPhone.setText("");


                // =================================================
                // REFRESH CUSTOMER LIST
                // =================================================

                loadCustomers();

                updateCustomerCount();


                // Hide error
                hideError();


                // =================================================
                // CLOSE BOTTOM SHEET
                // =================================================

                overlayDim.setVisibility(
                        View.GONE
                );

                addDebtSheet.setVisibility(
                        View.GONE
                );


            } else {


                // Database failed
                showError(
                        "Failed to save customer"
                );
            }
        });


        // ====================================================
        // BACK BUTTON
        // ====================================================

        btnBack.setOnClickListener(v -> {

            // Simply close this Activity.
            //
            // This returns to MainActivity instead of
            // creating another MainActivity.

            finish();
        });
    }


    // ========================================================
    // ON RESUME
    // ========================================================

    @Override
    protected void onResume() {

        super.onResume();


        // Refresh customers whenever we return
        // from Adddept2Activity.

        if (databaseHelper != null) {

            loadCustomers();

            updateCustomerCount();
        }
    }


    // ========================================================
    // SHOW ERROR
    // ========================================================

    private void showError(String message) {


        // Cancel previous timer
        if (hideErrorRunnable != null) {

            errorHandler.removeCallbacks(
                    hideErrorRunnable
            );
        }


        // Show error
        txtError.setText(message);

        txtError.setVisibility(
                View.VISIBLE
        );


        // Automatically hide after 3 seconds
        hideErrorRunnable =
                () -> txtError.setVisibility(
                        View.GONE
                );


        errorHandler.postDelayed(
                hideErrorRunnable,
                ERROR_DISPLAY_DURATION_MS
        );
    }


    // ========================================================
    // HIDE ERROR
    // ========================================================

    private void hideError() {


        // Cancel timer
        if (hideErrorRunnable != null) {

            errorHandler.removeCallbacks(
                    hideErrorRunnable
            );
        }


        // Hide error
        txtError.setVisibility(
                View.GONE
        );
    }


    // ========================================================
    // CHECK SMS PERMISSION
    // ========================================================

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


    // ========================================================
    // SEND SMS
    // ========================================================

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


            // Get SMS Manager
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


    // ========================================================
    // LOAD CUSTOMERS
    // ========================================================

    private void loadCustomers() {


        // Remove old data
        customerList.clear();


        // Get customers
        Cursor cursor =
                databaseHelper.getAllCustomers();


        // Check cursor
        if (cursor != null) {


            // =================================================
            // GET COLUMN INDEXES
            // =================================================

            int idIndex =
                    cursor.getColumnIndex(
                            DatabaseHelper.COLUMN_ID
                    );


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


            // =================================================
            // READ CUSTOMERS
            // =================================================

            while (cursor.moveToNext()) {


                // ---------------------------------------------
                // CUSTOMER ID
                // ---------------------------------------------

                String id = "";

                if (idIndex != -1 &&
                        !cursor.isNull(idIndex)) {

                    id =
                            cursor.getString(
                                    idIndex
                            );
                }


                // ---------------------------------------------
                // NAME
                // ---------------------------------------------

                String name = "";

                if (nameIndex != -1 &&
                        !cursor.isNull(nameIndex)) {

                    name =
                            cursor.getString(
                                    nameIndex
                            );
                }


                // ---------------------------------------------
                // PHONE
                // ---------------------------------------------

                String phone = "";

                if (phoneIndex != -1 &&
                        !cursor.isNull(phoneIndex)) {

                    phone =
                            cursor.getString(
                                    phoneIndex
                            );
                }


                // ---------------------------------------------
                // DATE
                // ---------------------------------------------

                String date = "";

                if (dateIndex != -1 &&
                        !cursor.isNull(dateIndex)) {

                    date =
                            cursor.getString(
                                    dateIndex
                            );
                }


                // ---------------------------------------------
                // TIME
                // ---------------------------------------------

                String time = "";

                if (timeIndex != -1 &&
                        !cursor.isNull(timeIndex)) {

                    time =
                            cursor.getString(
                                    timeIndex
                            );
                }


                // =================================================
                // CREATE CUSTOMER MAP
                // =================================================

                HashMap<String, String> customer =
                        new HashMap<>();


                customer.put(
                        "id",
                        id
                );


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


                // Add customer
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


    // ========================================================
    // UPDATE CUSTOMER COUNT
    // ========================================================

    private void updateCustomerCount() {


        // Get customer count
        int count =
                databaseHelper.getCustomerCount();


        // Show count
        txtCustomerCount.setText(
                String.valueOf(count)
        );
    }


    // ========================================================
    // DESTROY
    // ========================================================

    @Override
    protected void onDestroy() {

        super.onDestroy();


        // Cancel error timer
        if (hideErrorRunnable != null) {

            errorHandler.removeCallbacks(
                    hideErrorRunnable
            );
        }
    }


    // ========================================================
    // CUSTOMER ADAPTER
    // ========================================================

    private class CustomerAdapter
            extends ArrayAdapter<HashMap<String, String>> {


        // ====================================================
        // CONSTRUCTOR
        // ====================================================

        CustomerAdapter() {

            super(
                    AdddeptActivity.this,

                    R.layout.item_customer,

                    customerList
            );
        }


        // ====================================================
        // GET VIEW
        // ====================================================

        @Override
        public View getView(
                int position,
                View convertView,
                ViewGroup parent
        ) {


            // Create row if necessary
            if (convertView == null) {

                convertView =
                        getLayoutInflater()
                                .inflate(
                                        R.layout.item_customer,
                                        parent,
                                        false
                                );
            }


            // =================================================
            // FIND TEXTVIEWS
            // =================================================

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


            // =================================================
            // GET CUSTOMER
            // =================================================

            HashMap<String, String> customer =
                    customerList.get(position);


            // =================================================
            // DISPLAY CUSTOMER
            // =================================================

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


            return convertView;
        }
    }
}