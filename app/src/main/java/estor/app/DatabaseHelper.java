// Defines which package this Java file belongs to.
package estor.app;
// Imports ContentValues, which is used to store data before inserting it into SQLite.
import android.content.ContentValues;

// Imports Context, which allows the database helper to access the application environment.
import android.content.Context;

// Imports Cursor, which is used to read records returned from the SQLite database.
import android.database.Cursor;

// Imports SQLiteDatabase, which provides methods for creating and working with the database.
import android.database.sqlite.SQLiteDatabase;

// Imports SQLiteOpenHelper, which helps us create, open, and upgrade SQLite databases.
import android.database.sqlite.SQLiteOpenHelper;
// DatabaseHelper manages our SQLite database.
public class DatabaseHelper extends SQLiteOpenHelper {
    // The name of our SQLite database file.
    private static final String DATABASE_NAME = "estor.db";
    // The current version of our database.
    // Increase this number when you change the database structure.
    private static final int DATABASE_VERSION = 1;
    // The name of the table that will store customers.
    public static final String TABLE_CUSTOMERS = "customers";
    // The name of the ID column.
    public static final String COLUMN_ID = "id";
    // The name of the customer name column.
    public static final String COLUMN_NAME = "name";
    // The name of the customer phone column.
    public static final String COLUMN_PHONE = "phone";
    // The Amount of dept
    public static final String COLUMN_TOTAL = "amount";

    public static final String COLUMN_DATE = "date";

    public static final String COLUMN_TIME = "time";

    // Constructor of the DatabaseHelper class.
    // The Context is needed to create/open the database.
    public DatabaseHelper(Context context) {
        // Calls the parent SQLiteOpenHelper constructor.
        // context = application context
        // DATABASE_NAME = database file name
        // null = no custom cursor factory
        // DATABASE_VERSION = database version
        super(
                context,
                DATABASE_NAME,
                null,
                DATABASE_VERSION
        );
    }
    // This method runs automatically when the database is created for the first time.
    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL command used to create the customers table.
        String createTable = "CREATE TABLE " + TABLE_CUSTOMERS + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                                                                        + COLUMN_NAME + " TEXT NOT NULL, "
                                                                        + COLUMN_PHONE + " TEXT NOT NULL,"
                                                                        + COLUMN_TOTAL + " INTEGER NOT NULL,"
                                                                        + COLUMN_DATE + " DATE NOT NULL,"
                                                                        + COLUMN_TIME + " TIME NOT NULL" + ")";
        // Executes the SQL command and creates the table.
        db.execSQL(createTable);
    }
    // This method runs when the database version is changed.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Deletes the old customers table.
        // IF EXISTS prevents an error if the table does not exist.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOMERS);
        // Creates the table again using the newest structure.
        onCreate(db);
    }
    // ADD CUSTOMER
    // This method saves a new customer into SQLite.
    // It returns the ID of the newly inserted customer.
    public long addCustomer(String name, String phone) {
        // Opens the database in writable mode.
        // Writable means we can INSERT, UPDATE, or DELETE data.
        SQLiteDatabase db = this.getWritableDatabase();
        // ContentValues stores the information
        // that we want to put into the database.
        ContentValues values = new ContentValues();
        // Puts the customer's name into ContentValues.
        // "name" is the database column.
        // name is the Java variable containing the customer's name.
        values.put(COLUMN_NAME, name);
        // Puts the customer's phone number into ContentValues.
        values.put(COLUMN_PHONE, phone);
        // Inserts the customer into the customers table.
        // TABLE_CUSTOMERS = table where data is stored
        // null = no null column
        // values = data that will be inserted
        // The returned value is the new customer's ID.
        long result = db.insert(TABLE_CUSTOMERS, null, values);
        // Closes the database connection.
        db.close();
        // Returns the result.
        // If the result is -1, the insert failed.
        // If successful, it returns the customer's ID.
        return result;
    }
    // GET ALL CUSTOMERS
    // This method retrieves all customers from SQLite.
    public Cursor getAllCustomers() {
        // Opens the database in read-only mode.
        SQLiteDatabase db = this.getReadableDatabase();
        // Queries the customers table.
        return db.query(
                // Table to search.
                TABLE_CUSTOMERS,
                // null means select all columns.
                null,
                // null means there is no WHERE condition.
                null,
                // null means there are no WHERE values.
                null,
                // null means no GROUP BY.
                null,
                // null means no HAVING.
                null,
                // Sort the customers by ID.
                // DESC means newest customers appear first.
                COLUMN_ID + " DESC"
        );
    }

    // GET CUSTOMER COUNT

    // This method returns the total number of customers.
    public int getCustomerCount() {
        // Opens the database for reading.
        SQLiteDatabase db =
                this.getReadableDatabase();
        // SQL command that counts all records.
        Cursor cursor =
                db.rawQuery(
                        "SELECT COUNT(*) FROM " +
                                TABLE_CUSTOMERS,
                        null
                );
        // Variable that will store the customer count.
        int count = 0;
        // Moves the Cursor to the first result.
        if (cursor.moveToFirst()) {
            // Gets the number from column 0.
            count = cursor.getInt(0);
        }
        // Closes the Cursor after reading the result.
        cursor.close();
        // Closes the database.
        db.close();
        // Returns the total number of customers.
        return count;
    }
}