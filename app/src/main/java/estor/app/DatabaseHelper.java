package estor.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "estor.db";
    private static final int DATABASE_VERSION = 2; // Incremented version

    // Customers Table
    public static final String TABLE_CUSTOMERS = "customers";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PHONE = "phone";

    // Debts Table
    public static final String TABLE_DEBTS = "debts";
    public static final String COLUMN_DEBT_ID = "debt_id";
    public static final String COLUMN_DEBT_CUSTOMER_ID = "customer_id";
    public static final String COLUMN_DEBT_CUSTOMER_NAME = "customer_name";
    public static final String COLUMN_DEBT_CUSTOMER_PHONE = "customer_phone";
    public static final String COLUMN_DEBT_ITEM = "item";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createCustomersTable = "CREATE TABLE " + TABLE_CUSTOMERS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT NOT NULL, " +
                COLUMN_PHONE + " TEXT NOT NULL)";
        db.execSQL(createCustomersTable);

        String createDebtsTable = "CREATE TABLE " + TABLE_DEBTS + " (" +
                COLUMN_DEBT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DEBT_CUSTOMER_ID + " INTEGER, " +
                COLUMN_DEBT_CUSTOMER_NAME + " TEXT, " +
                COLUMN_DEBT_CUSTOMER_PHONE + " TEXT, " +
                COLUMN_DEBT_ITEM + " TEXT)";
        db.execSQL(createDebtsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOMERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEBTS);
        onCreate(db);
    }

    public long addCustomer(String name, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_PHONE, phone);
        long result = db.insert(TABLE_CUSTOMERS, null, values);
        db.close();
        return result;
    }

    public Cursor getAllCustomers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_CUSTOMERS, null, null, null, null, null, COLUMN_ID + " DESC");
    }

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
