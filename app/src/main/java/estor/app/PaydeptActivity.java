package estor.app;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class PaydeptActivity extends AppCompatActivity {

    // =========================================================
    // VIEWS
    // =========================================================

    private ListView listCustomers;

    private TextView txtCustomerCount;

    private ImageButton btnBack;


    // =========================================================
    // DATABASE
    // =========================================================

    private DatabaseHelper databaseHelper;


    // =========================================================
    // CUSTOMER LIST
    // =========================================================

    private ArrayList<HashMap<String, String>>
            customerList;


    // =========================================================
    // ADAPTER
    // =========================================================

    private CustomerDebtAdapter adapter;


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);


        setContentView(
                R.layout.paydept_main
        );


        // =====================================================
        // DATABASE
        // =====================================================

        databaseHelper =
                new DatabaseHelper(this);


        // =====================================================
        // FIND VIEWS
        // =====================================================

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


        // =====================================================
        // CUSTOMER LIST
        // =====================================================

        customerList =
                new ArrayList<>();


        adapter =
                new CustomerDebtAdapter();


        listCustomers.setAdapter(
                adapter
        );


        // =====================================================
        // LOAD DATA
        // =====================================================

        loadCustomers();


        updateCustomerCount();


        // =====================================================
        // CUSTOMER CLICK
        // =====================================================

        listCustomers.setOnItemClickListener(
                (parent, view, position, id) -> {

                    HashMap<String, String>
                            customer =
                            customerList.get(
                                    position
                            );


                    // =========================================
                    // GET CUSTOMER DATA
                    // =========================================

                    String customerId =
                            customer.get("id");


                    String customerName =
                            customer.get("name");


                    String customerPhone =
                            customer.get("phone");


                    // =========================================
                    // OPEN PAYDEPT2
                    // =========================================

                    Intent intent =
                            new Intent(
                                    PaydeptActivity.this,
                                    Paydept2Activity.class
                            );


                    intent.putExtra(
                            "customer_id",
                            customerId
                    );


                    intent.putExtra(
                            "customer_name",
                            customerName
                    );


                    intent.putExtra(
                            "customer_phone",
                            customerPhone
                    );


                    startActivity(
                            intent
                    );
                }
        );


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

            loadCustomers();

            updateCustomerCount();
        }
    }


    // =========================================================
    // LOAD CUSTOMERS
    // =========================================================

    private void loadCustomers() {

        customerList.clear();


        Cursor cursor =
                databaseHelper
                        .getCustomersWithTotalDebt();


        if (cursor != null) {

            int idIndex =
                    cursor.getColumnIndex(
                            DatabaseHelper.COLUMN_ID
                    );


            int nameIndex =
                    cursor.getColumnIndex(
                            DatabaseHelper.COLUMN_NAME
                    );


            int phoneIndex =
                    cursor.getColumnIndex(
                            DatabaseHelper.COLUMN_PHONE
                    );


            int debtIndex =
                    cursor.getColumnIndex(
                            "total_debt"
                    );


            while (cursor.moveToNext()) {

                String id = "";

                String name = "";

                String phone = "";

                double totalDebt = 0;


                if (idIndex != -1 &&
                        !cursor.isNull(idIndex)) {

                    id =
                            cursor.getString(
                                    idIndex
                            );
                }


                if (nameIndex != -1 &&
                        !cursor.isNull(nameIndex)) {

                    name =
                            cursor.getString(
                                    nameIndex
                            );
                }


                if (phoneIndex != -1 &&
                        !cursor.isNull(phoneIndex)) {

                    phone =
                            cursor.getString(
                                    phoneIndex
                            );
                }


                if (debtIndex != -1 &&
                        !cursor.isNull(debtIndex)) {

                    totalDebt =
                            cursor.getDouble(
                                    debtIndex
                            );
                }


                HashMap<String, String>
                        customer =
                        new HashMap<>();


                customer.put(
                        "id",
                        id
                );


                customer.put(
                        "name",
                        name
                );


                customer.put(
                        "phone",
                        phone
                );


                customer.put(
                        "total_debt",
                        String.format(
                                Locale.getDefault(),
                                "%.2f",
                                totalDebt
                        )
                );


                customerList.add(
                        customer
                );
            }


            cursor.close();
        }


        adapter.notifyDataSetChanged();
    }


    // =========================================================
    // UPDATE COUNT
    // =========================================================

    private void updateCustomerCount() {

        int count =
                databaseHelper.getCustomerCount();


        txtCustomerCount.setText(
                String.valueOf(count)
        );
    }


    // =========================================================
    // ADAPTER
    // =========================================================

    private class CustomerDebtAdapter
            extends ArrayAdapter<HashMap<String, String>> {


        CustomerDebtAdapter() {

            super(
                    PaydeptActivity.this,
                    R.layout.item_paydept,
                    customerList
            );
        }


        @Override
        public View getView(
                int position,
                View convertView,
                ViewGroup parent
        ) {

            if (convertView == null) {

                convertView =
                        getLayoutInflater()
                                .inflate(
                                        R.layout.item_paydept,
                                        parent,
                                        false
                                );
            }


            TextView txtName =
                    convertView.findViewById(
                            R.id.txtCustomerName
                    );


            TextView txtPhone =
                    convertView.findViewById(
                            R.id.txtCustomerPhone
                    );


            TextView txtTotalDebt =
                    convertView.findViewById(
                            R.id.txtTotalDebt
                    );


            HashMap<String, String>
                    customer =
                    customerList.get(
                            position
                    );


            txtName.setText(
                    customer.get("name")
            );


            txtPhone.setText(
                    customer.get("phone")
            );


            String totalDebt =
                    customer.get(
                            "total_debt"
                    );


            if (totalDebt == null) {

                totalDebt = "0.00";
            }


            txtTotalDebt.setText(
                    "₱ " +
                            totalDebt
            );


            return convertView;
        }
    }
}