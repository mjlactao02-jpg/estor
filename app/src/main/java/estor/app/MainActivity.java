package estor.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
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