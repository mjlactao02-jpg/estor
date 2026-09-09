package estor.app;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    private ListView listCustomers;

    private EditText edtSearchCustomer;

    private TextView txtCustomerCount;

    private Button btnAll;
    private Button btnUnpaid;
    private Button btnPartiallyPaid;
    private Button btnOverdue;

    private ImageButton btnBack;

    private DatabaseHelper databaseHelper;

    private ArrayList<HistoryItem> allCustomers;

    private ArrayList<HistoryItem> displayedCustomers;

    private HistoryAdapter adapter;

    private String currentFilter = "ALL";


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.history_main);


        databaseHelper =
                new DatabaseHelper(this);


        listCustomers =
                findViewById(
                        R.id.listCustomers
                );


        edtSearchCustomer =
                findViewById(
                        R.id.edtSearchCustomer
                );


        txtCustomerCount =
                findViewById(
                        R.id.txtCustomerCount
                );


        btnAll =
                findViewById(
                        R.id.btnAll
                );


        btnUnpaid =
                findViewById(
                        R.id.btnUnpaid
                );


        btnPartiallyPaid =
                findViewById(
                        R.id.btnPartiallyPaid
                );

        btnOverdue =
                findViewById(
                        R.id.btnOverdue
                );


        btnBack =
                findViewById(
                        R.id.btnBack
                );


        allCustomers =
                new ArrayList<>();


        displayedCustomers =
                new ArrayList<>();


        adapter =
                new HistoryAdapter();

        listCustomers.setAdapter(adapter);


        loadHistory();

        // =====================================================
        // OPEN CUSTOMER HISTORY
        // =====================================================
        listCustomers.setOnItemClickListener((parent, view, position, id) -> {
            HistoryItem customer = displayedCustomers.get(position);

            Intent intent = new Intent(
                    HistoryActivity.this,
                    History2Activity.class
            );

            intent.putExtra("customer_id", customer.customerId);
            intent.putExtra("customer_name", customer.customerName);
            intent.putExtra("customer_phone", customer.customerPhone);

            startActivity(intent);
        });


        // =====================================================
        // SEARCH
        // =====================================================

        edtSearchCustomer.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }


                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {

                        applyFilter();
                    }


                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                }
        );


        // =====================================================
        // ALL
        // =====================================================

        btnAll.setOnClickListener(v -> {

            currentFilter = "ALL";

            applyFilter();
        });


        // =====================================================
        // UNPAID
        // =====================================================

        btnUnpaid.setOnClickListener(v -> {

            currentFilter = "UNPAID";

            applyFilter();
        });


        // =====================================================
        // PARTIALLY PAID
        // =====================================================

        btnPartiallyPaid.setOnClickListener(v -> {

            currentFilter = "PARTIAL";

            applyFilter();
        });

        // =====================================================
        // OVERDUE
        // =====================================================

        if (btnOverdue != null) {
            btnOverdue.setOnClickListener(v -> {
                currentFilter = "OVERDUE";
                applyFilter();
            });
        }


        // =====================================================
        // BACK
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

            loadHistory();
        }
    }


    // =========================================================
    // LOAD HISTORY
    // =========================================================

    private void loadHistory() {

        allCustomers.clear();


        Cursor cursor =
                databaseHelper.getHistoryCustomers();


        if (cursor == null) {

            applyFilter();

            return;
        }


        try {

            int idIndex =
                    cursor.getColumnIndex(
                            "customer_id"
                    );


            int nameIndex =
                    cursor.getColumnIndex(
                            "customer_name"
                    );


            int phoneIndex =
                    cursor.getColumnIndex(
                            "customer_phone"
                    );


            int totalDebtIndex =
                    cursor.getColumnIndex(
                            "total_debt"
                    );


            int totalPaidIndex =
                    cursor.getColumnIndex(
                            "total_paid"
                    );


            while (cursor.moveToNext()) {

                int customerId =
                        cursor.getInt(
                                idIndex
                        );


                String name =
                        cursor.isNull(nameIndex)
                                ? ""
                                : cursor.getString(
                                nameIndex
                        );


                String phone =
                        cursor.isNull(phoneIndex)
                                ? ""
                                : cursor.getString(
                                phoneIndex
                        );


                double totalDebt =
                        cursor.getDouble(
                                totalDebtIndex
                        );


                double totalPaid =
                        cursor.getDouble(
                                totalPaidIndex
                        );


                double remaining =
                        totalDebt -
                                totalPaid;


                String status;

                // keep Paid/Partial/Unpaid for filter logic; overdue flagged separately
                if (remaining <= 0.001) {
                    status = "Paid";
                } else if (totalPaid > 0.001) {
                    status = "Partially Paid";
                } else {
                    status = "Unpaid";
                }

                // due/overdue resolved after cursor closed

                allCustomers.add(
                        new HistoryItem(
                                customerId,
                                name,
                                phone,
                                totalDebt,
                                totalPaid,
                                remaining,
                                status,
                                false,
                                null
                        )
                );
            }

        } finally {

            cursor.close();
        }

        // Resolve overdue after cursor closed
        for (HistoryItem h : allCustomers) {
            if (h.remaining > 0.001) {
                try {
                    String dueIso = databaseHelper.getEarliestDueDate(h.customerId);
                    h.dueDateIso = dueIso;
                    if (dueIso != null && !dueIso.trim().isEmpty()) {
                        h.isOverdue = DueDateUtils.isOverdue(dueIso);
                    }
                } catch (Exception ignored) {}
            }
        }


        updateFilterCounts();

        applyFilter();
    }


    // =========================================================
    // UPDATE COUNTS
    // =========================================================

    private void updateFilterCounts() {

        int allCount = 0;

        int unpaidCount = 0;

        int partialCount = 0;

        int overdueCount = 0;


        for (HistoryItem item :
                allCustomers) {

            allCount++;


            if (item.isOverdue) overdueCount++;

            if ("Unpaid".equalsIgnoreCase(
                    item.status
            )) {

                unpaidCount++;

            } else if (
                    "Partially Paid".equalsIgnoreCase(
                            item.status
                    )
            ) {

                partialCount++;
            }
        }


        btnAll.setText(
                "All • " +
                        allCount
        );


        btnUnpaid.setText(
                "Unpaid • " +
                        unpaidCount
        );


        btnPartiallyPaid.setText(
                "Partial • " +
                        partialCount
        );

        if (btnOverdue != null) {
            btnOverdue.setText(
                    "Overdue • " +
                            overdueCount
            );
        }
    }


    // =========================================================
    // APPLY FILTER
    // =========================================================

    private void applyFilter() {

        displayedCustomers.clear();


        String searchText =
                edtSearchCustomer == null
                        ? ""
                        : edtSearchCustomer
                          .getText()
                          .toString()
                          .trim()
                          .toLowerCase();


        for (HistoryItem item :
                allCustomers) {

            // -------------------------------------------------
            // SEARCH
            // -------------------------------------------------

            boolean matchesSearch;


            if (searchText.isEmpty()) {

                matchesSearch = true;

            } else {

                String name =
                        item.customerName
                                .toLowerCase();


                String phone =
                        item.customerPhone
                                .toLowerCase();


                matchesSearch =
                        name.contains(searchText)
                                ||
                                phone.contains(searchText);
            }


            if (!matchesSearch) {

                continue;
            }


            // -------------------------------------------------
            // FILTER
            // -------------------------------------------------

            boolean matchesFilter =
                    false;


            if ("ALL".equals(
                    currentFilter
            )) {

                matchesFilter = true;

            } else if (
                    "UNPAID".equals(
                            currentFilter
                    )
            ) {

                matchesFilter =
                        "Unpaid".equalsIgnoreCase(
                                item.status
                        );

            } else if (
                    "PARTIAL".equals(
                            currentFilter
                    )
            ) {

                matchesFilter =
                        "Partially Paid"
                                .equalsIgnoreCase(
                                        item.status
                                );
            } else if (
                    "OVERDUE".equals(
                            currentFilter
                    )
            ) {

                matchesFilter = item.isOverdue;
            }


            if (matchesFilter) {

                displayedCustomers.add(
                        item
                );
            }
        }


        txtCustomerCount.setText(
                String.valueOf(
                        displayedCustomers.size()
                )
        );

        adapter.notifyDataSetChanged();
    }


    // =========================================================
    // HISTORY ITEM
    // =========================================================

    private static class HistoryItem {

        int customerId;

        String customerName;

        String customerPhone;

        double totalDebt;

        double totalPaid;

        double remaining;

        String status;

        boolean isOverdue;

        String dueDateIso;


        HistoryItem(
                int customerId,
                String customerName,
                String customerPhone,
                double totalDebt,
                double totalPaid,
                double remaining,
                String status,
                boolean isOverdue,
                String dueDateIso
        ) {

            this.customerId =
                    customerId;

            this.customerName =
                    customerName;

            this.customerPhone =
                    customerPhone;

            this.totalDebt =
                    totalDebt;

            this.totalPaid =
                    totalPaid;

            this.remaining =
                    remaining;

            this.status =
                    status;

            this.isOverdue =
                    isOverdue;

            this.dueDateIso =
                    dueDateIso;
        }
    }


    // =========================================================
    // HISTORY ADAPTER
    // =========================================================

    private class HistoryAdapter
            extends BaseAdapter {


        @Override
        public int getCount() {

            return displayedCustomers.size();
        }


        @Override
        public Object getItem(
                int position
        ) {

            return displayedCustomers.get(
                    position
            );
        }


        @Override
        public long getItemId(
                int position
        ) {

            return displayedCustomers
                    .get(position)
                    .customerId;
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
                                        HistoryActivity.this
                                )
                                .inflate(
                                        R.layout.list_history,
                                        parent,
                                        false
                                );
            }


            TextView txtCustomerName =
                    convertView.findViewById(
                            R.id.txtCustomerName
                    );


            TextView txtPhone =
                    convertView.findViewById(
                            R.id.txtPhone
                    );


            TextView txtStatus =
                    convertView.findViewById(
                            R.id.txtStatus
                    );


            HistoryItem item =
                    displayedCustomers.get(
                            position
                    );


            // -------------------------------------------------
            // NAME
            // -------------------------------------------------

            txtCustomerName.setText(
                    item.customerName
            );


            // -------------------------------------------------
            // PHONE
            // -------------------------------------------------

            txtPhone.setText(
                    item.customerPhone
            );


            // -------------------------------------------------
            // STATUS
            // -------------------------------------------------

            txtStatus.setText(
                    item.status
            );


            // -------------------------------------------------
            // UNPAID
            // -------------------------------------------------

            if ("Unpaid".equalsIgnoreCase(
                    item.status
            )) {

                txtStatus.setTextColor(
                        Color.rgb(
                                240,
                                91,
                                91
                        )
                );


                txtStatus.setBackgroundResource(
                        R.drawable.bg_history_status
                );


                // -------------------------------------------------
                // PARTIALLY PAID
                // -------------------------------------------------

            } else if (
                    "Partially Paid".equalsIgnoreCase(
                            item.status
                    )
            ) {

                txtStatus.setTextColor(
                        Color.rgb(
                                53,
                                185,
                                107
                        )
                );


                txtStatus.setBackgroundResource(
                        R.drawable.bg_history_status_partial
                );


                // -------------------------------------------------
                // PAID
                // -------------------------------------------------

            } else {

                txtStatus.setTextColor(
                        Color.rgb(
                                88,
                                112,
                                217
                        )
                );


                txtStatus.setBackgroundResource(
                        R.drawable.bg_history_status_paid
                );
            }


            return convertView;
        }
    }
}