package estor.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    // =========================================================
    // DATABASE
    // =========================================================

    private static final String DATABASE_NAME = "estor.db";

    // Version 5 because quantity is being added to debts
    private static final int DATABASE_VERSION = 5;


    // =========================================================
    // CUSTOMERS TABLE
    // =========================================================

    public static final String TABLE_CUSTOMERS = "customers";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_TIME = "time";


    // =========================================================
    // DEBTS TABLE
    // =========================================================

    public static final String TABLE_DEBTS = "debts";

    public static final String COLUMN_DEBT_ID = "debt_id";
    public static final String COLUMN_DEBT_CUSTOMER_ID = "customer_id";
    public static final String COLUMN_DEBT_CUSTOMER_NAME = "customer_name";
    public static final String COLUMN_DEBT_CUSTOMER_PHONE = "customer_phone";
    public static final String COLUMN_DEBT_ITEM = "item";

    // Quantity of the product
    public static final String COLUMN_DEBT_QUANTITY = "quantity";

    // Total amount for the debt item
    public static final String COLUMN_DEBT_AMOUNT = "amount";

    // Amount already paid
    public static final String COLUMN_DEBT_PAID = "paid";


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public DatabaseHelper(Context context) {

        super(
                context,
                DATABASE_NAME,
                null,
                DATABASE_VERSION
        );
    }


    // =========================================================
    // CREATE DATABASE
    // =========================================================

    @Override
    public void onCreate(SQLiteDatabase db) {

        // -----------------------------------------------------
        // CUSTOMERS TABLE
        // -----------------------------------------------------

        String createCustomersTable =
                "CREATE TABLE " +
                        TABLE_CUSTOMERS +
                        " (" +

                        COLUMN_ID +
                        " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        COLUMN_NAME +
                        " TEXT NOT NULL, " +

                        COLUMN_PHONE +
                        " TEXT NOT NULL, " +

                        COLUMN_DATE +
                        " TEXT, " +

                        COLUMN_TIME +
                        " TEXT" +

                        ")";

        db.execSQL(createCustomersTable);


        // -----------------------------------------------------
        // DEBTS TABLE
        // -----------------------------------------------------

        String createDebtsTable =
                "CREATE TABLE " +
                        TABLE_DEBTS +
                        " (" +

                        COLUMN_DEBT_ID +
                        " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        COLUMN_DEBT_CUSTOMER_ID +
                        " INTEGER, " +

                        COLUMN_DEBT_CUSTOMER_NAME +
                        " TEXT, " +

                        COLUMN_DEBT_CUSTOMER_PHONE +
                        " TEXT, " +

                        COLUMN_DEBT_ITEM +
                        " TEXT, " +

                        COLUMN_DEBT_QUANTITY +
                        " INTEGER DEFAULT 1, " +

                        COLUMN_DEBT_AMOUNT +
                        " REAL DEFAULT 0, " +

                        COLUMN_DEBT_PAID +
                        " REAL DEFAULT 0" +

                        ")";

        db.execSQL(createDebtsTable);
    }


    // =========================================================
    // DATABASE UPGRADE
    // =========================================================

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {

        // -----------------------------------------------------
        // VERSION 2 -> 3
        // -----------------------------------------------------

        if (oldVersion < 3) {

            db.execSQL(
                    "ALTER TABLE " +
                            TABLE_CUSTOMERS +
                            " ADD COLUMN " +
                            COLUMN_DATE +
                            " TEXT"
            );

            db.execSQL(
                    "ALTER TABLE " +
                            TABLE_CUSTOMERS +
                            " ADD COLUMN " +
                            COLUMN_TIME +
                            " TEXT"
            );
        }


        // -----------------------------------------------------
        // VERSION 3 -> 4
        // -----------------------------------------------------

        if (oldVersion < 4) {

            db.execSQL(
                    "ALTER TABLE " +
                            TABLE_DEBTS +
                            " ADD COLUMN " +
                            COLUMN_DEBT_AMOUNT +
                            " REAL DEFAULT 0"
            );

            db.execSQL(
                    "ALTER TABLE " +
                            TABLE_DEBTS +
                            " ADD COLUMN " +
                            COLUMN_DEBT_PAID +
                            " REAL DEFAULT 0"
            );
        }


        // -----------------------------------------------------
        // VERSION 4 -> 5
        // Add quantity
        // -----------------------------------------------------

        if (oldVersion < 5) {

            db.execSQL(
                    "ALTER TABLE " +
                            TABLE_DEBTS +
                            " ADD COLUMN " +
                            COLUMN_DEBT_QUANTITY +
                            " INTEGER DEFAULT 1"
            );
        }
    }


    // =========================================================
    // ADD CUSTOMER
    // =========================================================

    public long addCustomer(
            String name,
            String phone
    ) {

        SQLiteDatabase db =
                this.getWritableDatabase();

        ContentValues values =
                new ContentValues();


        values.put(
                COLUMN_NAME,
                name
        );


        values.put(
                COLUMN_PHONE,
                phone
        );


        String date =
                new SimpleDateFormat(
                        "MMM dd, yyyy",
                        Locale.getDefault()
                ).format(new Date());


        values.put(
                COLUMN_DATE,
                date
        );


        String time =
                new SimpleDateFormat(
                        "hh:mm a",
                        Locale.getDefault()
                ).format(new Date());


        values.put(
                COLUMN_TIME,
                time
        );


        long result =
                db.insert(
                        TABLE_CUSTOMERS,
                        null,
                        values
                );


        db.close();

        return result;
    }


    // =========================================================
    // GET ALL CUSTOMERS
    // =========================================================

    public Cursor getAllCustomers() {

        SQLiteDatabase db =
                this.getReadableDatabase();

        return db.query(
                TABLE_CUSTOMERS,
                null,
                null,
                null,
                null,
                null,
                COLUMN_ID + " DESC"
        );
    }


    // =========================================================
    // GET CUSTOMER COUNT
    // =========================================================

    public int getCustomerCount() {

        SQLiteDatabase db =
                this.getReadableDatabase();

        Cursor cursor =
                db.rawQuery(
                        "SELECT COUNT(*) FROM " +
                                TABLE_CUSTOMERS,
                        null
                );

        int count = 0;

        if (cursor.moveToFirst()) {

            count =
                    cursor.getInt(0);
        }

        cursor.close();

        return count;
    }


    // =========================================================
    // INSERT DEBT
    // =========================================================
    //
    // New version:
    //
    // customer ID
    // customer name
    // phone
    // product
    // quantity
    // total line amount
    //
    // Example:
    //
    // Coke
    // quantity = 3
    // price = 20
    // amount = 60
    //
    // =========================================================

    public long insertDebt(
            int customerId,
            String name,
            String phone,
            String item,
            int quantity,
            double amount
    ) {

        SQLiteDatabase db =
                this.getWritableDatabase();

        ContentValues values =
                new ContentValues();


        values.put(
                COLUMN_DEBT_CUSTOMER_ID,
                customerId
        );


        values.put(
                COLUMN_DEBT_CUSTOMER_NAME,
                name
        );


        values.put(
                COLUMN_DEBT_CUSTOMER_PHONE,
                phone
        );


        values.put(
                COLUMN_DEBT_ITEM,
                item
        );


        // Save quantity
        values.put(
                COLUMN_DEBT_QUANTITY,
                quantity
        );


        // Save total line amount
        values.put(
                COLUMN_DEBT_AMOUNT,
                amount
        );


        // New debt = nothing paid
        values.put(
                COLUMN_DEBT_PAID,
                0
        );


        long result =
                db.insert(
                        TABLE_DEBTS,
                        null,
                        values
                );


        db.close();

        return result;
    }


    // =========================================================
    // OLD INSERT DEBT METHOD
    // =========================================================
    //
    // This is kept so existing code that uses the old method
    // will not immediately break.
    //
    // Quantity defaults to 1.
    //
    // =========================================================

    public long insertDebt(
            int customerId,
            String name,
            String phone,
            String item,
            double amount
    ) {

        return insertDebt(
                customerId,
                name,
                phone,
                item,
                1,
                amount
        );
    }


    // =========================================================
    // GET CUSTOMERS WITH TOTAL DEBT
    // =========================================================

    public Cursor getCustomersWithTotalDebt() {

        SQLiteDatabase db =
                this.getReadableDatabase();


        String query =
                "SELECT " +

                        "c." +
                        COLUMN_ID +
                        " AS " +
                        COLUMN_ID +
                        ", " +

                        "c." +
                        COLUMN_NAME +
                        " AS " +
                        COLUMN_NAME +
                        ", " +

                        "c." +
                        COLUMN_PHONE +
                        " AS " +
                        COLUMN_PHONE +
                        ", " +

                        "COALESCE(" +

                        "SUM(" +

                        "CASE " +

                        "WHEN d." +
                        COLUMN_DEBT_AMOUNT +
                        " - d." +
                        COLUMN_DEBT_PAID +
                        " > 0 " +

                        "THEN d." +
                        COLUMN_DEBT_AMOUNT +
                        " - d." +
                        COLUMN_DEBT_PAID +
                        " ELSE 0 " +

                        "END" +

                        "), 0) AS total_debt " +

                        "FROM " +
                        TABLE_CUSTOMERS +
                        " c " +

                        "LEFT JOIN " +
                        TABLE_DEBTS +
                        " d " +

                        "ON c." +
                        COLUMN_ID +
                        " = d." +
                        COLUMN_DEBT_CUSTOMER_ID +

                        " GROUP BY " +

                        "c." +
                        COLUMN_ID +
                        ", " +

                        "c." +
                        COLUMN_NAME +
                        ", " +

                        "c." +
                        COLUMN_PHONE +

                        " ORDER BY " +

                        "c." +
                        COLUMN_ID +
                        " DESC";


        return db.rawQuery(
                query,
                null
        );
    }


    // =========================================================
    // GET DEBT ITEMS FOR CUSTOMER
    // =========================================================

    public Cursor getDebtItemsForCustomer(
            int customerId
    ) {

        SQLiteDatabase db =
                this.getReadableDatabase();


        String query =
                "SELECT " +

                        COLUMN_DEBT_ID +
                        ", " +

                        COLUMN_DEBT_ITEM +
                        ", " +

                        COLUMN_DEBT_QUANTITY +
                        ", " +

                        COLUMN_DEBT_AMOUNT +
                        ", " +

                        COLUMN_DEBT_PAID +

                        " FROM " +
                        TABLE_DEBTS +

                        " WHERE " +
                        COLUMN_DEBT_CUSTOMER_ID +
                        " = ? " +

                        " AND (" +
                        COLUMN_DEBT_AMOUNT +
                        " - " +
                        COLUMN_DEBT_PAID +
                        ") > 0 " +

                        " ORDER BY " +
                        COLUMN_DEBT_ID +
                        " ASC";


        return db.rawQuery(
                query,
                new String[]{
                        String.valueOf(customerId)
                }
        );
    }


    // =========================================================
    // GET CUSTOMER TOTAL DEBT
    // =========================================================

    public double getCustomerTotalDebt(
            int customerId
    ) {

        SQLiteDatabase db =
                this.getReadableDatabase();


        Cursor cursor =
                db.rawQuery(

                        "SELECT COALESCE(" +
                                "SUM(" +
                                COLUMN_DEBT_AMOUNT +
                                " - " +
                                COLUMN_DEBT_PAID +
                                ")" +
                                ", 0) " +

                                "FROM " +
                                TABLE_DEBTS +

                                " WHERE " +
                                COLUMN_DEBT_CUSTOMER_ID +
                                " = ? " +

                                " AND (" +
                                COLUMN_DEBT_AMOUNT +
                                " - " +
                                COLUMN_DEBT_PAID +
                                ") > 0",

                        new String[]{
                                String.valueOf(customerId)
                        }
                );


        double total = 0;


        if (cursor.moveToFirst()) {

            total =
                    cursor.getDouble(0);
        }


        cursor.close();

        return total;
    }


    // =========================================================
    // MAKE PAYMENT
    // =========================================================
    //
    // The payment is applied to the customer's oldest debts
    // first.
    //
    // Example:
    //
    // Debt 1 = 40
    // Debt 2 = 50
    // Debt 3 = 100
    //
    // Payment = 60
    //
    // Result:
    //
    // Debt 1 = fully paid
    // Debt 2 = paid 20
    // Debt 3 = unchanged
    //
    // =========================================================

    public boolean makePayment(
            int customerId,
            double paymentAmount
    ) {

        if (paymentAmount <= 0) {

            return false;
        }


        SQLiteDatabase db =
                this.getWritableDatabase();


        Cursor cursor = null;


        boolean success = false;


        try {

            db.beginTransaction();


            // -------------------------------------------------
            // Get all unpaid debt items
            // -------------------------------------------------

            cursor =
                    db.query(

                            TABLE_DEBTS,

                            new String[]{
                                    COLUMN_DEBT_ID,
                                    COLUMN_DEBT_AMOUNT,
                                    COLUMN_DEBT_PAID
                            },

                            COLUMN_DEBT_CUSTOMER_ID +
                                    "=? AND (" +
                                    COLUMN_DEBT_AMOUNT +
                                    " - " +
                                    COLUMN_DEBT_PAID +
                                    ") > 0",

                            new String[]{
                                    String.valueOf(
                                            customerId
                                    )
                            },

                            null,
                            null,

                            COLUMN_DEBT_ID +
                                    " ASC"
                    );


            double remainingPayment =
                    paymentAmount;


            // -------------------------------------------------
            // Apply payment from oldest debt to newest
            // -------------------------------------------------

            while (
                    cursor.moveToNext()
                            &&
                            remainingPayment > 0
            ) {

                long debtId =
                        cursor.getLong(
                                cursor.getColumnIndexOrThrow(
                                        COLUMN_DEBT_ID
                                )
                        );


                double amount =
                        cursor.getDouble(
                                cursor.getColumnIndexOrThrow(
                                        COLUMN_DEBT_AMOUNT
                                )
                        );


                double paid =
                        cursor.getDouble(
                                cursor.getColumnIndexOrThrow(
                                        COLUMN_DEBT_PAID
                                )
                        );


                double balance =
                        amount - paid;


                if (balance <= 0) {

                    continue;
                }


                // How much goes into this debt
                double paymentForThisDebt =
                        Math.min(
                                remainingPayment,
                                balance
                        );


                double newPaid =
                        paid +
                                paymentForThisDebt;


                ContentValues values =
                        new ContentValues();


                values.put(
                        COLUMN_DEBT_PAID,
                        newPaid
                );


                int updated =
                        db.update(

                                TABLE_DEBTS,

                                values,

                                COLUMN_DEBT_ID +
                                        "=?",

                                new String[]{
                                        String.valueOf(
                                                debtId
                                        )
                                }
                        );


                if (updated <= 0) {

                    throw new Exception(
                            "Payment update failed"
                    );
                }


                remainingPayment -=
                        paymentForThisDebt;
            }


            // -------------------------------------------------
            // Make sure the payment was not larger than debt
            // -------------------------------------------------

            double totalDebtAfter =
                    getCustomerTotalDebtFromDatabase(
                            db,
                            customerId
                    );


            // Small floating point tolerance
            if (remainingPayment > 0.001) {

                throw new Exception(
                        "Payment exceeds customer debt"
                );
            }


            db.setTransactionSuccessful();

            success = true;


        } catch (Exception e) {

            success = false;


        } finally {

            if (cursor != null) {

                cursor.close();
            }


            db.endTransaction();

            db.close();
        }


        return success;
    }


    // =========================================================
    // INTERNAL TOTAL DEBT METHOD
    // =========================================================

    private double getCustomerTotalDebtFromDatabase(
            SQLiteDatabase db,
            int customerId
    ) {

        Cursor cursor =
                db.rawQuery(

                        "SELECT COALESCE(" +
                                "SUM(" +
                                COLUMN_DEBT_AMOUNT +
                                " - " +
                                COLUMN_DEBT_PAID +
                                ")" +
                                ", 0) " +

                                "FROM " +
                                TABLE_DEBTS +

                                " WHERE " +
                                COLUMN_DEBT_CUSTOMER_ID +
                                " = ? " +

                                " AND (" +
                                COLUMN_DEBT_AMOUNT +
                                " - " +
                                COLUMN_DEBT_PAID +
                                ") > 0",

                        new String[]{
                                String.valueOf(
                                        customerId
                                )
                        }
                );


        double total = 0;


        if (cursor.moveToFirst()) {

            total =
                    cursor.getDouble(0);
        }


        cursor.close();

        return total;
    }


    // =========================================================
    // GET TOTAL DEBT
    // =========================================================

    public double getTotalDebt() {

        SQLiteDatabase db =
                this.getReadableDatabase();


        Cursor cursor =
                db.rawQuery(

                        "SELECT SUM(" +
                                COLUMN_DEBT_AMOUNT +
                                " - " +
                                COLUMN_DEBT_PAID +
                                ") FROM " +
                                TABLE_DEBTS,

                        null
                );


        double total = 0;


        if (cursor.moveToFirst()) {

            if (!cursor.isNull(0)) {

                total =
                        cursor.getDouble(0);
            }
        }


        cursor.close();

        return total;
    }


    // =========================================================
    // GET UNPAID CUSTOMER COUNT
    // =========================================================

    public int getUnpaidCustomerCount() {

        SQLiteDatabase db =
                this.getReadableDatabase();


        Cursor cursor =
                db.rawQuery(

                        "SELECT COUNT(DISTINCT " +
                                COLUMN_DEBT_CUSTOMER_ID +
                                ") FROM " +
                                TABLE_DEBTS +

                                " WHERE " +
                                COLUMN_DEBT_PAID +
                                " = 0",

                        null
                );


        int count = 0;


        if (cursor.moveToFirst()) {

            count =
                    cursor.getInt(0);
        }


        cursor.close();

        return count;
    }


    // =========================================================
    // GET UNPAID AMOUNT
    // =========================================================

    public double getUnpaidAmount() {

        SQLiteDatabase db =
                this.getReadableDatabase();


        Cursor cursor =
                db.rawQuery(

                        "SELECT SUM(" +
                                COLUMN_DEBT_AMOUNT +
                                ") FROM " +
                                TABLE_DEBTS +

                                " WHERE " +
                                COLUMN_DEBT_PAID +
                                " = 0",

                        null
                );


        double amount = 0;


        if (cursor.moveToFirst()) {

            if (!cursor.isNull(0)) {

                amount =
                        cursor.getDouble(0);
            }
        }


        cursor.close();

        return amount;
    }


    // =========================================================
    // GET PARTIAL CUSTOMER COUNT
    // =========================================================

    public int getPartialCustomerCount() {

        SQLiteDatabase db =
                this.getReadableDatabase();


        Cursor cursor =
                db.rawQuery(

                        "SELECT COUNT(DISTINCT " +
                                COLUMN_DEBT_CUSTOMER_ID +
                                ") FROM " +
                                TABLE_DEBTS +

                                " WHERE " +
                                COLUMN_DEBT_PAID +
                                " > 0 AND " +
                                COLUMN_DEBT_PAID +
                                " < " +
                                COLUMN_DEBT_AMOUNT,

                        null
                );


        int count = 0;


        if (cursor.moveToFirst()) {

            count =
                    cursor.getInt(0);
        }


        cursor.close();

        return count;
    }


    // =========================================================
    // GET PARTIAL REMAINING AMOUNT
    // =========================================================

    public double getPartialAmount() {

        SQLiteDatabase db =
                this.getReadableDatabase();


        Cursor cursor =
                db.rawQuery(

                        "SELECT SUM(" +
                                COLUMN_DEBT_AMOUNT +
                                " - " +
                                COLUMN_DEBT_PAID +
                                ") FROM " +
                                TABLE_DEBTS +

                                " WHERE " +
                                COLUMN_DEBT_PAID +
                                " > 0 AND " +
                                COLUMN_DEBT_PAID +
                                " < " +
                                COLUMN_DEBT_AMOUNT,

                        null
                );


        double amount = 0;


        if (cursor.moveToFirst()) {

            if (!cursor.isNull(0)) {

                amount =
                        cursor.getDouble(0);
            }
        }


        cursor.close();

        return amount;
    }
}