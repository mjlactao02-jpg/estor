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

    private EditText edtProduct;

    private EditText edtQuantity;

    private EditText edtAmount;


    // =====================================================
    // BUTTONS
    // =====================================================

    private Button btnAddDebt;

    private Button btnConfirm;

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

    private String customerId;

    private String customerName;

    private String customerPhone;


    // =====================================================
    // TEMPORARY DEBT LIST
    // =====================================================

    private ArrayList<DebtItem> debtItems;

    private DebtAdapter adapter;


    // =====================================================
    // SMS
    // =====================================================

    private static final int SMS_PERMISSION_CODE = 2;


    // =====================================================
    // ON CREATE
    // =====================================================

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.adddept
        );


        // =====================================================
        // DATABASE
        // =====================================================

        databaseHelper =
                new DatabaseHelper(this);


        // =====================================================
        // GET CUSTOMER INFORMATION
        // =====================================================

        customerId =
                getIntent()
                        .getStringExtra(
                                "customer_id"
                        );

        customerName =
                getIntent()
                        .getStringExtra(
                                "customer_name"
                        );

        customerPhone =
                getIntent()
                        .getStringExtra(
                                "customer_phone"
                        );


        // =====================================================
        // INPUT FIELDS
        // =====================================================

        edtProduct =
                findViewById(
                        R.id.edtProduct
                );

        edtQuantity =
                findViewById(
                        R.id.edtQuantity
                );

        edtAmount =
                findViewById(
                        R.id.edtAmount
                );


        // =====================================================
        // BUTTONS
        // =====================================================

        btnAddDebt =
                findViewById(
                        R.id.btnAddDebt
                );

        btnConfirm =
                findViewById(
                        R.id.btnConfirm
                );

        btnBack =
                findViewById(
                        R.id.btnBack
                );


        // =====================================================
        // LISTVIEW
        // =====================================================

        listViewDebt =
                findViewById(
                        R.id.listViewDebt
                );


        // =====================================================
        // CUSTOMER CARD
        // =====================================================

        txtCustomerName =
                findViewById(
                        R.id.txtCustomerName
                );

        txtCustomerPhone =
                findViewById(
                        R.id.txtCustomerPhone
                );

        txtTotalAmount =
                findViewById(
                        R.id.txtTotalAmount
                );


        // =====================================================
        // DISPLAY CUSTOMER
        // =====================================================

        if (customerName != null) {

            txtCustomerName.setText(
                    customerName
            );

        } else {

            txtCustomerName.setText(
                    "Unknown"
            );
        }


        if (customerPhone != null) {

            txtCustomerPhone.setText(
                    customerPhone
            );

        } else {

            txtCustomerPhone.setText(
                    ""
            );
        }


        // =====================================================
        // CREATE TEMPORARY LIST
        // =====================================================

        debtItems =
                new ArrayList<>();


        adapter =
                new DebtAdapter(
                        Adddept2Activity.this,
                        debtItems
                );


        listViewDebt.setAdapter(
                adapter
        );


        updateTotalDisplay();


        // =====================================================
        // SMS PERMISSION
        // =====================================================

        checkSmsPermission();


        // =====================================================
        // BACK
        // =====================================================

        btnBack.setOnClickListener(v -> {

            finish();

        });


        // =====================================================
        // ADD ITEM
        // =====================================================

        btnAddDebt.setOnClickListener(v -> {

            addDebtItem();

        });


        // =====================================================
        // CONFIRM
        // =====================================================

        btnConfirm.setOnClickListener(v -> {

            confirmDebt();

        });
    }


    // =========================================================
    // ADD DEBT ITEM
    // =========================================================

    private void addDebtItem() {

        String product =
                edtProduct.getText()
                        .toString()
                        .trim();


        String quantityText =
                edtQuantity.getText()
                        .toString()
                        .trim();


        String amountText =
                edtAmount.getText()
                        .toString()
                        .trim();


        // =====================================================
        // PRODUCT
        // =====================================================

        if (product.isEmpty()) {

            edtProduct.setError(
                    "Enter product"
            );

            edtProduct.requestFocus();

            return;
        }


        // =====================================================
        // QUANTITY
        // =====================================================

        if (quantityText.isEmpty()) {

            edtQuantity.setError(
                    "Enter quantity"
            );

            edtQuantity.requestFocus();

            return;
        }


        // =====================================================
        // AMOUNT
        // =====================================================

        if (amountText.isEmpty()) {

            edtAmount.setError(
                    "Enter amount"
            );

            edtAmount.requestFocus();

            return;
        }


        // =====================================================
        // QUANTITY CONVERSION
        // =====================================================

        int quantity;


        try {

            quantity =
                    Integer.parseInt(
                            quantityText
                    );

        } catch (NumberFormatException e) {

            edtQuantity.setError(
                    "Enter a valid quantity"
            );

            edtQuantity.requestFocus();

            return;
        }


        if (quantity <= 0) {

            edtQuantity.setError(
                    "Quantity must be greater than 0"
            );

            edtQuantity.requestFocus();

            return;
        }


        // =====================================================
        // AMOUNT CONVERSION
        // =====================================================

        double amount;


        try {

            amount =
                    Double.parseDouble(
                            amountText
                    );

        } catch (NumberFormatException e) {

            edtAmount.setError(
                    "Enter a valid amount"
            );

            edtAmount.requestFocus();

            return;
        }


        if (amount <= 0) {

            edtAmount.setError(
                    "Amount must be greater than 0"
            );

            edtAmount.requestFocus();

            return;
        }


        // =====================================================
        // CREATE TEMPORARY ITEM
        // =====================================================

        DebtItem item =
                new DebtItem(
                        product,
                        quantity,
                        amount
                );


        debtItems.add(
                item
        );


        adapter.notifyDataSetChanged();


        updateTotalDisplay();


        // =====================================================
        // CLEAR INPUT
        // =====================================================

        edtProduct.setText("");

        edtQuantity.setText("");

        edtAmount.setText("");


        edtProduct.requestFocus();


        Toast.makeText(
                Adddept2Activity.this,
                "Debt item added",
                Toast.LENGTH_SHORT
        ).show();
    }


    // =========================================================
    // UPDATE TOTAL
    // =========================================================

    private void updateTotalDisplay() {

        double total = 0.0;


        for (DebtItem item : debtItems) {

            double lineTotal =
                    item.getQuantity()
                            * item.getAmount();


            total += lineTotal;
        }


        txtTotalAmount.setText(
                String.format(
                        Locale.getDefault(),
                        "₱ %.2f",
                        total
                )
        );
    }


    // =========================================================
    // CONFIRM DEBT
    // =========================================================

    private void confirmDebt() {

        // =====================================================
        // CUSTOMER PHONE
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
        // DEBT ITEMS
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
        // DATABASE
        // =====================================================

        SQLiteDatabase db =
                databaseHelper
                        .getReadableDatabase();


        long foundCustomerId = -1;


        // =====================================================
        // FIRST USE CUSTOMER ID
        // =====================================================

        if (customerId != null &&
                !customerId.trim().isEmpty()) {

            try {

                foundCustomerId =
                        Long.parseLong(
                                customerId
                        );

            } catch (NumberFormatException ignored) {

                foundCustomerId = -1;
            }
        }


        // =====================================================
        // FALLBACK BY PHONE
        // =====================================================

        if (foundCustomerId == -1) {

            Cursor cursor =
                    db.query(

                            DatabaseHelper.TABLE_CUSTOMERS,

                            new String[]{
                                    DatabaseHelper.COLUMN_ID
                            },

                            DatabaseHelper.COLUMN_PHONE +
                                    "=?",

                            new String[]{
                                    customerPhone
                            },

                            null,
                            null,
                            null
                    );


            if (cursor != null) {

                if (cursor.moveToFirst()) {

                    foundCustomerId =
                            cursor.getLong(
                                    cursor.getColumnIndexOrThrow(
                                            DatabaseHelper.COLUMN_ID
                                    )
                            );
                }

                cursor.close();
            }
        }


        // =====================================================
        // CUSTOMER NOT FOUND
        // =====================================================

        if (foundCustomerId == -1) {

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

                // Quantity x price
                double lineTotal =
                        item.getQuantity()
                                * item.getAmount();


                long result =
                        databaseHelper.insertDebt(

                                (int) foundCustomerId,

                                customerName,

                                customerPhone,

                                item.getProduct(),

                                item.getQuantity(),

                                lineTotal
                        );


                if (result == -1) {

                    saveSuccessful = false;

                    break;
                }
            }


        } catch (Exception e) {

            saveSuccessful = false;


            Toast.makeText(
                    Adddept2Activity.this,
                    "Failed to save debt: " +
                            e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }


        // =====================================================
        // SAVE FAILED
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
        // SMS
        // =====================================================

        sendDebtSms(
                customerPhone,
                debtItems
        );


        // =====================================================
        // CLEAR
        // =====================================================

        debtItems.clear();

        adapter.notifyDataSetChanged();

        updateTotalDisplay();


        // =====================================================
        // RETURN
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


        for (DebtItem item : items) {

            double lineTotal =
                    item.getQuantity()
                            * item.getAmount();


            total += lineTotal;


            message.append(
                            item.getProduct()
                    )
                    .append(" x")
                    .append(
                            item.getQuantity()
                    )
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


        message.append(
                        "\nTotal Debt: ₱"
                )
                .append(
                        String.format(
                                Locale.getDefault(),
                                "%.2f",
                                total
                        )
                );


        try {

            SmsManager smsManager =
                    SmsManager.getDefault();


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
    // ADAPTER
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
        public Object getItem(
                int position
        ) {

            return debtItems.get(
                    position
            );
        }


        @Override
        public long getItemId(
                int position
        ) {

            return position;
        }


        @Override
        public View getView(
                int position,
                View convertView,
                ViewGroup parent
        ) {

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


            DebtItem item =
                    debtItems.get(
                            position
                    );


            txtProduct.setText(
                    item.getProduct()
            );


            txtQuantity.setText(
                    "Qty: " +
                            item.getQuantity()
            );


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