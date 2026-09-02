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

    // Version 6 adds the transactions table.
    private static final int DATABASE_VERSION = 6;


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
    public static final String COLUMN_DEBT_QUANTITY = "quantity";
    public static final String COLUMN_DEBT_AMOUNT = "amount";
    public static final String COLUMN_DEBT_PAID = "paid";


    // =========================================================
    // TRANSACTIONS TABLE
    // =========================================================

    public static final String TABLE_TRANSACTIONS = "transactions";

    public static final String COLUMN_TRANSACTION_ID = "transaction_id";
    public static final String COLUMN_TRANSACTION_CUSTOMER_ID = "customer_id";
    public static final String COLUMN_TRANSACTION_CUSTOMER_NAME = "customer_name";
    public static final String COLUMN_TRANSACTION_TYPE = "type";
    public static final String COLUMN_TRANSACTION_AMOUNT = "amount";
    public static final String COLUMN_TRANSACTION_DATE = "transaction_date";
    public static final String COLUMN_TRANSACTION_TIME = "transaction_time";


    // Transaction types
    public static final String TRANSACTION_DEBT = "DEBT";
    public static final String TRANSACTION_PAYMENT = "PAYMENT";


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

        db.execSQL(
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

                        ")"
        );


        // -----------------------------------------------------
        // DEBTS TABLE
        // -----------------------------------------------------

        db.execSQL(
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

                        ")"
        );


        // -----------------------------------------------------
        // TRANSACTIONS TABLE
        // -----------------------------------------------------

        db.execSQL(
                "CREATE TABLE " +
                        TABLE_TRANSACTIONS +
                        " (" +

                        COLUMN_TRANSACTION_ID +
                        " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        COLUMN_TRANSACTION_CUSTOMER_ID +
                        " INTEGER, " +

                        COLUMN_TRANSACTION_CUSTOMER_NAME +
                        " TEXT NOT NULL, " +

                        COLUMN_TRANSACTION_TYPE +
                        " TEXT NOT NULL, " +

                        COLUMN_TRANSACTION_AMOUNT +
                        " REAL DEFAULT 0, " +

                        COLUMN_TRANSACTION_DATE +
                        " TEXT, " +

                        COLUMN_TRANSACTION_TIME +
                        " TEXT" +

                        ")"
        );
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


        // -----------------------------------------------------
        // VERSION 5 -> 6
        // -----------------------------------------------------

        if (oldVersion < 6) {

            db.execSQL(
                    "CREATE TABLE " +
                            TABLE_TRANSACTIONS +
                            " (" +

                            COLUMN_TRANSACTION_ID +
                            " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                            COLUMN_TRANSACTION_CUSTOMER_ID +
                            " INTEGER, " +

                            COLUMN_TRANSACTION_CUSTOMER_NAME +
                            " TEXT NOT NULL, " +

                            COLUMN_TRANSACTION_TYPE +
                            " TEXT NOT NULL, " +

                            COLUMN_TRANSACTION_AMOUNT +
                            " REAL DEFAULT 0, " +

                            COLUMN_TRANSACTION_DATE +
                            " TEXT, " +

                            COLUMN_TRANSACTION_TIME +
                            " TEXT" +

                            ")"
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
                ).format(
                        new Date()
                );


        values.put(
                COLUMN_DATE,
                date
        );


        String time =
                new SimpleDateFormat(
                        "hh:mm a",
                        Locale.getDefault()
                ).format(
                        new Date()
                );


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


        values.put(
                COLUMN_DEBT_QUANTITY,
                quantity
        );


        values.put(
                COLUMN_DEBT_AMOUNT,
                amount
        );


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
    // ADD DEBT TRANSACTION
    // =========================================================
    //
    // Saves an entry in the transaction history whenever
    // a new debt is successfully created.
    //
    // =========================================================

    public long addDebtTransaction(
            int customerId,
            String customerName,
            double amount
    ) {

        SQLiteDatabase db =
                this.getWritableDatabase();


        ContentValues values =
                new ContentValues();


        values.put(
                COLUMN_TRANSACTION_CUSTOMER_ID,
                customerId
        );


        values.put(
                COLUMN_TRANSACTION_CUSTOMER_NAME,
                customerName
        );


        values.put(
                COLUMN_TRANSACTION_TYPE,
                TRANSACTION_DEBT
        );


        values.put(
                COLUMN_TRANSACTION_AMOUNT,
                amount
        );


        String date =
                new SimpleDateFormat(
                        "MMM d, yyyy",
                        Locale.getDefault()
                ).format(
                        new Date()
                );


        String time =
                new SimpleDateFormat(
                        "h:mma",
                        Locale.getDefault()
                ).format(
                        new Date()
                );


        values.put(
                COLUMN_TRANSACTION_DATE,
                date
        );


        values.put(
                COLUMN_TRANSACTION_TIME,
                time
        );


        long result =
                db.insert(
                        TABLE_TRANSACTIONS,
                        null,
                        values
                );


        db.close();

        return result;
    }


    // =========================================================
    // ADD PAYMENT TRANSACTION
    // =========================================================
    //
    // Saves an entry in transaction history whenever a payment
    // is successfully completed.
    //
    // =========================================================

    public long addPaymentTransaction(
            int customerId,
            String customerName,
            double amount
    ) {

        SQLiteDatabase db =
                this.getWritableDatabase();


        ContentValues values =
                new ContentValues();


        values.put(
                COLUMN_TRANSACTION_CUSTOMER_ID,
                customerId
        );


        values.put(
                COLUMN_TRANSACTION_CUSTOMER_NAME,
                customerName
        );


        values.put(
                COLUMN_TRANSACTION_TYPE,
                TRANSACTION_PAYMENT
        );


        values.put(
                COLUMN_TRANSACTION_AMOUNT,
                amount
        );


        String date =
                new SimpleDateFormat(
                        "MMM d, yyyy",
                        Locale.getDefault()
                ).format(
                        new Date()
                );


        String time =
                new SimpleDateFormat(
                        "h:mma",
                        Locale.getDefault()
                ).format(
                        new Date()
                );


        values.put(
                COLUMN_TRANSACTION_DATE,
                date
        );


        values.put(
                COLUMN_TRANSACTION_TIME,
                time
        );


        long result =
                db.insert(
                        TABLE_TRANSACTIONS,
                        null,
                        values
                );


        db.close();

        return result;
    }


    // =========================================================
    // GET ALL TRANSACTIONS
    // =========================================================

    public Cursor getAllTransactions() {

        SQLiteDatabase db =
                this.getReadableDatabase();


        return db.query(
                TABLE_TRANSACTIONS,

                null,

                null,
                null,

                null,
                null,

                COLUMN_TRANSACTION_ID +
                        " DESC"
        );
    }


    // =========================================================
    // GET CUSTOMERS WITH REMAINING DEBT
    // =========================================================

    public Cursor getCustomersWithRemainingDebt() {

        SQLiteDatabase db =
                this.getReadableDatabase();


        String query =

                "SELECT " +

                        "c." +
                        COLUMN_ID +
                        " AS customer_id, " +

                        "c." +
                        COLUMN_NAME +
                        " AS customer_name, " +

                        "c." +
                        COLUMN_PHONE +
                        " AS customer_phone, " +

                        "COALESCE(" +

                        "SUM(" +

                        "CASE " +

                        "WHEN (" +
                        "d." +
                        COLUMN_DEBT_AMOUNT +
                        " - " +
                        "d." +
                        COLUMN_DEBT_PAID +
                        ") > 0 " +

                        "THEN (" +
                        "d." +
                        COLUMN_DEBT_AMOUNT +
                        " - " +
                        "d." +
                        COLUMN_DEBT_PAID +
                        ") " +

                        "ELSE 0 " +

                        "END" +

                        "), 0" +

                        ") AS total_debt " +

                        "FROM " +
                        TABLE_CUSTOMERS +
                        " c " +

                        "INNER JOIN " +
                        TABLE_DEBTS +
                        " d " +

                        "ON c." +
                        COLUMN_ID +
                        " = d." +
                        COLUMN_DEBT_CUSTOMER_ID +

                        " GROUP BY " +

                        "c." +
                        COLUMN_ID + ", " +

                        "c." +
                        COLUMN_NAME + ", " +

                        "c." +
                        COLUMN_PHONE +

                        " HAVING " +

                        "SUM(" +

                        "CASE " +

                        "WHEN (" +
                        "d." +
                        COLUMN_DEBT_AMOUNT +
                        " - " +
                        "d." +
                        COLUMN_DEBT_PAID +
                        ") > 0 " +

                        "THEN (" +
                        "d." +
                        COLUMN_DEBT_AMOUNT +
                        " - " +
                        "d." +
                        COLUMN_DEBT_PAID +
                        ") " +

                        "ELSE 0 " +

                        "END" +

                        ") > 0 " +

                        "ORDER BY c." +
                        COLUMN_ID +
                        " DESC";


        return db.rawQuery(
                query,
                null
        );
    }


    // =========================================================
    // COUNT CUSTOMERS WITH REMAINING DEBT
    // =========================================================

    public int getCustomersWithRemainingDebtCount() {

        SQLiteDatabase db =
                this.getReadableDatabase();


        Cursor cursor =
                db.rawQuery(

                        "SELECT COUNT(*) FROM (" +

                                "SELECT " +
                                COLUMN_DEBT_CUSTOMER_ID +

                                " FROM " +
                                TABLE_DEBTS +

                                " GROUP BY " +
                                COLUMN_DEBT_CUSTOMER_ID +

                                " HAVING SUM(" +

                                "CASE " +

                                "WHEN (" +
                                COLUMN_DEBT_AMOUNT +
                                " - " +
                                COLUMN_DEBT_PAID +
                                ") > 0 " +

                                "THEN (" +
                                COLUMN_DEBT_AMOUNT +
                                " - " +
                                COLUMN_DEBT_PAID +
                                ") " +

                                "ELSE 0 " +

                                "END" +

                                ") > 0" +

                                ")",

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
                        String.valueOf(
                                customerId
                        )
                }
        );
    }


    // =========================================================
    // GET CUSTOMER REMAINING DEBT
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

                                "CASE " +

                                "WHEN (" +
                                COLUMN_DEBT_AMOUNT +
                                " - " +
                                COLUMN_DEBT_PAID +
                                ") > 0 " +

                                "THEN (" +
                                COLUMN_DEBT_AMOUNT +
                                " - " +
                                COLUMN_DEBT_PAID +
                                ") " +

                                "ELSE 0 " +

                                "END" +

                                "), 0) " +

                                "FROM " +
                                TABLE_DEBTS +

                                " WHERE " +
                                COLUMN_DEBT_CUSTOMER_ID +
                                " = ?",

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
    // MAKE PAYMENT
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


        try {

            double currentDebt =
                    getCustomerTotalDebtFromDatabase(
                            db,
                            customerId
                    );


            if (paymentAmount >
                    currentDebt + 0.001) {

                return false;
            }


            db.beginTransaction();


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


            while (
                    cursor.moveToNext()
                            &&
                            remainingPayment > 0.001
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


                double remainingDebt =
                        amount - paid;


                if (remainingDebt <= 0) {

                    continue;
                }


                double paymentForThisDebt =
                        Math.min(
                                remainingPayment,
                                remainingDebt
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


                if (updated != 1) {

                    throw new Exception(
                            "Could not update debt"
                    );
                }


                remainingPayment -=
                        paymentForThisDebt;
            }


            if (remainingPayment > 0.001) {

                throw new Exception(
                        "Payment could not be completed"
                );
            }


            db.setTransactionSuccessful();


            return true;


        } catch (Exception e) {

            return false;


        } finally {

            if (cursor != null) {

                cursor.close();
            }


            if (db.inTransaction()) {

                db.endTransaction();
            }


            db.close();
        }
    }


    // =========================================================
    // INTERNAL TOTAL
    // =========================================================

    private double getCustomerTotalDebtFromDatabase(
            SQLiteDatabase db,
            int customerId
    ) {

        Cursor cursor =
                db.rawQuery(

                        "SELECT COALESCE(" +

                                "SUM(" +

                                "CASE " +

                                "WHEN (" +
                                COLUMN_DEBT_AMOUNT +
                                " - " +
                                COLUMN_DEBT_PAID +
                                ") > 0 " +

                                "THEN (" +
                                COLUMN_DEBT_AMOUNT +
                                " - " +
                                COLUMN_DEBT_PAID +
                                ") " +

                                "ELSE 0 " +

                                "END" +

                                "), 0) " +

                                "FROM " +
                                TABLE_DEBTS +

                                " WHERE " +
                                COLUMN_DEBT_CUSTOMER_ID +
                                " = ?",

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
    // DELETE CUSTOMER
    // =========================================================

    public boolean deleteCustomer(
            int customerId
    ) {

        SQLiteDatabase db =
                this.getWritableDatabase();


        try {

            db.beginTransaction();


            db.delete(
                    TABLE_DEBTS,
                    COLUMN_DEBT_CUSTOMER_ID + "=?",
                    new String[]{
                            String.valueOf(
                                    customerId
                            )
                    }
            );


            db.delete(
                    TABLE_TRANSACTIONS,
                    COLUMN_TRANSACTION_CUSTOMER_ID + "=?",
                    new String[]{
                            String.valueOf(
                                    customerId
                            )
                    }
            );


            int deleted =
                    db.delete(
                            TABLE_CUSTOMERS,
                            COLUMN_ID + "=?",
                            new String[]{
                                    String.valueOf(
                                            customerId
                                    )
                            }
                    );


            if (deleted != 1) {

                throw new Exception(
                        "Customer was not found"
                );
            }


            db.setTransactionSuccessful();


            return true;


        } catch (Exception e) {

            return false;


        } finally {

            if (db.inTransaction()) {

                db.endTransaction();
            }


            db.close();
        }
    }


    // =========================================================
    // TOTAL DEBT
    // =========================================================

    public double getTotalDebt() {

        SQLiteDatabase db =
                this.getReadableDatabase();


        Cursor cursor =
                db.rawQuery(

                        "SELECT COALESCE(" +

                                "SUM(" +

                                "CASE " +

                                "WHEN (" +
                                COLUMN_DEBT_AMOUNT +
                                " - " +
                                COLUMN_DEBT_PAID +
                                ") > 0 " +

                                "THEN (" +
                                COLUMN_DEBT_AMOUNT +
                                " - " +
                                COLUMN_DEBT_PAID +
                                ") " +

                                "ELSE 0 " +

                                "END" +

                                "), 0) " +

                                "FROM " +
                                TABLE_DEBTS,

                        null
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
    // UNPAID CUSTOMER COUNT
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
    // UNPAID AMOUNT
    // =========================================================

    public double getUnpaidAmount() {

        SQLiteDatabase db =
                this.getReadableDatabase();


        Cursor cursor =
                db.rawQuery(

                        "SELECT COALESCE(" +

                                "SUM(" +
                                COLUMN_DEBT_AMOUNT +
                                "), 0) " +

                                "FROM " +
                                TABLE_DEBTS +

                                " WHERE " +
                                COLUMN_DEBT_PAID +
                                " = 0",

                        null
                );


        double amount = 0;


        if (cursor.moveToFirst()) {

            amount =
                    cursor.getDouble(0);
        }


        cursor.close();

        return amount;
    }


    // =========================================================
    // PARTIAL CUSTOMER COUNT
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
    // PARTIAL REMAINING AMOUNT
    // =========================================================

    public double getPartialAmount() {

        SQLiteDatabase db =
                this.getReadableDatabase();


        Cursor cursor =
                db.rawQuery(

                        "SELECT COALESCE(" +

                                "SUM(" +

                                COLUMN_DEBT_AMOUNT +
                                " - " +
                                COLUMN_DEBT_PAID +

                                "), 0) " +

                                "FROM " +
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

            amount =
                    cursor.getDouble(0);
        }


        cursor.close();

        return amount;
    }
}