package estor.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
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

public class AdddeptActivity extends AppCompatActivity {

    // VARIABLE
    // ListView that displays all customers.
    private ListView listCustomers;
    // Displays the number of customers.
    private TextView txtCustomerCount;
    // Stores customer information.
    private ArrayList<HashMap<String, String>> customerList;
    // Adapter for the customer ListView.
    private CustomerAdapter adapter;
    // SQLite database helper.
    private DatabaseHelper databaseHelper;
    // SMS permission request code.
    private static final int SMS_PERMISSION_CODE = 1;
    // ON CREATE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the Add Debt screen.
        setContentView(R.layout.adddept_main);
        // DATABASE
        databaseHelper = new DatabaseHelper(this);
        // FIND MAIN SCREEN VIEWS
        listCustomers = findViewById(R.id.listCustomers);
        txtCustomerCount = findViewById(R.id.txtCustomerCount);
        View overlayDim = findViewById(R.id.overlayDim);
        View addDebtSheet = findViewById(R.id.includeAddDebtSheet);
        Button btnAddDebt = findViewById(R.id.btnAddDebt);
        ImageButton btnBack = findViewById(R.id.btnBack);

        // FIND BOTTOM SHEET INPUTS
        EditText etName = addDebtSheet.findViewById(R.id.etName);
        EditText etPhone = addDebtSheet.findViewById(R.id.etPhone);
        Button btnConfirm = addDebtSheet.findViewById(R.id.btnConfirm);

        // CREATE CUSTOMER LIST
        customerList = new ArrayList<>();
        adapter = new CustomerAdapter();
        listCustomers.setAdapter(adapter);

        // CUSTOMER CLICK
        listCustomers.setOnItemClickListener(
                (parent, view, position, id) -> {
                    // Get the selected customer.
                    HashMap<String, String> customer = customerList.get(position);
                    // Get customer name.
                    String customerName = customer.get("name");
                    // Get customer phone.
                    String customerPhone = customer.get("phone");
                    // Create Intent to open Adddept2Activity.
                    Intent intent = new Intent(AdddeptActivity.this, Adddept2Activity.class);

                    // Send customer name.
                    intent.putExtra("customer_name", customerName);
                    // Send customer phone.
                    intent.putExtra("customer_phone", customerPhone);
                    // Open Adddept2Activity.
                    startActivity(intent);
                }
        );

        // LOAD CUSTOMERS
        loadCustomers();
        updateCustomerCount();

        // ADD DEBT BUTTON
        btnAddDebt.setOnClickListener(v -> {
            // Show dark background.
            overlayDim.setVisibility(View.VISIBLE);
            // Show bottom sheet.
            addDebtSheet.setVisibility(View.VISIBLE);
        });
        // DISMISS BOTTOM SHEET
        overlayDim.setOnClickListener(v -> {
            // Hide dark background.
            overlayDim.setVisibility(View.GONE);
            // Hide bottom sheet.
            addDebtSheet.setVisibility(View.GONE);
        });

