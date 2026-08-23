package estor.app;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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

public class Adddept2Activity extends AppCompatActivity {

    // Product input field.
    private EditText edtProduct;

    // Quantity input field.
    private EditText edtQuantity;

    // Amount input field.
    private EditText edtAmount;

    // Button used to temporarily add a product to the ListView.
    private Button btnAddDebt;

    // Button used to save all products.
    private Button btnConfirm;

    // Back button.
    private ImageButton btnBack;

    // ListView that displays the products added by the user.
    private ListView listViewDebt;

    // Database helper used to access SQLite.
    private DatabaseHelper databaseHelper;

    // Name of the customer selected from the first screen.
    private String customerName;

    // Phone number of the selected customer.
    private String customerPhone;

    // This list temporarily stores products before Confirm is clicked.
    private ArrayList<DebtItem> debtItems;

    // Adapter that displays debtItems inside the ListView.
    private DebtAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Open the Add Debt Item screen.
        setContentView(R.layout.adddept);

        // Create the database helper.
        databaseHelper = new DatabaseHelper(this);

        // Get the customer name sent from AdddeptActivity.
        customerName = getIntent().getStringExtra("customer_name");

        // Get the customer phone sent from AdddeptActivity.
        customerPhone = getIntent().getStringExtra("customer_phone");

        // Product input.
        edtProduct = findViewById(R.id.edtProduct);

        // Quantity input.
        edtQuantity = findViewById(R.id.edtQuantity);

        // Amount input.
        edtAmount = findViewById(R.id.edtAmount);

        // Add Debt button.
        btnAddDebt = findViewById(R.id.btnAddDebt);

        // Confirm button.
        btnConfirm = findViewById(R.id.btnConfirm);

        // Back button.
        btnBack = findViewById(R.id.btnBack);

        // ListView.
        listViewDebt = findViewById(R.id.listViewDebt);

        // Create an empty list. Products are stored here temporarily.
        debtItems = new ArrayList<>();

        // Create the adapter.
        adapter = new DebtAdapter(Adddept2Activity.this, debtItems);

        // Connect the adapter to the ListView.
        listViewDebt.setAdapter(adapter);

        // Back button listener.
        btnBack.setOnClickListener(v -> finish());

        // Add Debt button listener.
        btnAddDebt.setOnClickListener(v -> addDebtItem());

        // Confirm button listener.
        btnConfirm.setOnClickListener(v -> confirmDebt());
    }

    // Add product to ListView.
    private void addDebtItem() {
        String product = edtProduct.getText().toString().trim();
        String quantityText = edtQuantity.getText().toString().trim();
        String amountText = edtAmount.getText().toString().trim();

        if (product.isEmpty()) {
            edtProduct.setError("Enter product");
            edtProduct.requestFocus();
            return;
        }

        if (quantityText.isEmpty()) {
            edtQuantity.setError("Enter quantity");
            edtQuantity.requestFocus();
            return;
        }

        if (amountText.isEmpty()) {
            edtAmount.setError("Enter amount");
            edtAmount.requestFocus();
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityText);
        } catch (NumberFormatException e) {
            edtQuantity.setError("Enter a valid quantity");
            edtQuantity.requestFocus();
            return;
        }

        if (quantity <= 0) {
            edtQuantity.setError("Quantity must be greater than 0");
            edtQuantity.requestFocus();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            edtAmount.setError("Enter a valid amount");
            edtAmount.requestFocus();
            return;
        }

        if (amount <= 0) {
            edtAmount.setError("Amount must be greater than 0");
            edtAmount.requestFocus();
            return;
        }

        DebtItem item = new DebtItem(product, quantity, amount);
        debtItems.add(item);
        adapter.notifyDataSetChanged();

        edtProduct.setText("");
        edtQuantity.setText("");
        edtAmount.setText("");
        edtProduct.requestFocus();

        Toast.makeText(Adddept2Activity.this, "Debt item added", Toast.LENGTH_SHORT).show();
    }

    // Confirm all debt items.
    private void confirmDebt() {
        if (customerPhone == null || customerPhone.trim().isEmpty()) {
            Toast.makeText(Adddept2Activity.this, "Customer information is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        if (debtItems.isEmpty()) {
            Toast.makeText(Adddept2Activity.this, "Add at least one debt item", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        // Create debt_items table if needed.
        db.execSQL("CREATE TABLE IF NOT EXISTS debt_items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "customer_id INTEGER NOT NULL, " +
                "product TEXT NOT NULL, " +
                "quantity INTEGER NOT NULL, " +
                "amount REAL NOT NULL, " +
                "date TEXT NOT NULL" +
                ")");

        long customerId = -1;
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_CUSTOMERS,
                new String[]{DatabaseHelper.COLUMN_ID},
                DatabaseHelper.COLUMN_PHONE + "=?",
                new String[]{customerPhone},
                null, null, null
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                customerId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
            }
            cursor.close();
        }

        if (customerId == -1) {
            Toast.makeText(Adddept2Activity.this, "Customer was not found", Toast.LENGTH_SHORT).show();
            return;
        }

        db.beginTransaction();
        try {
            String currentDate = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(new java.util.Date());

            for (DebtItem item : debtItems) {
                ContentValues values = new ContentValues();
                values.put("customer_id", customerId);
                values.put("product", item.getProduct());
                values.put("quantity", item.getQuantity());
                values.put("amount", item.getAmount());
                values.put("date", currentDate);

                db.insertOrThrow("debt_items", null, values);
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Toast.makeText(Adddept2Activity.this, "Failed to save debt: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        } finally {
            db.endTransaction();
        }

        Toast.makeText(Adddept2Activity.this, "Debt saved successfully", Toast.LENGTH_SHORT).show();
        debtItems.clear();
        adapter.notifyDataSetChanged();
        finish();
    }

    // Debt item class.
    private static class DebtItem {
        private String product;
        private int quantity;
        private double amount;

        DebtItem(String product, int quantity, double amount) {
            this.product = product;
            this.quantity = quantity;
            this.amount = amount;
        }

        String getProduct() { return product; }
        int getQuantity() { return quantity; }
        double getAmount() { return amount; }
    }

    // ListView adapter.
    private static class DebtAdapter extends BaseAdapter {
        private final android.content.Context context;
        private final ArrayList<DebtItem> debtItems;

        DebtAdapter(android.content.Context context, ArrayList<DebtItem> debtItems) {
            this.context = context;
            this.debtItems = debtItems;
        }

        @Override
        public int getCount() { return debtItems.size(); }

        @Override
        public Object getItem(int position) { return debtItems.get(position); }

        @Override
        public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.dept_item, parent, false);
            }

            TextView txtProduct = convertView.findViewById(R.id.txtProduct);
            TextView txtQuantity = convertView.findViewById(R.id.txtQuantity);
            TextView txtAmount = convertView.findViewById(R.id.txtAmount);

            // Get the debt item.
            DebtItem item = debtItems.get(position);

            txtProduct.setText(item.getProduct());
            txtQuantity.setText("Qty: " + item.getQuantity());
            txtAmount.setText("₱ " + String.format(Locale.getDefault(), "%.2f", item.getAmount()));

            return convertView;
        }
    }
}
