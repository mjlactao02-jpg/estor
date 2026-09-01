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

    // Increase version because we are adding debt amount and paid amount
    private static final int DATABASE_VERSION = 4;


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

    // NEW:
    // Amount of the debt
    public static final String COLUMN_DEBT_AMOUNT = "amount";

    // NEW:
    // Amount already paid
    public static final String COLUMN_DEBT_PAID = "paid";


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
                "CREATE TABLE " + TABLE_CUSTOMERS + " (" +

                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        COLUMN_NAME + " TEXT NOT NULL, " +

                        COLUMN_PHONE + " TEXT NOT NULL, " +

                        COLUMN_DATE + " TEXT, " +

                        COLUMN_TIME + " TEXT" +

                        ")";

        db.execSQL(createCustomersTable);


        // -----------------------------------------------------
        // DEBTS TABLE
        // -----------------------------------------------------

        String createDebtsTable =
                "CREATE TABLE " + TABLE_DEBTS + " (" +

                        COLUMN_DEBT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        COLUMN_DEBT_CUSTOMER_ID + " INTEGER, " +

                        COLUMN_DEBT_CUSTOMER_NAME + " TEXT, " +

                        COLUMN_DEBT_CUSTOMER_PHONE + " TEXT, " +

                        COLUMN_DEBT_ITEM + " TEXT, " +

                        // Debt amount
                        COLUMN_DEBT_AMOUNT + " REAL DEFAULT 0, " +

                        // Amount already paid
                        COLUMN_DEBT_PAID + " REAL DEFAULT 0" +

                        ")";

        db.execSQL(createDebtsTable);
    }


    // =========================================================
    // UPDATE DATABASE
    // =========================================================

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Version 2 -> 3
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


        // Version 3 -> 4
        if (oldVersion < 4) {

            // Add debt amount
            db.execSQL(
                    "ALTER TABLE " +
                            TABLE_DEBTS +
                            " ADD COLUMN " +
                            COLUMN_DEBT_AMOUNT +
                            " REAL DEFAULT 0"
            );

            // Add amount paid
            db.execSQL(
                    "ALTER TABLE " +
                            TABLE_DEBTS +
                            " ADD COLUMN " +
                            COLUMN_DEBT_PAID +
                            " REAL DEFAULT 0"
            );
        }
    }


    // =========================================================
    // ADD CUSTOMER
    // =========================================================

    public long addCustomer(String name, String phone) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME, name);
        values.put(COLUMN_PHONE, phone);

        // Get current date
        String date =
                new SimpleDateFormat(
                        "MMM dd, yyyy",
                        Locale.getDefault()
                ).format(new Date());

        values.put(COLUMN_DATE, date);


        // Get current time
        String time =
                new SimpleDateFormat(
                        "hh:mm a",
                        Locale.getDefault()
                ).format(new Date());

        values.put(COLUMN_TIME, time);


        // Insert customer
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
            count = cursor.getInt(0);
        }

        cursor.close();

        return count;
    }


    // =========================================================
    // INSERT DEBT
    // =========================================================

    public long insertDebt(
            int customerId,
            String name,
            String phone,
            String item,
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

        // Store debt amount
        values.put(
                COLUMN_DEBT_AMOUNT,
                amount
        );

        // New debt has no payment yet
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
                total = cursor.getDouble(0);
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
            count = cursor.getInt(0);
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
                amount = cursor.getDouble(0);
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
            count = cursor.getInt(0);
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
                amount = cursor.getDouble(0);
            }
        }

        cursor.close();

        return amount;
    }
}