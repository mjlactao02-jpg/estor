package estor.app;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;

public class TransactionActivity extends AppCompatActivity {

    private ListView listCustomers;

    private TextView txtCustomerCount;

    private ImageButton btnBack;

    private DatabaseHelper databaseHelper;

    private ArrayList<TransactionItem> transactionList;

    private TransactionAdapter adapter;


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.transaction_main
        );


        databaseHelper =
                new DatabaseHelper(this);


        listCustomers =
                findViewById(
                        R.id.listCustomers
                );


        txtCustomerCount =
                findViewById(
                        R.id.txtCustomerCount
                );


        btnBack =
                findViewById(
                        R.id.btnBack
                );


        transactionList =
                new ArrayList<>();


        adapter =
                new TransactionAdapter();


        listCustomers.setAdapter(
                adapter
        );


        loadTransactions();

        updateCustomerCount();


        btnBack.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            TransactionActivity.this,
                            MainActivity.class
                    );

            startActivity(intent);

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

            loadTransactions();

            updateCustomerCount();
        }
    }


    // =========================================================
    // LOAD TRANSACTIONS
    // =========================================================

    private void loadTransactions() {

        transactionList.clear();


        Cursor cursor =
                databaseHelper.getAllTransactions();


        if (cursor == null) {

            adapter.notifyDataSetChanged();

            return;
        }


        try {

            int idIndex =
                    cursor.getColumnIndex(
                            DatabaseHelper.COLUMN_TRANSACTION_ID
                    );


            int customerIdIndex =
                    cursor.getColumnIndex(
                            DatabaseHelper.COLUMN_TRANSACTION_CUSTOMER_ID
                    );


            int nameIndex =
                    cursor.getColumnIndex(
                            DatabaseHelper.COLUMN_TRANSACTION_CUSTOMER_NAME
                    );


            int typeIndex =
                    cursor.getColumnIndex(
                            DatabaseHelper.COLUMN_TRANSACTION_TYPE
                    );


            int amountIndex =
                    cursor.getColumnIndex(
                            DatabaseHelper.COLUMN_TRANSACTION_AMOUNT
                    );


            int dateIndex =
                    cursor.getColumnIndex(
                            DatabaseHelper.COLUMN_TRANSACTION_DATE
                    );


            int timeIndex =
                    cursor.getColumnIndex(
                            DatabaseHelper.COLUMN_TRANSACTION_TIME
                    );


            while (cursor.moveToNext()) {

                long id =
                        cursor.getLong(idIndex);


                int customerId =
                        cursor.getInt(customerIdIndex);


                String name =
                        cursor.isNull(nameIndex)
                                ? ""
                                : cursor.getString(nameIndex);


                String type =
                        cursor.isNull(typeIndex)
                                ? ""
                                : cursor.getString(typeIndex);


                double amount =
                        cursor.getDouble(amountIndex);


                String date =
                        cursor.isNull(dateIndex)
                                ? ""
                                : cursor.getString(dateIndex);


                String time =
                        cursor.isNull(timeIndex)
                                ? ""
                                : cursor.getString(timeIndex);


                transactionList.add(
                        new TransactionItem(
                                id,
                                customerId,
                                name,
                                type,
                                amount,
                                date,
                                time
                        )
                );
            }

        } finally {

            cursor.close();
        }


        adapter.notifyDataSetChanged();
    }


    // =========================================================
    // UPDATE CUSTOMER COUNT
    // =========================================================

    private void updateCustomerCount() {

        int count =
                databaseHelper.getCustomerCount();


        txtCustomerCount.setText(
                String.valueOf(count)
        );
    }


    // =========================================================
    // TRANSACTION ITEM
    // =========================================================

    private static class TransactionItem {

        long id;

        int customerId;

        String customerName;

        String type;

        double amount;

        String date;

        String time;


        TransactionItem(
                long id,
                int customerId,
                String customerName,
                String type,
                double amount,
                String date,
                String time
        ) {

            this.id = id;

            this.customerId = customerId;

            this.customerName = customerName;

            this.type = type;

            this.amount = amount;

            this.date = date;

            this.time = time;
        }
    }


    // =========================================================
    // ADAPTER
    // =========================================================

    private class TransactionAdapter
            extends BaseAdapter {


        @Override
        public int getCount() {

            return transactionList.size();
        }


        @Override
        public Object getItem(
                int position
        ) {

            return transactionList.get(
                    position
            );
        }


        @Override
        public long getItemId(
                int position
        ) {

            return transactionList
                    .get(position)
                    .id;
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
                                .from(
                                        TransactionActivity.this
                                )
                                .inflate(
                                        R.layout.item_transaction,
                                        parent,
                                        false
                                );
            }


            TextView txtDate =
                    convertView.findViewById(
                            R.id.txtTransactionDate
                    );


            TextView txtName =
                    convertView.findViewById(
                            R.id.txtTransactionName
                    );


            TextView txtType =
                    convertView.findViewById(
                            R.id.txtTransactionType
                    );


            TextView txtAmount =
                    convertView.findViewById(
                            R.id.txtTransactionAmount
                    );


            TransactionItem item =
                    transactionList.get(
                            position
                    );


            // -------------------------------------------------
            // DATE / TIME
            // -------------------------------------------------

            String dateTime =
                    item.date;


            if (item.time != null &&
                    !item.time.trim().isEmpty()) {

                dateTime =
                        dateTime +
                                " " +
                                item.time;
            }


            txtDate.setText(
                    dateTime
            );


            // -------------------------------------------------
            // NAME
            // -------------------------------------------------

            txtName.setText(
                    item.customerName
            );


            // -------------------------------------------------
            // DEBT
            // -------------------------------------------------

            if (
                    DatabaseHelper.TRANSACTION_DEBT
                            .equalsIgnoreCase(item.type)
            ) {

                txtType.setText(
                        "↓ Debt"
                );


                txtType.setTextColor(
                        android.graphics.Color.rgb(
                                180,
                                0,
                                0
                        )
                );


                txtAmount.setText(
                        "-₱" +
                                String.format(
                                        Locale.getDefault(),
                                        "%.2f",
                                        item.amount
                                )
                );


                txtAmount.setTextColor(
                        android.graphics.Color.RED
                );

            }

            // -------------------------------------------------
            // PAYMENT
            // -------------------------------------------------

            else if (
                    DatabaseHelper.TRANSACTION_PAYMENT
                            .equalsIgnoreCase(item.type)
            ) {

                txtType.setText(
                        "↑ Payment"
                );


                txtType.setTextColor(
                        android.graphics.Color.rgb(
                                0,
                                180,
                                0
                        )
                );


                txtAmount.setText(
                        "+₱" +
                                String.format(
                                        Locale.getDefault(),
                                        "%.2f",
                                        item.amount
                                )
                );


                txtAmount.setTextColor(
                        android.graphics.Color.rgb(
                                0,
                                180,
                                0
                        )
                );

            }

            // -------------------------------------------------
            // UNKNOWN
            // -------------------------------------------------

            else {

                txtType.setText(
                        item.type
                );


                txtAmount.setText(
                        "₱" +
                                String.format(
                                        Locale.getDefault(),
                                        "%.2f",
                                        item.amount
                                )
                );
            }


            return convertView;
        }
    }
}