package estor.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Locale;

public class Adddept2Activity extends AppCompatActivity {

    // =====================================================
    // INPUT FIELDS
    // =====================================================

    // Product name
    private EditText edtProduct;

    // Product quantity
    private EditText edtQuantity;

    // Product amount / price
    private EditText edtAmount;


    // =====================================================
    // BUTTONS
    // =====================================================

    // Adds an item temporarily to the ListView
    private Button btnAddDebt;

    // Saves all items to the database
    private Button btnConfirm;

    // Back button
    private ImageButton btnBack;


    // =====================================================
    // LISTVIEW
    // =====================================================

    private ListView listViewDebt;


    // =====================================================
    // CUSTOMER CARD
    // =====================================================

    private TextView txtCustomerName;
    private TextView txtCustomerPhone;
    private TextView txtTotalAmount;


    // =====================================================
    // DATABASE
    // =====================================================

    private DatabaseHelper databaseHelper;


    // =====================================================
    // CUSTOMER INFORMATION
    // =====================================================

    private String customerName;
    private String customerPhone;


    // =====================================================
    // TEMPORARY DEBT LIST
    // =====================================================

    private ArrayList<DebtItem> debtItems;

    private DebtAdapter adapter;


    // =====================================================
    // SMS PERMISSION
    // =====================================================

    private static final int SMS_PERMISSION_CODE = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Open Add Debt Item layout
        setContentView(R.layout.adddept);


        // =====================================================
        // DATABASE
        // =====================================================

        databaseHelper = new DatabaseHelper(this);


        // =====================================================
        // GET CUSTOMER INFORMATION
        // =====================================================

        customerName =
                getIntent().getStringExtra("customer_name");

        customerPhone =
                getIntent().getStringExtra("customer_phone");


        // =====================================================
        // CONNECT INPUT FIELDS
        // =====================================================

        edtProduct =
                findViewById(R.id.edtProduct);

        edtQuantity =
                findViewById(R.id.edtQuantity);

        edtAmount =
                findViewById(R.id.edtAmount);


        // =====================================================
        // CONNECT BUTTONS
        // =====================================================

        btnAddDebt =
                findViewById(R.id.btnAddDebt);

        btnConfirm =
                findViewById(R.id.btnConfirm);

        btnBack =
                findViewById(R.id.btnBack);


        // =====================================================
        // CONNECT LISTVIEW
        // =====================================================

        listViewDebt =
                findViewById(R.id.listViewDebt);


        // =====================================================
        // CONNECT CUSTOMER CARD
        // =====================================================

        txtCustomerName =
                findViewById(R.id.txtCustomerName);

        txtCustomerPhone =
                findViewById(R.id.txtCustomerPhone);

        txtTotalAmount =
                findViewById(R.id.txtTotalAmount);


        // =====================================================
        // DISPLAY CUSTOMER INFORMATION
        // =====================================================

        if (customerName != null) {

            txtCustomerName.setText(customerName);

        } else {

            txtCustomerName.setText("Unknown");
        }


        if (customerPhone != null) {

            txtCustomerPhone.setText(customerPhone);

        } else {

            txtCustomerPhone.setText("");
        }


        // =====================================================
        // CREATE EMPTY DEBT LIST
        // =====================================================

        debtItems =
                new ArrayList<>();


        // =====================================================
        // CREATE ADAPTER
        // =====================================================

        adapter =
                new DebtAdapter(
                        Adddept2Activity.this,
                        debtItems
                );


        // Connect adapter to ListView
        listViewDebt.setAdapter(adapter);


        // =====================================================
        // INITIAL TOTAL
        // =====================================================

        updateTotalDisplay();


        // =====================================================
        // SMS PERMISSION
        // =====================================================

        checkSmsPermission();


        // =====================================================
        // BACK BUTTON
        // =====================================================

        btnBack.setOnClickListener(v -> {

            finish();

        });


        // =====================================================
        // ADD DEBT BUTTON
        // =====================================================

        btnAddDebt.setOnClickListener(v -> {

            addDebtItem();

        });


        // =====================================================
        // CONFIRM BUTTON
        // =====================================================

