package estor.app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class Adddept2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Open the Add Debt Item screen
        setContentView(R.layout.adddept);

        // Get the customer information
        // that was sent from AdddeptActivity.
        String customerName =
                getIntent().getStringExtra("customer_name");

        String customerPhone =
                getIntent().getStringExtra("customer_phone");

        // You can use customerName and customerPhone
        // later when you add TextViews to adddept.xml.
    }
}