package estor.app;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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

    // VARIABLES
    // ListView that displays all customers.
    private ListView listCustomers;
    // Displays the number of customers.
    private TextView txtCustomerCount;
    // Stores customer information.
    private ArrayList<HashMap<String, String>> customerList;
    // Adapter for the ListView.
    private CustomerAdapter adapter;
    // SQLite database helper.
    private DatabaseHelper databaseHelper;

    // ON CREATE

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the Add Debt customer screen.
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
        // Adddept2Activity will ONLY open
        // when the user clicks a customer.

        listCustomers.setOnItemClickListener(
                (parent, view, position, id) -> {
                    // Get the customer that was clicked.
                    HashMap<String, String> customer = customerList.get(position);

                    // Get customer name.
                    String customerName = customer.get("name");

                    // Get customer phone.
                    String customerPhone = customer.get("phone");

                    // Create Intent.
                    Intent intent = new Intent(AdddeptActivity.this, Adddept2Activity.class);

                    // Send customer name.
                    intent.putExtra("customer_name", customerName);

                    // Send customer phone.
                    intent.putExtra("customer_phone", customerPhone);

                    // Open Adddept2Activity.
                    startActivity(intent);
                });

        // LOAD CUSTOMERS

        loadCustomers();
        updateCustomerCount();

        // ADD DEBT BUTTON
        btnAddDebt.setOnClickListener(v -> {
            // Show dark overlay.
            overlayDim.setVisibility(View.VISIBLE);
            // Show bottom sheet.
            addDebtSheet.setVisibility(View.VISIBLE);
        });

        // DISMISS BOTTOM SHEET

        overlayDim.setOnClickListener(v -> {
            // Hide overlay.
            overlayDim.setVisibility(View.GONE);
            // Hide bottom sheet.
            addDebtSheet.setVisibility(View.GONE);
        });


        // CONFIRM CUSTOMER
        btnConfirm.setOnClickListener(v -> {
            // Get customer name.
            String name = etName.getText().toString().trim();
            // Get phone number.
            String phone = etPhone.getText().toString().trim();


            // VALIDATION
            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(
                        AdddeptActivity.this,
                        "Please fill in both fields",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }


            // Check name length.
            if (name.length() > 20) {

                Toast.makeText(
                        AdddeptActivity.this,
                        "Name is too long",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }


            // Check phone length.
            if (phone.length() > 11) {

                Toast.makeText(
                        AdddeptActivity.this,
                        "Phone number is too long",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }


            // SAVE CUSTOMER
            long result = databaseHelper.addCustomer(name, phone,);

            // SUCCESS

            if (result != -1) {
                Toast.makeText(
                        AdddeptActivity.this,
                        "Customer added successfully",
                        Toast.LENGTH_SHORT
                ).show();
                // Clear name.
                etName.setText("");
                // Clear phone.
                etPhone.setText("");
                // Reload customer list.
                loadCustomers();
                // Update customer count.
                updateCustomerCount();
                // Hide overlay.
                overlayDim.setVisibility(View.GONE);
                // Hide bottom sheet.
                addDebtSheet.setVisibility(View.GONE);
            } else {

                // FAILED
                Toast.makeText(
                        AdddeptActivity.this,
                        "Failed to save customer",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // BACK BUTTON
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(AdddeptActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    // LOAD CUSTOMERS
    private void loadCustomers() {
        // Clear old list.
        customerList.clear();
        // Get customers from SQLite.
        Cursor cursor = databaseHelper.getAllCustomers();
        // Check cursor.
        if (cursor != null) {
            // Get name column.
            int nameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME);
            // Get phone column.
            int phoneIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PHONE);
            // Read every customer.
            while (cursor.moveToNext()) {
                // Get customer name.
                String name = cursor.getString(nameIndex);
                // Get customer phone.
                String phone = cursor.getString(phoneIndex);
                // Create customer object.
                HashMap<String, String> customer = new HashMap<>();
                // Save name.
                customer.put("name", name);
                // Save phone.
                customer.put("phone", phone);
                // Add to list.
                customerList.add(customer);
            }
            // Close cursor.
            cursor.close();
        }
        // Tell adapter that data changed.
        adapter.notifyDataSetChanged();
    }
    // UPDATE CUSTOMER COUNT
    private void updateCustomerCount() {
        // Get number of customers.
        int count = databaseHelper.getCustomerCount();
        // Display customer count.
        txtCustomerCount.setText(String.valueOf(count)
        );
    }
    // CUSTOMER ADAPTER

    private class CustomerAdapter extends ArrayAdapter<HashMap<String, String>> {
        // CONSTRUCTOR
        CustomerAdapter() {
            super(AdddeptActivity.this,
                    R.layout.customer_item,
                    customerList
            );
        }
        // GET VIEW
        @Override
        public View getView(
                int position,
                View convertView,
                ViewGroup parent) {
            // Create a new row if needed.
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.customer_item, parent, false);
            }
            // Find customer name TextView.
            TextView txtName = convertView.findViewById(R.id.txtCustomerName);
            // Find customer phone TextView.
            TextView txtPhone = convertView.findViewById(R.id.txtCustomerPhone);
            // Get customer at this position.
            HashMap<String, String> customer = customerList.get(position);
            // Display name.
            txtName.setText(customer.get("name"));
            // Display phone.
            txtPhone.setText(customer.get("phone"));
            // Return completed row.
            return convertView;
        }
    }
}