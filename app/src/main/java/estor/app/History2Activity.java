package estor.app;

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

public class History2Activity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView txtCustomerName;
    private TextView txtCustomerPhone;
    private TextView txtTotalBorrowed;
    private TextView txtTotalPaid;
    private TextView txtCurrentDebt;
    private ListView listHistory;

    private DatabaseHelper databaseHelper;
    private int customerId = -1;

    private final ArrayList<HistoryItem> historyItems = new ArrayList<>();
    private HistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history2);

        databaseHelper = new DatabaseHelper(this);

        btnBack = findViewById(R.id.btnBack);
        txtCustomerName = findViewById(R.id.txtCustomerName);
        txtCustomerPhone = findViewById(R.id.txtCustomerPhone);
        txtTotalBorrowed = findViewById(R.id.txtTotalBorrowed);
        txtTotalPaid = findViewById(R.id.txtTotalPaid);
        txtCurrentDebt = findViewById(R.id.txtCurrentDebt);
        listHistory = findViewById(R.id.listHistory);

        customerId = getIntent().getIntExtra("customer_id", -1);

        String customerName = getIntent().getStringExtra("customer_name");
        String customerPhone = getIntent().getStringExtra("customer_phone");

        txtCustomerName.setText(
                customerName == null || customerName.trim().isEmpty()
                        ? "Unknown Customer"
                        : customerName
        );

        txtCustomerPhone.setText(
                customerPhone == null ? "" : customerPhone
        );

        adapter = new HistoryAdapter();
        listHistory.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        loadHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper != null && listHistory != null) {
            loadHistory();
        }
    }

    private void loadHistory() {
        if (customerId < 0) {
            txtTotalBorrowed.setText("₱0.00");
            txtTotalPaid.setText("₱0.00");
            txtCurrentDebt.setText("₱0.00");
            historyItems.clear();
            adapter.notifyDataSetChanged();
            return;
        }

        loadTotals();
        loadItems();
    }

    private void loadTotals() {
        Cursor cursor = databaseHelper.getHistoryTotalsForCustomer(customerId);

        double totalBorrowed = 0;
        double totalPaid = 0;

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int borrowedIndex = cursor.getColumnIndex("total_borrowed");
                    int paidIndex = cursor.getColumnIndex("total_paid");

                    if (borrowedIndex >= 0) {
                        totalBorrowed = cursor.getDouble(borrowedIndex);
                    }

                    if (paidIndex >= 0) {
                        totalPaid = cursor.getDouble(paidIndex);
                    }
                }
            } finally {
                cursor.close();
            }
        }

        double currentDebt = Math.max(0, totalBorrowed - totalPaid);

        txtTotalBorrowed.setText(formatMoney(totalBorrowed));
        txtTotalPaid.setText(formatMoney(totalPaid));
        txtCurrentDebt.setText(formatMoney(currentDebt));
    }

    private void loadItems() {
        historyItems.clear();

        Cursor cursor = databaseHelper.getHistoryItemsForCustomer(customerId);

        if (cursor != null) {
            try {
                int itemIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DEBT_ITEM);
                int quantityIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DEBT_QUANTITY);
                int amountIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DEBT_AMOUNT);
                int paidIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DEBT_PAID);
                int dateIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DEBT_DATE);
                int timeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DEBT_TIME);

                while (cursor.moveToNext()) {
                    String item = itemIndex >= 0 && !cursor.isNull(itemIndex)
                            ? cursor.getString(itemIndex)
                            : "Unknown Item";

                    int quantity = quantityIndex >= 0 && !cursor.isNull(quantityIndex)
                            ? cursor.getInt(quantityIndex)
                            : 1;

                    double amount = amountIndex >= 0 && !cursor.isNull(amountIndex)
                            ? cursor.getDouble(amountIndex)
                            : 0;

                    double paid = paidIndex >= 0 && !cursor.isNull(paidIndex)
                            ? cursor.getDouble(paidIndex)
                            : 0;

                    String date = dateIndex >= 0 && !cursor.isNull(dateIndex)
                            ? cursor.getString(dateIndex)
                            : "";

                    String time = timeIndex >= 0 && !cursor.isNull(timeIndex)
                            ? cursor.getString(timeIndex)
                            : "";

                    historyItems.add(new HistoryItem(
                            item,
                            quantity,
                            amount,
                            paid,
                            date,
                            time
                    ));
                }
            } finally {
                cursor.close();
            }
        }

        adapter.notifyDataSetChanged();
    }

    private String formatMoney(double amount) {
        return String.format(Locale.getDefault(), "₱%.2f", amount);
    }

    private static class HistoryItem {
        String item;
        int quantity;
        double amount;
        double paid;
        String date;
        String time;

        HistoryItem(
                String item,
                int quantity,
                double amount,
                double paid,
                String date,
                String time
        ) {
            this.item = item;
            this.quantity = quantity;
            this.amount = amount;
            this.paid = paid;
            this.date = date;
            this.time = time;
        }
    }

    private class HistoryAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return historyItems.size();
        }

        @Override
        public Object getItem(int position) {
            return historyItems.get(position);
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
            if (convertView == null) {
                convertView = LayoutInflater.from(History2Activity.this)
                        .inflate(R.layout.item_history2, parent, false);
            }

            TextView txtDateTime = convertView.findViewById(R.id.txtHistoryDateTime);
            TextView txtProduct = convertView.findViewById(R.id.txtHistoryProduct);
            TextView txtQuantity = convertView.findViewById(R.id.txtHistoryQuantity);
            TextView txtAmount = convertView.findViewById(R.id.txtHistoryAmount);
            TextView txtStatus = convertView.findViewById(R.id.txtHistoryStatus);

            HistoryItem item = historyItems.get(position);

            String dateTime = item.date;
            if (item.time != null && !item.time.trim().isEmpty()) {
                if (!dateTime.isEmpty()) {
                    dateTime += " • ";
                }
                dateTime += item.time;
            }

            if (dateTime.trim().isEmpty()) {
                dateTime = "Previous debt";
            }

            txtDateTime.setText(dateTime);
            txtProduct.setText(item.item);
            txtQuantity.setText("Qty " + item.quantity);
            txtAmount.setText(formatMoney(item.amount));

            double remaining = Math.max(0, item.amount - item.paid);

            if (remaining <= 0.001) {
                txtStatus.setText("Paid");
                txtStatus.setTextColor(android.graphics.Color.rgb(53, 185, 107));
                txtStatus.setBackgroundResource(R.drawable.bg_history_status_partial);
            } else if (item.paid > 0.001) {
                txtStatus.setText("Partial");
                txtStatus.setTextColor(android.graphics.Color.rgb(240, 91, 91));
                txtStatus.setBackgroundResource(R.drawable.bg_history_status);
            } else {
                txtStatus.setText("Unpaid");
                txtStatus.setTextColor(android.graphics.Color.rgb(240, 91, 91));
                txtStatus.setBackgroundResource(R.drawable.bg_history_status);
            }

            return convertView;
        }
    }
}
