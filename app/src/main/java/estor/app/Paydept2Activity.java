package estor.app;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Locale;
public class Paydept2Activity extends AppCompatActivity {
    private int customerId;
    private String customerName;
    private String customerPhone;
    private DatabaseHelper databaseHelper;
    private TextView txtCustomerName;
    private TextView txtCustomerPhone;
    private TextView txtTotalDebt;
    private ListView listViewDebt;
    private ArrayList<DebtItem> debtItems;
    private DebtAdapter adapter;
    private EditText edtAmountToPay;
    private Button btnHalf;
    private Button btnFull;
    private Button btnPayDebt;
    private ImageButton btnBack;
    private LinearLayout paymentContainer;
    private static final int SMS_PERMISSION_CODE = 3;
    private int originalPaymentBottomMargin = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setContentView(R.layout.paydept2);

        databaseHelper = new DatabaseHelper(this);

        checkSmsPermission();
        String id = getIntent().getStringExtra("customer_id");

        if (id == null) {
            Toast.makeText(this, "Customer information is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        try {customerId = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid customer ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        customerName = getIntent().getStringExtra("customer_name");
        customerPhone = getIntent().getStringExtra("customer_phone");
        txtCustomerName = findViewById(R.id.txtCustomerName);
        txtCustomerPhone = findViewById(R.id.txtCustomerPhone);
        txtTotalDebt = findViewById(R.id.txtTotalDebt);
        listViewDebt = findViewById(R.id.listViewDebt);
        edtAmountToPay = findViewById(R.id.edtAmountToPay);
        btnHalf = findViewById(R.id.btnHalf);
        btnFull = findViewById(R.id.btnFull);
        btnPayDebt = findViewById(R.id.btnPayDebt);
        btnBack = findViewById(R.id.btnBack);
        paymentContainer = findViewById(R.id.paymentContainer);
        setupKeyboardFix();

        if (customerName != null && !customerName.trim().isEmpty()) {
            txtCustomerName.setText(customerName);
        } else {
            txtCustomerName.setText("Unknown");
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
        debtItems = new ArrayList<>();
        adapter = new DebtAdapter(this, debtItems);
        listViewDebt.setAdapter(adapter);
        loadDebtItems();
        btnBack.setOnClickListener(v -> {
            finish();
        });

        // HALF
        btnHalf.setOnClickListener(v -> {
            double total = databaseHelper.getCustomerTotalDebt(customerId);

            if (total <= 0.001) {
                edtAmountToPay.setText("");
                return;
            }

            double half = total / 2.0;
            edtAmountToPay.setText(String.format(Locale.getDefault(), "%.2f", half)
            );

            edtAmountToPay.setSelection(edtAmountToPay.length());
        });


        // =====================================================
        // FULL
        // =====================================================

        btnFull.setOnClickListener(v -> {

            double total =
                    databaseHelper
                            .getCustomerTotalDebt(
                                    customerId
                            );


            if (total <= 0.001) {

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
        });


        // =====================================================
        // PAY DEBT
        // =====================================================

        btnPayDebt.setOnClickListener(v -> {

            makePayment();

        });
    }


    // =========================================================
    // KEYBOARD FIX
    // =========================================================

    private void setupKeyboardFix() {

        if (paymentContainer == null) {

            return;
        }


        ViewGroup.LayoutParams params =
                paymentContainer.getLayoutParams();


        if (!(params instanceof
                androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)) {

            return;
        }


        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams constraintParams =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)
                        params;


        originalPaymentBottomMargin =
                constraintParams.bottomMargin;


        View rootView =
                findViewById(
                        android.R.id.content
                );


        ViewCompat.setOnApplyWindowInsetsListener(
                rootView,
                (view, insets) -> {

                    Insets imeInsets =
                            insets.getInsets(
                                    WindowInsetsCompat.Type.ime()
                            );


                    Insets systemInsets =
                            insets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                            );


                    boolean keyboardVisible =
                            insets.isVisible(
                                    WindowInsetsCompat.Type.ime()
                            );


                    ViewGroup.LayoutParams currentParams =
                            paymentContainer.getLayoutParams();


                    if (!(currentParams instanceof
                            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)) {

                        return insets;
                    }


                    androidx.constraintlayout.widget.ConstraintLayout.LayoutParams paymentParams =
                            (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)
                                    currentParams;


                    if (keyboardVisible) {

                        int keyboardHeight =
                                imeInsets.bottom;


                        int navigationBarHeight =
                                systemInsets.bottom;


                        int keyboardOverlap =
                                Math.max(
                                        0,
                                        keyboardHeight -
                                                navigationBarHeight
                                );


                        paymentParams.bottomMargin =
                                originalPaymentBottomMargin +
                                        keyboardOverlap;

                    } else {

                        paymentParams.bottomMargin =
                                originalPaymentBottomMargin;
                    }


                    paymentContainer.setLayoutParams(
                            paymentParams
                    );


                    return insets;
                }
        );


        ViewCompat.requestApplyInsets(
                rootView
        );
    }


    // =========================================================
    // ON RESUME
    // =========================================================

    @Override
    protected void onResume() {

        super.onResume();


        if (databaseHelper != null &&
                debtItems != null) {

            loadDebtItems();
        }
    }


    // =========================================================
    // LOAD DEBT ITEMS
    // =========================================================

    private void loadDebtItems() {

        debtItems.clear();


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


                double remaining =
                        amount - paid;


                if (remaining > 0.001) {

                    debtItems.add(
                            new DebtItem(
                                    debtId,
                                    product,
                                    quantity,
                                    remaining
                            )
                    );
                }
            }


            cursor.close();
        }


