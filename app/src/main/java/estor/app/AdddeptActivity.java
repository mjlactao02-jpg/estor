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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class AdddeptActivity
        extends AppCompatActivity {


    // =========================================================
    // VIEWS
    // =========================================================

    private ListView listCustomers;

    private TextView txtCustomerCount;

    private Button btnAddDebt;

    private ImageButton btnBack;


    // =========================================================
    // DATABASE
    // =========================================================

    private DatabaseHelper databaseHelper;


    // =========================================================
    // CUSTOMER LIST
    // =========================================================

    private ArrayList<HashMap<String, String>>
            customerList;


    private CustomerAdapter adapter;


    // =========================================================
    // ERROR
    // =========================================================

    private TextView txtError;


    private final Handler errorHandler =
            new Handler(
                    Looper.getMainLooper()
            );


    private Runnable hideErrorRunnable;


    private static final long
            ERROR_DISPLAY_DURATION_MS = 3000;


    // =========================================================
    // SMS
    // =========================================================

    private static final int SMS_PERMISSION_CODE = 1;


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);


        // =====================================================
        // OPEN LAYOUT
        // =====================================================

        setContentView(
                R.layout.adddept_main
        );


        // =====================================================
        // DATABASE
        // =====================================================

        databaseHelper =
                new DatabaseHelper(this);


        // =====================================================
        // MAIN VIEWS
        // =====================================================

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


        // =====================================================
        // BOTTOM SHEET
        // =====================================================

        View overlayDim =
                findViewById(
                        R.id.overlayDim
                );


        View addDebtSheet =
                findViewById(
                        R.id.includeAddDebtSheet
                );


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


        // =====================================================
        // CUSTOMER LIST
        // =====================================================

        customerList =
                new ArrayList<>();


        adapter =
                new CustomerAdapter();


        listCustomers.setAdapter(
                adapter
        );


        // Load all customers.
        loadCustomers();


        // Update customer count.
        updateCustomerCount();


        // =====================================================
        // NORMAL CLICK
        // =====================================================
        //
        // Normal tap still opens Adddept2Activity.
        //
        // =====================================================

        listCustomers.setOnItemClickListener(
                (parent, view, position, id) -> {

                    HashMap<String, String>
                            customer =
                            customerList.get(
                                    position
                            );


                    // Customer ID
                    String customerId =
                            customer.get(
                                    "id"
                            );


                    // Customer name
                    String name =
                            customer.get(
                                    "name"
                            );


                    // Customer phone
                    String phone =
                            customer.get(
                                    "phone"
                            );


                    // Open Adddept2Activity
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


                    startActivity(
                            intent
                    );
                }
        );


        // =====================================================
        // LONG PRESS
        // =====================================================
        //
        // Long pressing a customer shows the delete popup.
        //
        // =====================================================

        listCustomers.setOnItemLongClickListener(
                (parent, view, position, id) -> {

                    // Get selected customer.
                    HashMap<String, String>
                            customer =
                            customerList.get(
                                    position
                            );


                    // Get customer ID.
                    String customerId =
                            customer.get(
                                    "id"
                            );


                    // Get customer name.
                    String customerName =
                            customer.get(
                                    "name"
                            );


                    // Show delete dialog.
                    showDeleteCustomerDialog(
                            customerId,
                            customerName
                    );


                    // Tell ListView that the long press
                    // has already been handled.
                    return true;
                }
        );


        // =====================================================
        // ADD CUSTOMER BUTTON
        // =====================================================

        btnAddDebt.setOnClickListener(v -> {

            // Show dark overlay.
            overlayDim.setVisibility(
                    View.VISIBLE
            );


            // Show bottom sheet.
            addDebtSheet.setVisibility(
                    View.VISIBLE
            );


            // Hide previous error.
            hideError();


            // Focus name input.
            etName.requestFocus();
        });


        // =====================================================
        // CLOSE BOTTOM SHEET
        // =====================================================

        overlayDim.setOnClickListener(v -> {

            // Hide overlay.
            overlayDim.setVisibility(
                    View.GONE
            );


            // Hide sheet.
            addDebtSheet.setVisibility(
                    View.GONE
            );


            // Hide error.
            hideError();
        });


        // =====================================================
        // SMS PERMISSION
        // =====================================================

        checkSmsPermission();


        // =====================================================
        // CONFIRM CUSTOMER
        // =====================================================

        btnConfirm.setOnClickListener(v -> {

            // Get name.
            String name =
                    etName.getText()
                            .toString()
                            .trim();


            // Get phone.
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
            // NAME VALIDATION
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
            // SUCCESS
            // =================================================

            if (result != -1) {

                Toast.makeText(
                        AdddeptActivity.this,
                        "Customer added successfully",
                        Toast.LENGTH_SHORT
                ).show();


                // Send SMS.
                sendSms(
                        phone,

                        "Hello " +
                                name +
                                ", your customer account has been created in estor."
                );


                // Clear inputs.
                etName.setText("");

                etPhone.setText("");


                // Refresh ListView.
                loadCustomers();


                // Refresh count.
                updateCustomerCount();


                // Hide error.
                hideError();


                // Hide overlay.
                overlayDim.setVisibility(
                        View.GONE
                );


                // Hide bottom sheet.
                addDebtSheet.setVisibility(
                        View.GONE
                );


            } else {

                Toast.makeText(
                        AdddeptActivity.this,
                        "Failed to save customer",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });


        // =====================================================
        // BACK BUTTON
        // =====================================================

        btnBack.setOnClickListener(v -> {

            finish();

        });
    }


    // =========================================================
    // ON RESUME
    // =========================================================

    @Override
    protected void onResume() {

        super.onResume();


        if (databaseHelper != null) {

            // Refresh after returning from Adddept2Activity.
            loadCustomers();

            updateCustomerCount();
        }
    }


    // =========================================================
    // LOAD ALL CUSTOMERS
    // =========================================================
    //
    // Important:
    //
    // Add Debt shows ALL registered customers.
    //
    // A customer with ₱0 remaining is still visible here.
    //
    // =========================================================

    private void loadCustomers() {

        // Remove old data.
        customerList.clear();


        // Get all customers.
        Cursor cursor =
                databaseHelper.getAllCustomers();


        if (cursor != null) {

            // Column indexes.
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


            // Read every customer.
            while (cursor.moveToNext()) {

                String id = "";

                String name = "";

                String phone = "";

                String date = "";

                String time = "";


                // Customer ID.
                if (idIndex != -1 &&
                        !cursor.isNull(idIndex)) {

                    id =
                            cursor.getString(
                                    idIndex
                            );
                }


                // Customer name.
                if (nameIndex != -1 &&
                        !cursor.isNull(nameIndex)) {

                    name =
                            cursor.getString(
                                    nameIndex
                            );
                }


                // Customer phone.
                if (phoneIndex != -1 &&
                        !cursor.isNull(phoneIndex)) {

                    phone =
                            cursor.getString(
                                    phoneIndex
                            );
                }


                // Date.
                if (dateIndex != -1 &&
                        !cursor.isNull(dateIndex)) {

                    date =
                            cursor.getString(
                                    dateIndex
                            );
                }


                // Time.
                if (timeIndex != -1 &&
                        !cursor.isNull(timeIndex)) {

                    time =
                            cursor.getString(
                                    timeIndex
                            );
                }


                // Create customer map.
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


                // Add customer to ListView.
                customerList.add(
                        customer
                );
            }


            // Close cursor.
            cursor.close();
        }


        // Refresh ListView.
        adapter.notifyDataSetChanged();
    }


    // =========================================================
    // DELETE CUSTOMER DIALOG
    // =========================================================
    //
    // Called when the user long presses a customer.
    //
    // =========================================================

    private void showDeleteCustomerDialog(
            String customerId,
            String customerName
    ) {

        // Create confirmation popup.
        new AlertDialog.Builder(
                AdddeptActivity.this
        )

                // ---------------------------------------------
                // TITLE
                // ---------------------------------------------

                .setTitle(
                        "Delete Customer"
                )


                // ---------------------------------------------
                // MESSAGE
                // ---------------------------------------------

                .setMessage(
                        "Are you sure you want to delete " +
                                customerName +
                                "?\n\n" +

                                "This will also delete all debt " +
                                "records belonging to this customer."
                )


                // ---------------------------------------------
                // CANCEL
                // ---------------------------------------------

                .setNegativeButton(
                        "Cancel",
                        null
                )


                // ---------------------------------------------
                // DELETE
                // ---------------------------------------------

                .setPositiveButton(
                        "Delete",
                        (dialog, which) -> {

                            deleteCustomer(
                                    customerId,
                                    customerName
                            );
                        }
                )


                // Show dialog.
                .show();
    }


    // =========================================================
    // DELETE CUSTOMER
    // =========================================================

    private void deleteCustomer(
            String customerId,
            String customerName
    ) {

        // Make sure ID exists.
        if (customerId == null ||
                customerId.trim().isEmpty()) {

            Toast.makeText(
                    AdddeptActivity.this,
                    "Invalid customer ID",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        try {

            // Convert ID to integer.
            int id =
                    Integer.parseInt(
                            customerId
                    );


            // Delete from database.
            boolean deleted =
                    databaseHelper.deleteCustomer(
                            id
                    );


            // =================================================
            // DELETE SUCCESS
            // =================================================

            if (deleted) {

                Toast.makeText(
                        AdddeptActivity.this,
                        customerName +
                                " deleted successfully",
                        Toast.LENGTH_SHORT
                ).show();


                // Refresh customer list.
                loadCustomers();


                // Refresh customer count.
                updateCustomerCount();


            } else {

                Toast.makeText(
                        AdddeptActivity.this,
                        "Failed to delete customer",
                        Toast.LENGTH_SHORT
                ).show();
            }


        } catch (NumberFormatException e) {

            Toast.makeText(
                    AdddeptActivity.this,
                    "Invalid customer ID",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }


    // =========================================================
    // UPDATE CUSTOMER COUNT
    // =========================================================

    private void updateCustomerCount() {

        // Add Debt count = number of customers visible.
        txtCustomerCount.setText(
                String.valueOf(
                        customerList.size()
                )
        );
    }


    // =========================================================
    // SHOW ERROR
    // =========================================================

    private void showError(
            String message
    ) {

        // Cancel old timer.
        if (hideErrorRunnable != null) {

            errorHandler.removeCallbacks(
                    hideErrorRunnable
            );
        }


        // Set message.
        txtError.setText(
                message
        );


        // Show error.
        txtError.setVisibility(
                View.VISIBLE
        );


        // Create hide action.
        hideErrorRunnable =
                () -> txtError.setVisibility(
                        View.GONE
                );


        // Hide after 3 seconds.
        errorHandler.postDelayed(
                hideErrorRunnable,
                ERROR_DISPLAY_DURATION_MS
        );
    }


    // =========================================================
    // HIDE ERROR
    // =========================================================

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


    // =========================================================
    // CHECK SMS PERMISSION
    // =========================================================

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


    // =========================================================
    // SEND SMS
    // =========================================================

    private void sendSms(
            String phoneNumber,
            String message
    ) {

        // Check permission.
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


            // Send SMS.
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


    // =========================================================
    // DESTROY
    // =========================================================

    @Override
    protected void onDestroy() {

        super.onDestroy();


        if (hideErrorRunnable != null) {

            errorHandler.removeCallbacks(
                    hideErrorRunnable
            );
        }
    }


    // =========================================================
    // CUSTOMER ADAPTER
    // =========================================================

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

            // Create row if needed.
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

            HashMap<String, String>
                    customer =
                    customerList.get(
                            position
                    );


            // =================================================
            // DISPLAY CUSTOMER
            // =================================================

            txtName.setText(
                    customer.get(
                            "name"
                    )
            );


            txtPhone.setText(
                    customer.get(
                            "phone"
                    )
            );


            txtDate.setText(
                    customer.get(
                            "date"
                    )
            );


            txtTime.setText(
                    customer.get(
                            "time"
                    )
            );


            return convertView;
        }
    }
}