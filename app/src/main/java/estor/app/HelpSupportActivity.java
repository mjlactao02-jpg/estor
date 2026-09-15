package estor.app;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class HelpSupportActivity extends AppCompatActivity {

    private LinearLayout faqContainer;
    private EditText editSearch;
    private TextView txtNoResults;
    private TextView txtAppInfo;

    // FAQ data
    private String[] faqQuestions;
    private String[] faqAnswers;

    // Keep views for filtering
    private View[] faqViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_support_main);

        faqContainer = findViewById(R.id.faqContainer);
        editSearch = findViewById(R.id.editSearch);
        txtNoResults = findViewById(R.id.txtNoResults);
        txtAppInfo = findViewById(R.id.txtAppInfo);

        loadFaqData();
        setupBackButton();
        setupSearch();
        setupFaqList();
        setupCategories();
        setupContact();
        setupAppInfo();
    }

    private void loadFaqData() {
        faqQuestions = new String[]{
                "How do I add a new debt?",
                "How do I record a payment?",
                "What do Unpaid, Partial and Overdue mean?",
                "How do due dates work?",
                "How do I change the store name?",
                "How do I change my Recovery PIN?",
                "I forgot my PIN, what do I do?",
                "Payment reminders are not sending",
                "Does Estor need internet?",
                "How do I view full history for a customer?",
                "How is Total Debt calculated?",
                "Can I delete a customer?",
                "What happens after I pay part of a debt?",
                "Why is Due Soon empty?"
        };

        faqAnswers = new String[]{
                "Tap Add Debt on the home screen. Select an existing customer or create a new one, then enter item name, quantity, amount and optional due date. Tap Save. The debt is recorded in History and updates Total Debt on the dashboard.",
                "Tap Pay Debt on the home screen, select the customer who has remaining debt, enter the payment amount (must not exceed their total debt) and confirm. The payment is applied to the oldest debt first (FIFO).",
                "Unpaid = paid is 0 for that debt item. Partial = paid is greater than 0 but less than amount (e.g., paid ₱50 of ₱100). Overdue = due date is before today and remaining balance is still greater than 0.",
                "Due date is optional per debt item. It is stored as YYYY-MM-DD. Overdue badge shows when due date is before today. Due Soon on the dashboard shows the 3 customers with the nearest due dates.",
                "Open Settings > Store Name. Type your store name and press Done on the keyboard or tap outside the field. It is saved automatically.",
                "Open Settings > Change Recovery PIN. Enter your previous 4-digit Recovery PIN, then enter the new 4-digit PIN and confirm it. Tap Save.",
                "On the PIN screen tap Forgot PIN and enter your Recovery PIN. If no Recovery PIN was set, you will see \"No recovery PIN has been set.\" Create one in Settings first.",
                "Check: 1) Settings > Enable reminders is ON, 2) SMS permission is granted, 3) Allow Estor to set Alarms in Android Settings > Apps > Estor > Alarms & reminders, 4) Restart the phone to re-schedule via Boot Receiver. Battery saver can also block reminders.",
                "No. Estor works fully offline using local storage. Internet is only needed if you tap Email or Call Support, which opens your email or dialer app.",
                "Tap History on the home screen, then tap a customer. History detail shows every debt item for that customer, with dates, amounts and paid values, plus totals for Total Borrowed and Total Paid.",
                "Total Debt on the dashboard is the sum of (amount - paid) for all debt items where remaining is greater than 0. It updates every time you return to the home screen.",
                "Yes. Open History, open the customer detail, and use the delete option. This removes the customer and all their debts and transactions. This cannot be undone.",
                "The paid amount increases for the oldest unpaid debt item first. If payment exceeds one item, the remainder moves to the next oldest debt until the payment amount is fully applied.",
                "Due Soon shows up to 3 customers who have a due date and still owe money, sorted by nearest due date. If no debts have a due date or all debts are fully paid, it shows \"No dues soon — all clear!\""
        };
    }

    private void setupBackButton() {
        View back = findViewById(R.id.btnBack);
        if (back != null) back.setOnClickListener(v -> finish());
    }

    private void setupSearch() {
        if (editSearch == null) return;
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                filterFaq(s.toString());
            }
        });
    }

    private void setupFaqList() {
        if (faqContainer == null) return;
        faqContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        faqViews = new View[faqQuestions.length];

        for (int i = 0; i < faqQuestions.length; i++) {
            View item = inflater.inflate(R.layout.item_faq, faqContainer, false);
            TextView txtQ = item.findViewById(R.id.txtFaqQuestion);
            TextView txtA = item.findViewById(R.id.txtFaqAnswer);
            ImageView chevron = item.findViewById(R.id.imgFaqChevron);

            txtQ.setText(faqQuestions[i]);
            txtA.setText(faqAnswers[i]);
            txtA.setVisibility(View.GONE);

            item.setOnClickListener(v -> {
                boolean opening = txtA.getVisibility() != View.VISIBLE;
                txtA.setVisibility(opening ? View.VISIBLE : View.GONE);
                if (chevron != null) chevron.setRotation(opening ? 90f : 0f);
            });

            faqContainer.addView(item);
            faqViews[i] = item;
        }
    }

    private void filterFaq(String query) {
        if (faqViews == null) return;
        String q = query == null ? "" : query.trim().toLowerCase();
        int visible = 0;
        for (int i = 0; i < faqViews.length; i++) {
            boolean match = q.isEmpty()
                    || faqQuestions[i].toLowerCase().contains(q)
                    || faqAnswers[i].toLowerCase().contains(q);
            faqViews[i].setVisibility(match ? View.VISIBLE : View.GONE);
            if (match) visible++;
        }
        if (txtNoResults != null) {
            txtNoResults.setVisibility(visible == 0 ? View.VISIBLE : View.GONE);
        }
    }

    private void setupCategories() {
        View cat1 = findViewById(R.id.cardCatGettingStarted);
        View cat2 = findViewById(R.id.cardCatDebts);
        View cat3 = findViewById(R.id.cardCatPayments);
        View cat4 = findViewById(R.id.cardCatReminders);
        View cat5 = findViewById(R.id.cardCatSecurity);

        if (cat1 != null) cat1.setOnClickListener(v -> showCategoryDialog(
                getString(R.string.help_category_getting_started),
                "Estor is an offline debt ledger for sari-sari stores.\n\n"
                        + "• Dashboard shows Total Debt, Unpaid, Partial, Overdue and Due Soon.\n"
                        + "• Add Debt to create customers and debts.\n"
                        + "• Store Name in Settings personalizes reminders."
        ));
        if (cat2 != null) cat2.setOnClickListener(v -> showCategoryDialog(
                getString(R.string.help_category_debts),
                "Manage customers and debts.\n\n"
                        + "• Add Debt: item, quantity, amount, optional due date.\n"
                        + "• Pay Debt list shows only customers who still owe.\n"
                        + "• Debt items are ordered by due date.\n"
                        + "• Deleting a customer removes all their debts."
        ));
        if (cat3 != null) cat3.setOnClickListener(v -> showCategoryDialog(
                getString(R.string.help_category_payments),
                "Payments and history.\n\n"
                        + "• Pay Debt applies to oldest debt first (FIFO).\n"
                        + "• Transaction shows all DEBT and PAYMENT logs.\n"
                        + "• History shows per-customer borrowed vs paid totals.\n"
                        + "• Unpaid = paid 0, Partial = 0 < paid < amount."
        ));
        if (cat4 != null) cat4.setOnClickListener(v -> showCategoryDialog(
                getString(R.string.help_category_reminders),
                "Automatic payment reminders via SMS.\n\n"
                        + "• Enable in Settings: turn on Enable reminders.\n"
                        + "• Choose frequency: Daily to Every weekend.\n"
                        + "• Needs SEND_SMS and Allow Alarms permission.\n"
                        + "• Reminders are re-scheduled after reboot."
        ));
        if (cat5 != null) cat5.setOnClickListener(v -> showCategoryDialog(
                getString(R.string.help_category_security),
                "PIN and Recovery PIN.\n\n"
                        + "• PIN locks the app on launch.\n"
                        + "• Recovery PIN is set in Settings > Change Recovery PIN.\n"
                        + "• To change: enter previous 4-digit, then new and confirm.\n"
                        + "• Forgot PIN: use Forgot PIN on the lock screen."
        ));
    }

    private void showCategoryDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void setupContact() {
        View email = findViewById(R.id.cardEmail);
        View phone = findViewById(R.id.cardPhone);

        if (email != null) email.setOnClickListener(v -> {
            try {
                String addr = getString(R.string.help_contact_email);
                String subject = getString(R.string.help_email_subject);
                String body = "App: estor v" + getVersionName() + "\nDevice: " + android.os.Build.MODEL + "\nAndroid: " + android.os.Build.VERSION.RELEASE + "\n\nDescribe your issue:\n";
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + addr + "?subject=" + Uri.encode(subject) + "&body=" + Uri.encode(body)));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "No email app found.", Toast.LENGTH_SHORT).show();
            }
        });

        if (phone != null) phone.setOnClickListener(v -> {
            try {
                String tel = getString(R.string.help_contact_phone);
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + tel));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "No dialer app found.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupAppInfo() {
        if (txtAppInfo == null) return;
        String ver = getVersionName();
        txtAppInfo.setText("estor v" + ver + "  •  Offline •  © 2026");
    }

    private String getVersionName() {
        try {
            PackageManager pm = getPackageManager();
            PackageInfo pi = pm.getPackageInfo(getPackageName(), 0);
            return pi.versionName != null ? pi.versionName : "1.0";
        } catch (Exception e) {
            return "1.0";
        }
    }
}
