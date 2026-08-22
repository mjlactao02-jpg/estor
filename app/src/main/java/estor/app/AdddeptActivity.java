package estor.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.widget.ImageButton;
import android.widget.Toast;

public class AdddeptActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adddept_main);

        // ==== ADD to your Activity's onCreate(), after existing findViewById calls ====

        View overlayDim = findViewById(R.id.overlayDim);
        View addDebtSheet = findViewById(R.id.includeAddDebtSheet);
        EditText etName = addDebtSheet.findViewById(R.id.etName);
        EditText etPhone = addDebtSheet.findViewById(R.id.etPhone);
        Button btnConfirm = addDebtSheet.findViewById(R.id.btnConfirm);
        Button btnAddDebt = findViewById(R.id.btnAddDebt);
        ImageButton btnBack = findViewById(R.id.btnBack);


// Show the sheet when "Add Debt" is tapped
        btnAddDebt.setOnClickListener(v -> {
            overlayDim.setVisibility(View.VISIBLE);
            addDebtSheet.setVisibility(View.VISIBLE);
        });

// Dismiss when tapping outside the sheet
        overlayDim.setOnClickListener(v -> {
            overlayDim.setVisibility(View.GONE);
            addDebtSheet.setVisibility(View.GONE);
        });

// Confirm button: read inputs, add to list, close sheet
        btnConfirm.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: add (name, phone) to your listCustomers adapter/data source here
            // e.g. customerList.add(new Customer(name, phone, 0.0));
            // adapter.notifyDataSetChanged();
            // txtCustomerCount.setText(String.valueOf(customerList.size()));

            etName.setText("");
            etPhone.setText("");
            overlayDim.setVisibility(View.GONE);
            addDebtSheet.setVisibility(View.GONE);
        });
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(AdddeptActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }
}