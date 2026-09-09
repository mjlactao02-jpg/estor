package estor.app;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DueDateUtils {

    private static final String ISO_FORMAT = "yyyy-MM-dd";
    private static final String DISPLAY_FORMAT = "MMM dd, yyyy";
    private static final String INPUT_MMDDYYYY = "MM/dd/yyyy";

    // =========================================================
    // PARSE user input -> ISO yyyy-MM-dd | null if empty
    // accepts mm/dd/yyyy and yyyy-MM-dd
    // =========================================================
    public static String parseToIso(String input) {
        if (input == null) return null;
        String s = input.trim();
        if (s.isEmpty()) return null;

        // try mm/dd/yyyy first (hint)
        String iso = tryParse(s, INPUT_MMDDYYYY, ISO_FORMAT);
        if (iso != null) return iso;

        // try yyyy-MM-dd
        iso = tryParse(s, ISO_FORMAT, ISO_FORMAT);
        if (iso != null) return iso;

        // try MMM d, yyyy (e.g. May 22, 2026)
        iso = tryParse(s, DISPLAY_FORMAT, ISO_FORMAT);
        if (iso != null) return iso;

        // try MMM dd, yyyy
        iso = tryParse(s, "MMM dd, yyyy", ISO_FORMAT);
        if (iso != null) return iso;

        return null; // invalid
    }

    private static String tryParse(String input, String fromPattern, String toPattern) {
        try {
            SimpleDateFormat from = new SimpleDateFormat(fromPattern, Locale.US);
            from.setLenient(false);
            Date d = from.parse(input);
            SimpleDateFormat to = new SimpleDateFormat(toPattern, Locale.US);
            return to.format(d);
        } catch (ParseException e) {
            return null;
        }
    }

    // =========================================================
    // FORMAT ISO -> display MMM dd, yyyy
    // =========================================================
    public static String formatForDisplay(String iso) {
        if (iso == null || iso.trim().isEmpty()) return "";
        String out = tryParse(iso.trim(), ISO_FORMAT, DISPLAY_FORMAT);
        return out != null ? out : iso;
    }

    // =========================================================
    // For DatePicker: ISO -> mm/dd/yyyy (EditText hint)
    // =========================================================
    public static String isoToInput(String iso) {
        if (iso == null || iso.trim().isEmpty()) return "";
        String out = tryParse(iso.trim(), ISO_FORMAT, INPUT_MMDDYYYY);
        return out != null ? out : iso;
    }

    // =========================================================
    // TODAY ISO
    // =========================================================
    public static String todayIso() {
        SimpleDateFormat sdf = new SimpleDateFormat(ISO_FORMAT, Locale.US);
        return sdf.format(new Date());
    }

    // =========================================================
    // IS OVERDUE? due < today and not paid
    // =========================================================
    public static boolean isOverdue(String iso) {
        if (iso == null || iso.trim().isEmpty()) return false;
        String today = todayIso();
        // string compare works for yyyy-MM-dd
        return iso.trim().compareTo(today) < 0;
    }

    public static boolean isDueToday(String iso) {
        if (iso == null || iso.trim().isEmpty()) return false;
        return iso.trim().equals(todayIso());
    }

    public static boolean isDueSoon(String iso, int days) {
        if (iso == null || iso.trim().isEmpty()) return false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(ISO_FORMAT, Locale.US);
            sdf.setLenient(false);
            Date due = sdf.parse(iso.trim());
            Date today = sdf.parse(todayIso());
            long diff = due.getTime() - today.getTime();
            long diffDays = TimeUnit.MILLISECONDS.toDays(diff);
            return diffDays >= 0 && diffDays <= days;
        } catch (ParseException e) {
            return false;
        }
    }

    public static long daysOverdue(String iso) {
        if (iso == null || iso.trim().isEmpty()) return 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(ISO_FORMAT, Locale.US);
            sdf.setLenient(false);
            Date due = sdf.parse(iso.trim());
            Date today = sdf.parse(todayIso());
            long diff = today.getTime() - due.getTime();
            if (diff <= 0) return 0;
            return TimeUnit.MILLISECONDS.toDays(diff);
        } catch (ParseException e) {
            return 0;
        }
    }

    // =========================================================
    // Validate raw input returns null if ok else error msg
    // =========================================================
    public static String validate(String input) {
        if (input == null || input.trim().isEmpty()) return null; // optional
        String iso = parseToIso(input);
        if (iso == null) return "Use mm/dd/yyyy";
        return null;
    }
}
