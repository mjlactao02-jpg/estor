package estor.app;

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

    ListView listCustomers;

    TextView txtCustomerCount;

    Button btnAddDebt;

    ImageButton btnBack;

    DatabaseHelper databaseHelper;

    ArrayList<HashMap<String, String>> customerList;

    CustomerAdapter adapter;

    TextView txtError;


    // ========================================================
    // SMS
    // ========================================================

    private static final int SMS_PERMISSION_CODE = 1;


    // ========================================================
    // ERROR HANDLER
    // ========================================================

    private final Handler errorHandler =
            new Handler(
                    Looper.getMainLooper()
            );

    private Runnable hideErrorRunnable;

    private static final long
            ERROR_DISPLAY_DURATION_MS = 3000;


    // ========================================================
    // ON CREATE
    // ========================================================

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);


        setContentView(
                R.layout.adddept_main
        );


        // ====================================================
        // DATABASE
        // ====================================================

        databaseHelper =
                new DatabaseHelper(this);


        // ====================================================
        // MAIN VIEWS
        // ====================================================

        listCustomers =
                findViewById(
                        R.id.listCustomers
                );


        txtCustomerCount =
                findViewById(
                        R.id.txtCustomerCount
                );


        btnAddDebt =
                findViewById(
                        R.id.btnAddDebt
                );


        btnBack =
                findViewById(
                        R.id.btnBack
                );


        // ====================================================
        // BOTTOM SHEET
        // ====================================================

        View overlayDim =
                findViewById(
                        R.id.overlayDim
                );


        View addDebtSheet =
                findViewById(
                        R.id.includeAddDebtSheet
                );


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


        adapter =
                new CustomerAdapter();


        listCustomers.setAdapter(
                adapter
        );


        loadCustomers();


        updateCustomerCount();


        // ====================================================
        // CUSTOMER CLICK
        // ====================================================

        listCustomers.setOnItemClickListener(
                (parent, view, position, id) -> {


                    HashMap<String, String>
                            customer =
                            customerList.get(
                                    position
                            );


                    String customerId =
                            customer.get("id");


                    String name =
                            customer.get("name");


                    String phone =
                            customer.get("phone");


                    Intent intent =
                            new Intent(
                                    AdddeptActivity.this,
                                    Adddept2Activity.class
                            );


                    intent.putExtra(
                            "customer_id",
                            customerId
                    );


                    intent.putExtra(
                            "customer_name",
                            name
                    );


                    intent.putExtra(
                            "customer_phone",
                            phone
                    );


                    startActivity(intent);
                }
        );


        // ====================================================
        // ADD CUSTOMER BUTTON
        // ====================================================

        btnAddDebt.setOnClickListener(v -> {


            overlayDim.setVisibility(
                    View.VISIBLE
            );


            addDebtSheet.setVisibility(
                    View.VISIBLE
            );


            hideError();


            etName.requestFocus();
        });


        // ====================================================
        // CLOSE BOTTOM SHEET
        // ====================================================

        overlayDim.setOnClickListener(v -> {


            overlayDim.setVisibility(
                    View.GONE
            );


            addDebtSheet.setVisibility(
                    View.GONE
            );


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


            String name =
                    etName.getText()
                            .toString()
                            .trim();


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
                // CLEAR INPUT
                // =================================================

                etName.setText("");

                etPhone.setText("");


                // =================================================
                // REFRESH LIST
                // =================================================

                loadCustomers();

                updateCustomerCount();


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

                showError(
                        "Failed to save customer"
                );
            }
        });


        // ====================================================
        // BACK BUTTON
        // ====================================================

        btnBack.setOnClickListener(v -> {

            finish();

        });
    }


    // ========================================================
    // ON RESUME
    // ========================================================

    @Override
    protected void onResume() {

        super.onResume();


        if (databaseHelper != null) {

            loadCustomers();

            updateCustomerCount();
        }
    }


    // ========================================================
    // SHOW ERROR
    // ========================================================

    private void showError(
            String message
    ) {

        if (hideErrorRunnable != null) {

            errorHandler.removeCallbacks(
                    hideErrorRunnable
            );
        }


        txtError.setText(
                message
        );


        txtError.setVisibility(
                View.VISIBLE
        );


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

        if (hideErrorRunnable != null) {

            errorHandler.removeCallbacks(
                    hideErrorRunnable
            );
        }


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

            SmsManager smsManager =
                    SmsManager.getDefault();


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

        customerList.clear();


        Cursor cursor =
                databaseHelper.getAllCustomers();


        if (cursor != null) {

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


            while (cursor.moveToNext()) {


                String id = "";

                String name = "";

                String phone = "";

                String date = "";

                String time = "";


                if (idIndex != -1 &&
                        !cursor.isNull(idIndex)) {

                    id =
                            cursor.getString(
                                    idIndex
                            );
                }


                if (nameIndex != -1 &&
                        !cursor.isNull(nameIndex)) {

                    name =
                            cursor.getString(
                                    nameIndex
                            );
                }


                if (phoneIndex != -1 &&
                        !cursor.isNull(phoneIndex)) {

                    phone =
                            cursor.getString(
                                    phoneIndex
                            );
                }


                if (dateIndex != -1 &&
                        !cursor.isNull(dateIndex)) {

                    date =
                            cursor.getString(
                                    dateIndex
                            );
                }


                if (timeIndex != -1 &&
                        !cursor.isNull(timeIndex)) {

                    time =
                            cursor.getString(
                                    timeIndex
                            );
                }


                HashMap<String, String>
                        customer =
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


                customerList.add(
                        customer
                );
            }


            cursor.close();
        }


        adapter.notifyDataSetChanged();
    }


    // ========================================================
    // UPDATE CUSTOMER COUNT
    // ========================================================

    private void updateCustomerCount() {

        int count =
                databaseHelper.getCustomerCount();


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


        CustomerAdapter() {

            super(
                    AdddeptActivity.this,
                    R.layout.item_customer,
                    customerList
            );
        }


        @Override
        public View getView(
                int position,
                View convertView,
                ViewGroup parent
        ) {

            if (convertView == null) {

                convertView =
                        getLayoutInflater()
                                .inflate(
                                        R.layout.item_customer,
                                        parent,
                                        false
                                );
            }


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


            HashMap<String, String>
                    customer =
                    customerList.get(
                            position
                    );


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