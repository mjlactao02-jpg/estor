package estor.app;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // Buttons
    View button2, button3, button4, button5;

    ImageButton button1;


    // Dashboard TextViews
    TextView txtBalance;
    TextView txtUnpaidCount;
    TextView txtUnpaidAmount;
    TextView txtPartialCount;
    TextView txtPartialAmount;
    TextView txtOverdueCount;
    TextView txtOverdueAmount;
    LinearLayout cardOverdue;
    LinearLayout dueSoonContainer;
    TextView txtDueSoonEmpty;


    // Database
    DatabaseHelper databaseHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        // =====================================================
        // CONNECT DATABASE
        // =====================================================

        databaseHelper =
                new DatabaseHelper(this);


        // =====================================================
        // CONNECT BUTTONS
        // =====================================================

        button1 =
                findViewById(R.id.btnSettings);

        button2 =
                findViewById(R.id.btnAddDebt);

        button3 =
                findViewById(R.id.btnPayDebt);

        button4 =
                findViewById(R.id.btnTransaction);

        button5 =
                findViewById(R.id.btnHistory);


        // =====================================================
        // CONNECT DASHBOARD TEXT
        // =====================================================

        txtBalance =
                findViewById(R.id.txtBalance);

        txtUnpaidCount =
                findViewById(R.id.txtUnpaidCount);

        txtUnpaidAmount =
                findViewById(R.id.txtUnpaidAmount);

        txtPartialCount =
                findViewById(R.id.txtPartialCount);

        txtPartialAmount =
                findViewById(R.id.txtPartialAmount);

        txtOverdueCount =
                findViewById(R.id.txtOverdueCount);

        txtOverdueAmount =
                findViewById(R.id.txtOverdueAmount);

        cardOverdue =
                findViewById(R.id.cardOverdue);

        dueSoonContainer =
                findViewById(R.id.dueSoonContainer);

        txtDueSoonEmpty =
                findViewById(R.id.txtDueSoonEmpty);


        // =====================================================
        // LOAD TOTAL DEBT
        // =====================================================

        loadDashboard();


        // =====================================================
        // SETTINGS
        // =====================================================

        button1.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            SettingsActivity.class
                    );

            startActivity(intent);
        });


        // =====================================================
        // ADD DEBT
        // =====================================================

        button2.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            AdddeptActivity.class
                    );

            startActivity(intent);
        });


        // =====================================================
        // PAY DEBT
        // =====================================================

        button3.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            PaydeptActivity.class
                    );

            startActivity(intent);
        });


        // =====================================================
        // TRANSACTION
        // =====================================================

        button4.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            TransactionActivity.class
                    );

            startActivity(intent);
        });


        // =====================================================
        // HISTORY
        // =====================================================

        button5.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            HistoryActivity.class
                    );

            startActivity(intent);
        });

        // =====================================================
        // OVERDUE CARD -> PAY DEPT (or history overdue)
        // =====================================================

        if (cardOverdue != null) {
            cardOverdue.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, PaydeptActivity.class);
                startActivity(intent);
            });
        }
    }


    // =========================================================
    // LOAD DASHBOARD
    // =========================================================

    private void loadDashboard() {

        // -----------------------------------------------------
        // TOTAL REMAINING DEBT
        // -----------------------------------------------------

        double totalDebt =
                databaseHelper.getTotalDebt();


        // -----------------------------------------------------
        // UNPAID
        // -----------------------------------------------------

        int unpaidCount =
                databaseHelper.getUnpaidCustomerCount();

        double unpaidAmount =
                databaseHelper.getUnpaidAmount();


        // -----------------------------------------------------
        // PARTIAL
        // -----------------------------------------------------

        int partialCount =
                databaseHelper.getPartialCustomerCount();

        double partialAmount =
                databaseHelper.getPartialAmount();


        // -----------------------------------------------------
        // DISPLAY TOTAL DEBT
        // -----------------------------------------------------

        txtBalance.setText(
                String.format(
                        Locale.getDefault(),
                        "₱%.2f",
                        totalDebt
                )
        );


        // -----------------------------------------------------
        // DISPLAY UNPAID CUSTOMER COUNT
        // -----------------------------------------------------

        txtUnpaidCount.setText(
                unpaidCount +
                        (unpaidCount == 1
                                ? " Customer"
                                : " Customers")
        );


        // -----------------------------------------------------
        // DISPLAY UNPAID AMOUNT
        // -----------------------------------------------------

        txtUnpaidAmount.setText(
                String.format(
                        Locale.getDefault(),
                        "₱%.2f",
                        unpaidAmount
                )
        );


        // -----------------------------------------------------
        // DISPLAY PARTIAL CUSTOMER COUNT
        // -----------------------------------------------------

        txtPartialCount.setText(
                partialCount +
                        (partialCount == 1
                                ? " Customer"
                                : " Customers")
        );


        // -----------------------------------------------------
        // DISPLAY PARTIAL AMOUNT
        // -----------------------------------------------------

        txtPartialAmount.setText(
                String.format(
                        Locale.getDefault(),
                        "₱%.2f",
                        partialAmount
                )
        );

        // -----------------------------------------------------
        // OVERDUE
        // -----------------------------------------------------

        int overdueCount = databaseHelper.getOverdueCustomerCount();
        double overdueAmount = databaseHelper.getOverdueAmount();

        if (txtOverdueCount != null) {
            txtOverdueCount.setText(
                    overdueCount + (overdueCount == 1 ? " Customer" : " Customers")
            );
        }

        if (txtOverdueAmount != null) {
            txtOverdueAmount.setText(
                    String.format(Locale.getDefault(), "₱%.2f", overdueAmount)
            );
        }

        // -----------------------------------------------------
        // DUE SOON (nearest 3)
        // -----------------------------------------------------

        loadDueSoon();
    }

    private void loadDueSoon() {
        if (dueSoonContainer == null) return;

        dueSoonContainer.removeAllViews();

        Cursor cursor = null;
        try {
            cursor = databaseHelper.getDueSoonCustomers(3);

            int count = 0;
            if (cursor != null && cursor.moveToFirst()) {
                int nameIdx = cursor.getColumnIndex("customer_name");
                int dueIdx = cursor.getColumnIndex("earliest_due");
                int debtIdx = cursor.getColumnIndex("total_debt");
                int idIdx = cursor.getColumnIndex("customer_id");
                int phoneIdx = cursor.getColumnIndex("customer_phone");

                do {
                    String name = nameIdx != -1 && !cursor.isNull(nameIdx) ? cursor.getString(nameIdx) : "Unknown";
                    String dueIso = dueIdx != -1 && !cursor.isNull(dueIdx) ? cursor.getString(dueIdx) : "";
                    double debt = debtIdx != -1 ? cursor.getDouble(debtIdx) : 0;
                    int custId = idIdx != -1 ? cursor.getInt(idIdx) : -1;
                    String phone = phoneIdx != -1 && !cursor.isNull(phoneIdx) ? cursor.getString(phoneIdx) : "";

                    String dueDisp = DueDateUtils.formatForDisplay(dueIso);
                    boolean overdue = DueDateUtils.isOverdue(dueIso);

                    LinearLayout row = new LinearLayout(this);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setGravity(Gravity.CENTER_VERTICAL);
                    row.setPadding(6, 8, 6, 8);

                    TextView txtName = new TextView(this);
                    LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                    txtName.setLayoutParams(lp1);
                    txtName.setText(name);
                    txtName.setTextColor(0xFF222222);
                    txtName.setTextSize(14f);
                    txtName.setMaxLines(1);
                    txtName.setEllipsize(android.text.TextUtils.TruncateAt.END);

                    TextView txtDue = new TextView(this);
                    txtDue.setText(overdue ? dueDisp + " • OVERDUE" : dueDisp);
                    txtDue.setTextColor(overdue ? 0xFFF05B5B : 0xFF6C63FF);
                    txtDue.setTextSize(12f);
                    txtDue.setPadding(8, 0, 8, 0);

                    TextView txtAmt = new TextView(this);
                    txtAmt.setText(String.format(Locale.getDefault(), "₱%.2f", debt));
                    txtAmt.setTextColor(0xFF222222);
                    txtAmt.setTextSize(13f);
                    txtAmt.setMinWidth(90);
                    txtAmt.setGravity(Gravity.END);

                    row.addView(txtName);
                    row.addView(txtDue);
                    row.addView(txtAmt);

                    final int fId = custId;
                    final String fName = name;
                    final String fPhone = phone;
                    row.setClickable(true);
                    row.setFocusable(true);
                    row.setBackgroundResource(android.R.drawable.list_selector_background);
                    row.setOnClickListener(v -> {
                        Intent intent = new Intent(MainActivity.this, Paydept2Activity.class);
                        intent.putExtra("customer_id", String.valueOf(fId));
                        intent.putExtra("customer_name", fName);
                        intent.putExtra("customer_phone", fPhone);
                        startActivity(intent);
                    });

                    dueSoonContainer.addView(row);

                    // divider
                    if (count < 2) {
                        View div = new View(this);
                        div.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
                        div.setBackgroundColor(0xFFEEEEEE);
                        dueSoonContainer.addView(div);
                    }

                    count++;
                    if (count >= 3) break;
                } while (cursor.moveToNext());
            }

            if (txtDueSoonEmpty != null) {
                boolean empty = dueSoonContainer.getChildCount() == 0;
                txtDueSoonEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                dueSoonContainer.setVisibility(empty ? View.GONE : View.VISIBLE);
            }

        } finally {
            if (cursor != null) cursor.close();
        }
    }


    // =========================================================
    // REFRESH WHEN RETURNING TO MAIN ACTIVITY
    // =========================================================

    @Override
    protected void onResume() {

        super.onResume();

        // Reload the totals every time MainActivity appears
        if (databaseHelper != null) {
            loadDashboard();
        }
    }
}