        // SMS PERMISSION
        checkSmsPermission();
        // CONFIRM CUSTOMER
        btnConfirm.setOnClickListener(v -> {
            // Get customer name.
            String name = etName.getText().toString().trim();
            // Get phone number.
            String phone = etPhone.getText().toString().trim();
            // VALIDATION
            // Check if fields are empty.
            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(
                        AdddeptActivity.this,
                        "Please fill in all fields",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            // NAME VALIDATION
            if (name.length() > 20) {
                Toast.makeText(AdddeptActivity.this, "Name must not exceed 20 characters", Toast.LENGTH_SHORT).show();
                return;
            }
            // PHONE VALIDATION
            // Philippine mobile number:
            // Example: 09123456789

            if (!phone.matches("^09\\d{9}$")) {
                Toast.makeText(
                        AdddeptActivity.this,
                        "Enter a valid phone number: 09XXXXXXXXX",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }
            // SAVE CUSTOMER TO SQLITE
            long result = databaseHelper.addCustomer(name, phone);

            // CHECK IF SAVE WAS SUCCESSFUL
            if (result != -1) {
                // Customer successfully saved.
                Toast.makeText(AdddeptActivity.this, "Customer added successfully", Toast.LENGTH_SHORT).show();

                // SEND SMS
                sendSms(phone, "Hello " + name + ", your customer account has been created in estor.");

                // CLEAR INPUTS
                etName.setText("");
                etPhone.setText("");

                // REFRESH CUSTOMER LIST
                loadCustomers();
                updateCustomerCount();

                // CLOSE BOTTOM SHEET
                overlayDim.setVisibility(View.GONE);
                addDebtSheet.setVisibility(View.GONE);

            } else {
                // SQLite failed.
                Toast.makeText(AdddeptActivity.this, "Failed to save customer", Toast.LENGTH_SHORT).show();
            }
        });

        // BACK BUTTON
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(AdddeptActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    // CHECK SMS PERMISSION
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

    // SEND SMS
    private void sendSms(String phoneNumber, String message) {
        // Check if SMS permission is available.
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
            // Send the SMS.
            smsManager.sendTextMessage(
                    phoneNumber,
                    null,
                    message,
                    null,
                    null
            );

            // Tell the user the SMS was sent.
            Toast.makeText(
                    AdddeptActivity.this,
                    "SMS Sent!",
                    Toast.LENGTH_SHORT
            ).show();


        } catch (Exception e) {
            // Display error if SMS fails.
            Toast.makeText(
                    AdddeptActivity.this,
                    "Failed to send SMS",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // LOAD CUSTOMERS
    private void loadCustomers() {
        // Remove old data.
        customerList.clear();
        // Get all customers from SQLite.
        Cursor cursor = databaseHelper.getAllCustomers();
        // Check if cursor exists.
        if (cursor != null) {
            // Get name column.
            int nameIndex =
                    cursor.getColumnIndex(
                            DatabaseHelper.COLUMN_NAME
                    );


            // Get phone column.
            int phoneIndex =
                    cursor.getColumnIndex(
                            DatabaseHelper.COLUMN_PHONE
                    );


            // Read each customer.
            while (cursor.moveToNext()) {
                // Get customer name.
                String name = cursor.getString(nameIndex);
                // Get customer phone.
                String phone = cursor.getString(phoneIndex);
                // Create customer object.
                HashMap<String, String> customer = new HashMap<>();
                // Store name.
                customer.put("name", name);
                // Store phone.
                customer.put("phone", phone);
                // Add customer to list.
                customerList.add(customer);
            }
            // Close cursor.
            cursor.close();
        }
        // Tell adapter that the data changed.
        adapter.notifyDataSetChanged();
    }

    // UPDATE CUSTOMER COUNT
    private void updateCustomerCount() {
        // Get number of customers.
        int count = databaseHelper.getCustomerCount();
        // Display number.
        txtCustomerCount.setText(String.valueOf(count)
        );
    }
    // CUSTOMER ADAPTER
    private class CustomerAdapter extends ArrayAdapter<HashMap<String, String>> {
        // Constructor.
        CustomerAdapter() {
            super(
                    AdddeptActivity.this,
                    R.layout.customer_item,
                    customerList
            );
        }

        // GET VIEW
        @Override
        public View getView(
                int position,
                View convertView,
                ViewGroup parent
        ) {
            // Create row if needed.
            if (convertView == null) {

                convertView =
                        getLayoutInflater().inflate(
                                R.layout.customer_item,
                                parent,
                                false
                        );
            }
            // Find customer name TextView.
            TextView txtName =
                    convertView.findViewById(R.id.txtCustomerName
                    );
            // Find customer phone TextView.
            TextView txtPhone =
                    convertView.findViewById(R.id.txtCustomerPhone
                    );
            // Get customer.
            HashMap<String, String> customer = customerList.get(position);
            // Display customer name.
            txtName.setText(
                    customer.get("name")
            );
            // Display customer phone.
            txtPhone.setText(customer.get("phone")
            );
            // Return completed row.
            return convertView;
        }
    }
}