        btnConfirm.setOnClickListener(v -> {

            confirmDebt();

        });
    }


    // =========================================================
    // ADD DEBT ITEM TO LISTVIEW
    // =========================================================

    private void addDebtItem() {

        // Get product
        String product =
                edtProduct.getText()
                        .toString()
                        .trim();


        // Get quantity
        String quantityText =
                edtQuantity.getText()
                        .toString()
                        .trim();


        // Get amount
        String amountText =
                edtAmount.getText()
                        .toString()
                        .trim();


        // =====================================================
        // VALIDATE PRODUCT
        // =====================================================

        if (product.isEmpty()) {

            edtProduct.setError("Enter product");

            edtProduct.requestFocus();

            return;
        }


        // =====================================================
        // VALIDATE QUANTITY
        // =====================================================

        if (quantityText.isEmpty()) {

            edtQuantity.setError("Enter quantity");

            edtQuantity.requestFocus();

            return;
        }


        // =====================================================
        // VALIDATE AMOUNT
        // =====================================================

        if (amountText.isEmpty()) {

            edtAmount.setError("Enter amount");

            edtAmount.requestFocus();

            return;
        }


        // =====================================================
        // CONVERT QUANTITY
        // =====================================================

        int quantity;

        try {

            quantity =
                    Integer.parseInt(quantityText);

        } catch (NumberFormatException e) {

            edtQuantity.setError(
                    "Enter a valid quantity"
            );

            edtQuantity.requestFocus();

            return;
        }


        // Quantity must be greater than zero
        if (quantity <= 0) {

            edtQuantity.setError(
                    "Quantity must be greater than 0"
            );

            edtQuantity.requestFocus();

            return;
        }


        // =====================================================
        // CONVERT AMOUNT
        // =====================================================

        double amount;

        try {

            amount =
                    Double.parseDouble(amountText);

        } catch (NumberFormatException e) {

            edtAmount.setError(
                    "Enter a valid amount"
            );

            edtAmount.requestFocus();

            return;
        }


        // Amount must be greater than zero
        if (amount <= 0) {

            edtAmount.setError(
                    "Amount must be greater than 0"
            );

            edtAmount.requestFocus();

            return;
        }


        // =====================================================
        // CREATE DEBT ITEM
        // =====================================================

        DebtItem item =
                new DebtItem(
                        product,
                        quantity,
                        amount
                );


        // Add item to temporary list
        debtItems.add(item);


        // Refresh ListView
        adapter.notifyDataSetChanged();


        // Refresh total
        updateTotalDisplay();


        // =====================================================
        // CLEAR INPUT FIELDS
        // =====================================================

        edtProduct.setText("");

        edtQuantity.setText("");

        edtAmount.setText("");


        // Focus product field
        edtProduct.requestFocus();


        Toast.makeText(
                Adddept2Activity.this,
                "Debt item added",
                Toast.LENGTH_SHORT
        ).show();
    }


    // =========================================================
    // UPDATE TOTAL DISPLAY
    // =========================================================

    private void updateTotalDisplay() {

        double total = 0.0;


        // Calculate every item's total
        for (DebtItem item : debtItems) {

            double lineTotal =
                    item.getQuantity()
                            * item.getAmount();

            total += lineTotal;
        }


        // Display total
        txtTotalAmount.setText(
                String.format(
                        Locale.getDefault(),
                        "₱ %.2f",
                        total
                )
        );
    }


    // =========================================================
    // CONFIRM ALL DEBT ITEMS
    // =========================================================

    private void confirmDebt() {

        // =====================================================
        // CHECK CUSTOMER PHONE
        // =====================================================

        if (customerPhone == null ||
                customerPhone.trim().isEmpty()) {

            Toast.makeText(
                    Adddept2Activity.this,
                    "Customer information is missing",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // =====================================================
        // CHECK DEBT ITEMS
        // =====================================================

        if (debtItems.isEmpty()) {

            Toast.makeText(
                    Adddept2Activity.this,
                    "Add at least one debt item",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // =====================================================
        // OPEN DATABASE
        // =====================================================

        SQLiteDatabase db =
                databaseHelper.getReadableDatabase();


        // =====================================================
        // FIND CUSTOMER ID
        // =====================================================

        long customerId = -1;


        Cursor cursor =
                db.query(
                        DatabaseHelper.TABLE_CUSTOMERS,

                        new String[]{
                                DatabaseHelper.COLUMN_ID
                        },

                        DatabaseHelper.COLUMN_PHONE + "=?",

                        new String[]{
                                customerPhone
                        },

                        null,
                        null,
                        null
                );


        if (cursor != null) {

            if (cursor.moveToFirst()) {

                customerId =
                        cursor.getLong(
                                cursor.getColumnIndexOrThrow(
                                        DatabaseHelper.COLUMN_ID
                                )
                        );
            }

            cursor.close();
        }


        // =====================================================
        // CUSTOMER NOT FOUND
        // =====================================================

        if (customerId == -1) {

            Toast.makeText(
                    Adddept2Activity.this,
                    "Customer was not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // =====================================================
        // SAVE ALL ITEMS
        // =====================================================

        boolean saveSuccessful = true;


        try {

            for (DebtItem item : debtItems) {

                // -------------------------------------------------
                // IMPORTANT
                // -------------------------------------------------
                // amount entered = price per item
                //
                // Example:
                //
                // Quantity = 3
                // Amount = 20
                //
                // Debt saved = 60
                // -------------------------------------------------

                double lineTotal =
                        item.getQuantity()
                                * item.getAmount();


                // Save to DatabaseHelper
                long result =
                        databaseHelper.insertDebt(
                                (int) customerId,

                                customerName,

                                customerPhone,

                                item.getProduct(),

                                lineTotal
                        );


                // Check if insertion failed
                if (result == -1) {

                    saveSuccessful = false;

                    break;
                }
            }


        } catch (Exception e) {

            saveSuccessful = false;

            Toast.makeText(
                    Adddept2Activity.this,
                    "Failed to save debt: "
                            + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }


        // =====================================================
        // CHECK SAVE RESULT
        // =====================================================

        if (!saveSuccessful) {

            Toast.makeText(
                    Adddept2Activity.this,
                    "Failed to save debt",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        // =====================================================
        // SUCCESS
        // =====================================================

        Toast.makeText(
                Adddept2Activity.this,
                "Debt saved successfully",
                Toast.LENGTH_SHORT
        ).show();


        // =====================================================
        // SEND SMS
        // =====================================================

        sendDebtSms(
                customerPhone,
                debtItems
        );


        // =====================================================
        // CLEAR LIST
        // =====================================================

        debtItems.clear();

        adapter.notifyDataSetChanged();

        updateTotalDisplay();


        // =====================================================
        // RETURN TO PREVIOUS SCREEN
        // =====================================================

        finish();
    }


    // =========================================================
    // SMS PERMISSION
    // =========================================================

    private void checkSmsPermission() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,

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

    private void sendDebtSms(
            String phoneNumber,
            ArrayList<DebtItem> items
    ) {

        if (phoneNumber == null ||
                phoneNumber.trim().isEmpty()) {

            return;
        }


        // =====================================================
        // CHECK SMS PERMISSION
        // =====================================================

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
        ) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(
                    this,
                    "SMS permission not granted",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // =====================================================
        // CREATE MESSAGE
        // =====================================================

        StringBuilder message =
                new StringBuilder();


        message.append("Hello ")
                .append(
                        customerName != null
                                ? customerName
                                : ""
                )
                .append(
                        ", here is your debt summary:\n\n"
                );


        double total = 0.0;


        // =====================================================
        // ADD PRODUCTS TO SMS
        // =====================================================

        for (DebtItem item : items) {

            double lineTotal =
                    item.getQuantity()
                            * item.getAmount();


            total += lineTotal;


            message.append(
                            item.getProduct()
                    )
                    .append(" x")
                    .append(item.getQuantity())
                    .append(" - ₱")
                    .append(
                            String.format(
                                    Locale.getDefault(),
                                    "%.2f",
                                    lineTotal
                            )
                    )
                    .append("\n");
        }


        // =====================================================
        // ADD TOTAL
        // =====================================================

        message.append("\nTotal Debt: ₱")
                .append(
                        String.format(
                                Locale.getDefault(),
                                "%.2f",
                                total
                        )
                );


        // =====================================================
        // SEND SMS
        // =====================================================

        try {

            SmsManager smsManager =
                    SmsManager.getDefault();


            // Split long messages
            ArrayList<String> parts =
                    smsManager.divideMessage(
                            message.toString()
                    );


            smsManager.sendMultipartTextMessage(
                    phoneNumber,
                    null,
                    parts,
                    null,
                    null
            );


            Toast.makeText(
                    this,
                    "SMS Sent!",
                    Toast.LENGTH_SHORT
            ).show();


        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Failed to send SMS",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }


    // =========================================================
    // DEBT ITEM CLASS
    // =========================================================

    private static class DebtItem {

        private String product;

        private int quantity;

        private double amount;


        DebtItem(
                String product,
                int quantity,
                double amount
        ) {

            this.product = product;

            this.quantity = quantity;

            this.amount = amount;
        }


        String getProduct() {

            return product;
        }


        int getQuantity() {

            return quantity;
        }


        double getAmount() {

            return amount;
        }
    }


    // =========================================================
    // LISTVIEW ADAPTER
    // =========================================================

    private static class DebtAdapter
            extends BaseAdapter {

        private final android.content.Context context;

        private final ArrayList<DebtItem> debtItems;


        DebtAdapter(
                android.content.Context context,
                ArrayList<DebtItem> debtItems
        ) {

            this.context = context;

            this.debtItems = debtItems;
        }


        @Override
        public int getCount() {

            return debtItems.size();
        }


        @Override
        public Object getItem(int position) {

            return debtItems.get(position);
        }


        @Override
        public long getItemId(int position) {

            return position;
        }


        @Override
        public View getView(
                int position,
                View convertView,
                ViewGroup parent
        ) {

            // Create the ListView item
            if (convertView == null) {

                convertView =
                        LayoutInflater
                                .from(context)
                                .inflate(
                                        R.layout.dept_item,
                                        parent,
                                        false
                                );
            }


            // =================================================
            // CONNECT TEXTVIEWS
            // =================================================

            TextView txtProduct =
                    convertView.findViewById(
                            R.id.txtProduct
                    );

            TextView txtQuantity =
                    convertView.findViewById(
                            R.id.txtQuantity
                    );

            TextView txtAmount =
                    convertView.findViewById(
                            R.id.txtAmount
                    );


            // Get item
            DebtItem item =
                    debtItems.get(position);


            // Product
            txtProduct.setText(
                    item.getProduct()
            );


            // Quantity
            txtQuantity.setText(
                    "Qty: " +
                            item.getQuantity()
            );


            // Price
            txtAmount.setText(
                    "₱ " +
                            String.format(
                                    Locale.getDefault(),
                                    "%.2f",
                                    item.getAmount()
                            )
            );


            return convertView;
        }
    }
}