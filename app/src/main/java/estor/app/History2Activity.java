package estor.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
    private TextView txtEmptyHistory;
    private ImageButton btnHeaderSms;

    private DatabaseHelper databaseHelper;
    private int customerId = -1;
    private String customerName;
    private String customerPhone;

    private double totalBorrowed;
    private double totalPaid;
    private double currentDebt;

    private static final int SMS_PERMISSION_CODE = 101;

    private final ArrayList<HistoryItem> historyItems = new ArrayList<>();
    private HistoryAdapter adapter;

    private HistoryItem pendingSmsItem;

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
        txtEmptyHistory = findViewById(R.id.txtEmptyHistory);
        btnHeaderSms = findViewById(R.id.btnHeaderSms);

        customerId = getIntent().getIntExtra("customer_id", -1);

        customerName = getIntent().getStringExtra("customer_name");
        customerPhone = getIntent().getStringExtra("customer_phone");

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

        if (btnHeaderSms != null) {
            btnHeaderSms.setOnClickListener(v -> {
                if (historyItems.isEmpty()) {
                    Toast.makeText(this, "No debts to send", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Send latest item via header icon
                handleSmsForItem(historyItems.get(0));
            });
        }

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

        currentDebt = Math.max(0, totalBorrowed - totalPaid);
        this.totalBorrowed = totalBorrowed;
        this.totalPaid = totalPaid;

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
                int dueDateIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DEBT_DUE_DATE);

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

                    String dueDate = dueDateIndex >= 0 && !cursor.isNull(dueDateIndex)
                            ? cursor.getString(dueDateIndex)
                            : null;

                    historyItems.add(new HistoryItem(
                            item,
                            quantity,
                            amount,
                            paid,
                            date,
                            time,
                            dueDate
                    ));
                }
            } finally {
                cursor.close();
            }
        }

        boolean empty = historyItems.isEmpty();
        if (txtEmptyHistory != null) txtEmptyHistory.setVisibility(empty ? View.VISIBLE : View.GONE);
        listHistory.setVisibility(empty ? View.GONE : View.VISIBLE);
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
        String dueDateIso;

        HistoryItem(
                String item,
                int quantity,
                double amount,
                double paid,
                String date,
                String time,
                String dueDateIso
        ) {
            this.item = item;
            this.quantity = quantity;
            this.amount = amount;
            this.paid = paid;
            this.date = date;
            this.time = time;
            this.dueDateIso = dueDateIso;
        }
    }

    private static class ViewHolder {
            TextView txtDateTime;
            TextView txtProduct;
            TextView txtQuantity;
            TextView txtAmount;
            TextView txtStatus;
            TextView txtDueDate;
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
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(History2Activity.this)
                        .inflate(R.layout.item_history2, parent, false);
                holder = new ViewHolder();
                holder.txtDateTime = convertView.findViewById(R.id.txtHistoryDateTime);
                holder.txtProduct = convertView.findViewById(R.id.txtHistoryProduct);
                holder.txtQuantity = convertView.findViewById(R.id.txtHistoryQuantity);
                holder.txtAmount = convertView.findViewById(R.id.txtHistoryAmount);
                holder.txtStatus = convertView.findViewById(R.id.txtHistoryStatus);
                holder.txtDueDate = convertView.findViewById(R.id.txtHistoryDueDate);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            TextView txtDateTime = holder.txtDateTime;
            TextView txtProduct = holder.txtProduct;
            TextView txtQuantity = holder.txtQuantity;
            TextView txtAmount = holder.txtAmount;
            TextView txtStatus = holder.txtStatus;
            TextView txtDueDate = holder.txtDueDate;

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

            // Due date: hide if none, color if overdue
            if (item.dueDateIso != null && !item.dueDateIso.trim().isEmpty()) {
                String disp = DueDateUtils.formatForDisplay(item.dueDateIso);
                if (remaining > 0.001 && DueDateUtils.isOverdue(item.dueDateIso)) {
                    txtDueDate.setText("Due: " + disp + " • OVERDUE");
                    txtDueDate.setTextColor(android.graphics.Color.rgb(240, 91, 91));
                    txtDueDate.setVisibility(View.VISIBLE);
                } else if (remaining > 0.001 && DueDateUtils.isDueSoon(item.dueDateIso, 3)) {
                    txtDueDate.setText("Due: " + disp);
                    txtDueDate.setTextColor(android.graphics.Color.rgb(230, 160, 0));
                    txtDueDate.setVisibility(View.VISIBLE);
                } else {
                    txtDueDate.setText("Due: " + disp);
                    txtDueDate.setTextColor(android.graphics.Color.rgb(108, 99, 255));
                    txtDueDate.setVisibility(View.VISIBLE);
                }
            } else {
                txtDueDate.setVisibility(View.GONE);
            }

            if (remaining <= 0.001) {
                txtStatus.setText("Paid");
                txtStatus.setTextColor(android.graphics.Color.rgb(53, 185, 107));
                txtStatus.setBackgroundResource(R.drawable.bg_history_status_partial);
            } else if (item.paid > 0.001) {
                txtStatus.setText("Partial");
                txtStatus.setTextColor(android.graphics.Color.rgb(240, 91, 91));
                txtStatus.setBackgroundResource(R.drawable.bg_history_status);
            } else {
                // Unpaid but check overdue for status text
                if (item.dueDateIso != null && remaining > 0.001 && DueDateUtils.isOverdue(item.dueDateIso)) {
                    txtStatus.setText("Overdue");
                } else {
                    txtStatus.setText("Unpaid");
                }
                txtStatus.setTextColor(android.graphics.Color.rgb(240, 91, 91));
                txtStatus.setBackgroundResource(R.drawable.bg_history_status);
            }

            return convertView;
        }
    }

    // =========================================================
    // SMS - handle per-item and header
    // =========================================================

    private void handleSmsForItem(HistoryItem item) {
        if (customerPhone == null || customerPhone.trim().isEmpty()) {
            Toast.makeText(this, "No phone number for this customer", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                pendingSmsItem = item;
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
                return;
            }
        }
        sendSmsForItem(item);
    }

    private void sendSmsForItem(HistoryItem item) {
        if (customerPhone == null || customerPhone.trim().isEmpty()) {
            Toast.makeText(this, "No phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        double remaining = Math.max(0, item.amount - item.paid);
        String status;
        if (remaining <= 0.001) status = "Paid";
        else if (item.paid > 0.001) status = "Partial";
        else {
            if (item.dueDateIso != null && DueDateUtils.isOverdue(item.dueDateIso)) status = "Overdue";
            else status = "Unpaid";
        }

        String debtDate = item.date != null ? item.date : "";
        if (item.time != null && !item.time.trim().isEmpty()) {
            if (!debtDate.isEmpty()) debtDate += " " + item.time;
            else debtDate = item.time;
        }
        if (debtDate.trim().isEmpty()) debtDate = "N/A";

        String dueDisp = "N/A";
        if (item.dueDateIso != null && !item.dueDateIso.trim().isEmpty()) {
            dueDisp = DueDateUtils.formatForDisplay(item.dueDateIso);
            if (remaining > 0.001 && DueDateUtils.isOverdue(item.dueDateIso)) {
                dueDisp += " (OVERDUE " + DueDateUtils.daysOverdue(item.dueDateIso) + "d)";
            }
        }

        String storeName = SettingsActivity.getStoreName(this);
        if (storeName == null || storeName.trim().isEmpty()) storeName = "Estor";

        StringBuilder sb = new StringBuilder();
        sb.append("Hello ").append(customerName != null ? customerName : "Customer").append(",\n\n");
        sb.append("From ").append(storeName).append(":\n");
        sb.append("Item: ").append(item.item).append("\n");
        sb.append("Qty: ").append(item.quantity).append("\n");
        sb.append("Amount: ").append(formatMoney(item.amount)).append("\n");
        sb.append("Debt Date: ").append(debtDate).append("\n");
        sb.append("Due Date: ").append(dueDisp).append("\n");
        sb.append("Status: ").append(status).append("\n");
        sb.append("Current Debt: ").append(formatMoney(currentDebt)).append("\n");
        sb.append("Total Paid: ").append(formatMoney(totalPaid)).append("\n");
        sb.append("Total Borrowed: ").append(formatMoney(totalBorrowed));

        try {
            SmsManager smsManager = SmsManager.getDefault();
            String message = sb.toString();
            java.util.ArrayList<String> parts = smsManager.divideMessage(message);
            smsManager.sendMultipartTextMessage(customerPhone.trim(), null, parts, null, null);
            Toast.makeText(this, "SMS sent to " + customerPhone, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send SMS", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (pendingSmsItem != null) {
                    sendSmsForItem(pendingSmsItem);
                    pendingSmsItem = null;
                }
            } else {
                Toast.makeText(this, "SMS permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
