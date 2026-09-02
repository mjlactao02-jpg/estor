package estor.app;

import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
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

import java.util.ArrayList;
import java.util.Locale;

public class Paydept2Activity extends AppCompatActivity {

    // =========================================================
    // CUSTOMER INFORMATION
    // =========================================================

    private int customerId;

    private String customerName;

    private String customerPhone;


    // =========================================================
    // DATABASE
    // =========================================================

    private DatabaseHelper databaseHelper;


    // =========================================================
    // CUSTOMER VIEWS
    // =========================================================

    private TextView txtCustomerName;

    private TextView txtCustomerPhone;

    private TextView txtTotalDebt;


    // =========================================================
    // DEBT LIST
    // =========================================================

    private ListView listViewDebt;

    private ArrayList<DebtItem> debtItems;

    private DebtAdapter adapter;


    // =========================================================
    // PAYMENT VIEWS
    // =========================================================

    private EditText edtAmountToPay;

    private Button btnHalf;

    private Button btnFull;

    private Button btnPayDebt;

    private ImageButton btnBack;


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
                R.layout.paydept2
        );


        // =====================================================
        // DATABASE
        // =====================================================

        databaseHelper =
                new DatabaseHelper(this);


        // =====================================================
        // GET CUSTOMER ID
        // =====================================================

        String customerIdString =
                getIntent()
                        .getStringExtra(
                                "customer_id"
                        );


        if (customerIdString == null) {

            Toast.makeText(
                    this,
                    "Customer information is missing",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }


        try {

            customerId =
                    Integer.parseInt(
                            customerIdString
                    );

        } catch (NumberFormatException e) {

            Toast.makeText(
                    this,
                    "Invalid customer ID",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }


        // =====================================================
        // GET CUSTOMER NAME
        // =====================================================

        customerName =
                getIntent()
                        .getStringExtra(
                                "customer_name"
                        );


        // =====================================================
        // GET CUSTOMER PHONE
        // =====================================================

        customerPhone =
                getIntent()
                        .getStringExtra(
                                "customer_phone"
                        );


        // =====================================================
        // CONNECT CUSTOMER VIEWS
        // =====================================================

        txtCustomerName =
                findViewById(
                        R.id.txtCustomerName
                );


        txtCustomerPhone =
                findViewById(
                        R.id.txtCustomerPhone
                );


        txtTotalDebt =
                findViewById(
                        R.id.txtTotalDebt
                );


        // =====================================================
        // CONNECT LISTVIEW
        // =====================================================

        listViewDebt =
                findViewById(
                        R.id.listViewDebt
                );


        // =====================================================
        // CONNECT PAYMENT VIEWS
        // =====================================================

        edtAmountToPay =
                findViewById(
                        R.id.edtAmountToPay
                );


        btnHalf =
                findViewById(
                        R.id.btnHalf
                );


        btnFull =
                findViewById(
                        R.id.btnFull
                );


        btnPayDebt =
                findViewById(
                        R.id.btnPayDebt
                );


        btnBack =
                findViewById(
                        R.id.btnBack
                );


        // =====================================================
        // DISPLAY CUSTOMER
        // =====================================================

        if (customerName == null ||
                customerName.trim().isEmpty()) {

            txtCustomerName.setText(
                    "Unknown"
            );

        } else {

            txtCustomerName.setText(
                    customerName
            );
        }


        if (customerPhone == null) {

            txtCustomerPhone.setText(
                    ""
            );

        } else {

            txtCustomerPhone.setText(
                    customerPhone
            );
        }


        // =====================================================
        // CREATE DEBT LIST
        // =====================================================

        debtItems =
                new ArrayList<>();


        adapter =
                new DebtAdapter(
                        this,
                        debtItems
                );


        listViewDebt.setAdapter(
                adapter
        );


        // =====================================================
        // LOAD DEBT ITEMS
        // =====================================================

        loadDebtItems();


        // =====================================================
        // BACK BUTTON
        // =====================================================

        btnBack.setOnClickListener(v -> {

            finish();

        });


        // =====================================================
        // HALF BUTTON
        // =====================================================

        btnHalf.setOnClickListener(v -> {

            setHalfPayment();

        });


        // =====================================================
        // FULL BUTTON
        // =====================================================

        btnFull.setOnClickListener(v -> {

            setFullPayment();

        });


        // =====================================================
        // PAY DEBT BUTTON
        // =====================================================

        btnPayDebt.setOnClickListener(v -> {

            makePayment();

        });
    }


    // =========================================================
    // ON RESUME
    // =========================================================

    @Override
    protected void onResume() {

        super.onResume();


        if (databaseHelper != null) {

            loadDebtItems();
        }
    }


    // =========================================================
    // LOAD DEBT ITEMS
    // =========================================================

    private void loadDebtItems() {

        // Clear existing items
        debtItems.clear();


        // Get debt records for selected customer
        Cursor cursor =
                databaseHelper
                        .getDebtItemsForCustomer(
                                customerId
                        );


        if (cursor != null) {

            int debtIdIndex =
                    cursor.getColumnIndex(
                            DatabaseHelper.COLUMN_DEBT_ID
                    );


            int productIndex =
                    cursor.getColumnIndex(
                            DatabaseHelper.COLUMN_DEBT_ITEM
                    );


            int quantityIndex =
                    cursor.getColumnIndex(
                            DatabaseHelper.COLUMN_DEBT_QUANTITY
                    );


            int amountIndex =
                    cursor.getColumnIndex(
                            DatabaseHelper.COLUMN_DEBT_AMOUNT
                    );


            int paidIndex =
                    cursor.getColumnIndex(
                            DatabaseHelper.COLUMN_DEBT_PAID
                    );


            // =================================================
            // READ DEBTS
            // =================================================

            while (cursor.moveToNext()) {

                long debtId =
                        cursor.getLong(
                                debtIdIndex
                        );


                String product =
                        cursor.getString(
                                productIndex
                        );


                int quantity =
                        cursor.getInt(
                                quantityIndex
                        );


                double amount =
                        cursor.getDouble(
                                amountIndex
                        );


                double paid =
                        cursor.getDouble(
                                paidIndex
                        );


                // Current remaining balance
                double remaining =
                        amount - paid;


                // Only display debts that still have balance
                if (remaining > 0) {

                    DebtItem item =
                            new DebtItem(
                                    debtId,
                                    product,
                                    quantity,
                                    remaining
                            );


                    debtItems.add(
                            item
                    );
                }
            }


            cursor.close();
        }


        // Refresh ListView
        adapter.notifyDataSetChanged();


        // Update total
        updateTotalDebt();


        // =====================================================
        // IF NO DEBT
        // =====================================================

        if (debtItems.isEmpty()) {

            edtAmountToPay.setText("");

            Toast.makeText(
                    this,
                    "This customer has no remaining debt",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }


    // =========================================================
    // UPDATE TOTAL DEBT
    // =========================================================

    private void updateTotalDebt() {

        double total = 0;


        for (DebtItem item : debtItems) {

            total +=
                    item.getRemainingAmount();
        }


        txtTotalDebt.setText(
                String.format(
                        Locale.getDefault(),
                        "₱ %.2f",
                        total
                )
        );
    }


    // =========================================================
    // GET TOTAL FROM DATABASE
    // =========================================================

    private double getCurrentTotalDebt() {

        return databaseHelper
                .getCustomerTotalDebt(
                        customerId
                );
    }


    // =========================================================
    // HALF PAYMENT
    // =========================================================

    private void setHalfPayment() {

        double total =
                getCurrentTotalDebt();


        if (total <= 0) {

            edtAmountToPay.setText("");

            return;
        }


        double half =
                total / 2.0;


        edtAmountToPay.setText(
                String.format(
                        Locale.getDefault(),
                        "%.2f",
                        half
                )
        );


        edtAmountToPay.setSelection(
                edtAmountToPay.length()
        );
    }


    // =========================================================
    // FULL PAYMENT
    // =========================================================

    private void setFullPayment() {

        double total =
                getCurrentTotalDebt();


        if (total <= 0) {

            edtAmountToPay.setText("");

            return;
        }


        edtAmountToPay.setText(
                String.format(
                        Locale.getDefault(),
                        "%.2f",
                        total
                )
        );


        edtAmountToPay.setSelection(
                edtAmountToPay.length()
        );
    }


    // =========================================================
    // MAKE PAYMENT
    // =========================================================

    private void makePayment() {

        String amountText =
                edtAmountToPay.getText()
                        .toString()
                        .trim();


        // =====================================================
        // EMPTY CHECK
        // =====================================================

        if (amountText.isEmpty()) {

            edtAmountToPay.setError(
                    "Enter amount"
            );

            edtAmountToPay.requestFocus();

            return;
        }


        // =====================================================
        // CONVERT AMOUNT
        // =====================================================

        double paymentAmount;


        try {

            paymentAmount =
                    Double.parseDouble(
                            amountText
                    );

        } catch (NumberFormatException e) {

            edtAmountToPay.setError(
                    "Enter a valid amount"
            );

            edtAmountToPay.requestFocus();

            return;
        }


        // =====================================================
        // GREATER THAN ZERO
        // =====================================================

        if (paymentAmount <= 0) {

            edtAmountToPay.setError(
                    "Amount must be greater than 0"
            );

            edtAmountToPay.requestFocus();

            return;
        }


        // =====================================================
        // GET CURRENT DEBT
        // =====================================================

        double currentDebt =
                getCurrentTotalDebt();


        // =====================================================
        // NO DEBT
        // =====================================================

        if (currentDebt <= 0) {

            Toast.makeText(
                    this,
                    "This customer has no remaining debt",
                    Toast.LENGTH_SHORT
            ).show();

            loadDebtItems();

            return;
        }


        // =====================================================
        // PAYMENT CANNOT EXCEED DEBT
        // =====================================================

        if (paymentAmount >
                currentDebt + 0.001) {

            edtAmountToPay.setError(
                    "Amount cannot be greater than debt"
            );

            edtAmountToPay.requestFocus();

            return;
        }


        // =====================================================
        // SAVE PAYMENT
        // =====================================================

        boolean paymentSuccessful =
                databaseHelper.makePayment(
                        customerId,
                        paymentAmount
                );


        // =====================================================
        // CHECK RESULT
        // =====================================================

        if (!paymentSuccessful) {

            Toast.makeText(
                    this,
                    "Failed to process payment",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        // =====================================================
        // SUCCESS
        // =====================================================

        Toast.makeText(
                this,
                "Payment successful",
                Toast.LENGTH_SHORT
        ).show();


        // =====================================================
        // CLEAR INPUT
        // =====================================================

        edtAmountToPay.setText("");


        // =====================================================
        // RELOAD DEBT
        // =====================================================

        loadDebtItems();


        // =====================================================
        // CHECK IF FULLY PAID
        // =====================================================

        double remainingDebt =
                getCurrentTotalDebt();


        if (remainingDebt <= 0.001) {

            Toast.makeText(
                    this,
                    "Debt fully paid",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
        }
    }


    // =========================================================
    // DEBT ITEM CLASS
    // =========================================================

    private static class DebtItem {

        private long debtId;

        private String product;

        private int quantity;

        private double remainingAmount;


        DebtItem(
                long debtId,
                String product,
                int quantity,
                double remainingAmount
        ) {

            this.debtId =
                    debtId;

            this.product =
                    product;

            this.quantity =
                    quantity;

            this.remainingAmount =
                    remainingAmount;
        }


        String getProduct() {

            return product;
        }


        int getQuantity() {

            return quantity;
        }


        double getRemainingAmount() {

            return remainingAmount;
        }
    }


    // =========================================================
    // DEBT ADAPTER
    // =========================================================

    private static class DebtAdapter
            extends BaseAdapter {

        private final android.content.Context context;

        private final ArrayList<DebtItem> debtItems;


        DebtAdapter(
                android.content.Context context,
                ArrayList<DebtItem> debtItems
        ) {

            this.context =
                    context;

            this.debtItems =
                    debtItems;
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

            return debtItems
                    .get(position)
                    .debtId;
        }


        @Override
        public View getView(
                int position,
                View convertView,
                ViewGroup parent
        ) {

            // =================================================
            // CREATE ROW
            // =================================================

            if (convertView == null) {

                convertView =
                        LayoutInflater
                                .from(context)
                                .inflate(
                                        R.layout.item_paydept2,
                                        parent,
                                        false
                                );
            }


            // =================================================
            // CONNECT VIEWS
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


            // =================================================
            // GET ITEM
            // =================================================

            DebtItem item =
                    debtItems.get(
                            position
                    );


            // =================================================
            // DISPLAY PRODUCT
            // =================================================

            txtProduct.setText(
                    item.getProduct()
            );


            // =================================================
            // DISPLAY QUANTITY
            // =================================================

            txtQuantity.setText(
                    "Qty " +
                            item.getQuantity()
            );


            // =================================================
            // DISPLAY REMAINING AMOUNT
            // =================================================

            txtAmount.setText(
                    "₱ " +
                            String.format(
                                    Locale.getDefault(),
                                    "%.2f",
                                    item.getRemainingAmount()
                            )
            );


            return convertView;
        }
    }
}