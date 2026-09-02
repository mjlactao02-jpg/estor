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

public class PaydeptActivity
        extends AppCompatActivity {

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
        // CREATE LIST
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
        // CLICK CUSTOMER
        // =====================================================

        listCustomers.setOnItemClickListener(
                (parent, view, position, id) -> {

                    HashMap<String, String>
                            customer =
                            customerList.get(
                                    position
                            );


                    String customerId =
                            customer.get(
                                    "id"
                            );


                    String customerName =
                            customer.get(
                                    "name"
                            );


                    String customerPhone =
                            customer.get(
                                    "phone"
                            );


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
    // REFRESH WHEN RETURNING
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
    // LOAD ONLY CUSTOMERS WITH DEBT
    // =========================================================

    private void loadCustomers() {

        customerList.clear();


        Cursor cursor =
                databaseHelper
                        .getCustomersWithRemainingDebt();


        if (cursor != null) {

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


            int debtIndex =
                    cursor.getColumnIndex(
                            "total_debt"
                    );


            while (cursor.moveToNext()) {

                String id = "";

                String name = "";

                String phone = "";

                double totalDebt = 0;


                // ------------------------------------------------
                // ID
                // ------------------------------------------------

                if (idIndex != -1 &&
                        !cursor.isNull(idIndex)) {

                    id =
                            cursor.getString(
                                    idIndex
                            );
                }


                // ------------------------------------------------
                // NAME
                // ------------------------------------------------

                if (nameIndex != -1 &&
                        !cursor.isNull(nameIndex)) {

                    name =
                            cursor.getString(
                                    nameIndex
                            );
                }


                // ------------------------------------------------
                // PHONE
                // ------------------------------------------------

                if (phoneIndex != -1 &&
                        !cursor.isNull(phoneIndex)) {

                    phone =
                            cursor.getString(
                                    phoneIndex
                            );
                }


                // ------------------------------------------------
                // TOTAL DEBT
                // ------------------------------------------------

                if (debtIndex != -1 &&
                        !cursor.isNull(debtIndex)) {

                    totalDebt =
                            cursor.getDouble(
                                    debtIndex
                            );
                }


                // ------------------------------------------------
                // EXTRA SAFETY CHECK
                // ------------------------------------------------
                //
                // A customer with ₱0 remaining is never added.
                //

                if (totalDebt <= 0.001) {

                    continue;
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

        txtCustomerCount.setText(
                String.valueOf(
                        customerList.size()
                )
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
                    customer.get(
                            "name"
                    )
            );


            txtPhone.setText(
                    customer.get(
                            "phone"
                    )
            );


            txtTotalDebt.setText(
                    "₱ " +
                            customer.get(
                                    "total_debt"
                            )
            );


            return convertView;
        }
    }
}