        adapter.notifyDataSetChanged();


        updateTotalDebt();
    }


    // =========================================================
    // UPDATE TOTAL DEBT
    // =========================================================

    private void updateTotalDebt() {

        double total =
                databaseHelper
                        .getCustomerTotalDebt(
                                customerId
                        );


        txtTotalDebt.setText(
                String.format(
                        Locale.getDefault(),
                        "₱ %.2f",
                        total
                )
        );
    }


    // =========================================================
    // MAKE PAYMENT
    // =========================================================

    private void makePayment() {

        String amountText =
                edtAmountToPay
                        .getText()
                        .toString()
                        .trim();


        if (amountText.isEmpty()) {

            edtAmountToPay.setError(
                    "Enter amount"
            );

            edtAmountToPay.requestFocus();

            return;
        }


        double payment;


        try {

            payment =
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


        if (payment <= 0) {

            edtAmountToPay.setError(
                    "Amount must be greater than 0"
            );

            edtAmountToPay.requestFocus();

            return;
        }


        double currentDebt =
                databaseHelper
                        .getCustomerTotalDebt(
                                customerId
                        );


        if (currentDebt <= 0.001) {

            Toast.makeText(
                    this,
                    "No remaining debt",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }


        if (payment >
                currentDebt + 0.001) {

            edtAmountToPay.setError(
                    "Amount cannot be greater than debt"
            );

            edtAmountToPay.requestFocus();

            return;
        }


        // =====================================================
        // PROCESS PAYMENT
        // =====================================================

        boolean success =
                databaseHelper.makePayment(
                        customerId,
                        payment
                );


        if (!success) {

            Toast.makeText(
                    this,
                    "Failed to process payment",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // =====================================================
        // SAVE PAYMENT TRANSACTION
        // =====================================================

        long transactionResult =
                databaseHelper.addPaymentTransaction(

                        customerId,

                        customerName,

                        payment
                );


        if (transactionResult == -1) {

            Toast.makeText(
                    this,
                    "Payment saved, but transaction history could not be saved",
                    Toast.LENGTH_LONG
            ).show();
        }


        // =====================================================
        // REMAINING DEBT
        // =====================================================

        double remainingDebt =
                databaseHelper
                        .getCustomerTotalDebt(
                                customerId
                        );


        // =====================================================
        // SMS
        // =====================================================

        sendPaymentSms(
                payment,
                remainingDebt
        );


        Toast.makeText(
                this,
                "Payment successful",
                Toast.LENGTH_SHORT
        ).show();


        edtAmountToPay.setText("");


        if (remainingDebt <= 0.001) {

            loadDebtItems();


            Toast.makeText(
                    this,
                    "Debt fully paid",
                    Toast.LENGTH_SHORT
            ).show();


            setResult(
                    RESULT_OK
            );


            finish();

            return;
        }


        loadDebtItems();
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
    // PAYMENT SMS
    // =========================================================

    private void sendPaymentSms(
            double paymentAmount,
            double remainingDebt
    ) {

        if (customerPhone == null ||
                customerPhone.trim().isEmpty()) {

            return;
        }


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
        ) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(
                    this,
                    "Payment saved, but SMS permission was not granted",
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
                .append(",\n\n");


        message.append(
                        "We received your payment of ₱"
                )
                .append(
                        String.format(
                                Locale.getDefault(),
                                "%.2f",
                                paymentAmount
                        )
                )
                .append(".\n\n");


        message.append(
                        "Remaining debt: ₱"
                )
                .append(
                        String.format(
                                Locale.getDefault(),
                                "%.2f",
                                remainingDebt
                        )
                )
                .append("\n");


        if (remainingDebt <= 0.001) {

            message.append(
                    "\nYour debt has been fully paid.\n"
            );
        }


        message.append(
                "\nThank you."
        );


        try {

            SmsManager smsManager =
                    SmsManager.getDefault();


            ArrayList<String> parts =
                    smsManager.divideMessage(
                            message.toString()
                    );


            smsManager.sendMultipartTextMessage(
                    customerPhone,
                    null,
                    parts,
                    null,
                    null
            );


        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Payment saved, but SMS failed",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }


    // =========================================================
    // DEBT ITEM
    // =========================================================

    private static class DebtItem {

        private final long debtId;

        private final String product;

        private final int quantity;

        private final double remainingAmount;


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

        private final Context context;

        private final ArrayList<DebtItem> debtItems;


        DebtAdapter(
                Context context,
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
                    "Qty " +
                            item.getQuantity()
            );


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