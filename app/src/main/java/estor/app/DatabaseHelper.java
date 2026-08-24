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

    // Database name and version
    private static final String DATABASE_NAME = "estor.db";

    // IMPORTANT: We changed 2 to 3 because we added date and time.
    private static final int DATABASE_VERSION = 3;

    // Customers table
    public static final String TABLE_CUSTOMERS = "customers";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_TIME = "time";
    // Debts table
    public static final String TABLE_DEBTS = "debts";
    public static final String COLUMN_DEBT_ID = "debt_id";
    public static final String COLUMN_DEBT_CUSTOMER_ID = "customer_id";
    public static final String COLUMN_DEBT_CUSTOMER_NAME = "customer_name";
    public static final String COLUMN_DEBT_CUSTOMER_PHONE = "customer_phone";
    public static final String COLUMN_DEBT_ITEM = "item";

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Create database
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Customers table
        String createCustomersTable =
                "CREATE TABLE " + TABLE_CUSTOMERS + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_NAME + " TEXT NOT NULL, " +
                        COLUMN_PHONE + " TEXT NOT NULL, " +
                        COLUMN_DATE + " TEXT, " +
                        COLUMN_TIME + " TEXT" +
                        ")";
        db.execSQL(createCustomersTable);

        // Debts table
        String createDebtsTable =
                "CREATE TABLE " + TABLE_DEBTS + " (" +
                        COLUMN_DEBT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_DEBT_CUSTOMER_ID + " INTEGER, " +
                        COLUMN_DEBT_CUSTOMER_NAME + " TEXT, " +
                        COLUMN_DEBT_CUSTOMER_PHONE + " TEXT, " +
                        COLUMN_DEBT_ITEM + " TEXT" +
                        ")";
        db.execSQL(createDebtsTable);
    }

    // Update database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // If old database does not have date and time, add them.
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_CUSTOMERS + " ADD COLUMN " + COLUMN_DATE + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_CUSTOMERS + " ADD COLUMN " + COLUMN_TIME + " TEXT");
        }
    }

    // Add customer
    public long addCustomer(String name, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME, name);
        values.put(COLUMN_PHONE, phone);

        // Date
        String date = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
        values.put(COLUMN_DATE, date);

        // Time
        String time = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
        values.put(COLUMN_TIME, time);

        long result = db.insert(TABLE_CUSTOMERS, null, values);
        db.close();
        return result;
    }

    // Get all customers
    public Cursor getAllCustomers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_CUSTOMERS, null, null, null, null, null, COLUMN_ID + " DESC");
    }

    // Get customer count
    public int getCustomerCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_CUSTOMERS, null);

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();
        db.close();
        return count;
    }

    // Insert debt
    public long insertDebt(int customerId, String name, String phone, String item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_DEBT_CUSTOMER_ID, customerId);
        values.put(COLUMN_DEBT_CUSTOMER_NAME, name);
        values.put(COLUMN_DEBT_CUSTOMER_PHONE, phone);
        values.put(COLUMN_DEBT_ITEM, item);

        long result = db.insert(TABLE_DEBTS, null, values);
        db.close();
        return result;
    }
}
