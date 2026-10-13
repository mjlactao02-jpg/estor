package estor.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class CoachTour {

    private static final String PREFS = "estor_prefs";
    private static final String KEY = "coach_main_done_v1";

    public static boolean isDone(Activity activity) {
        return activity.getSharedPreferences(
                PREFS, Activity.MODE_PRIVATE)
                .getBoolean(KEY, false);
    }

    public static void markDone(Activity activity) {
        activity.getSharedPreferences(
                PREFS, Activity.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY, true)
                .apply();
    }

    // =========================================================
    // STEP
    // =========================================================

    public static class Step {
        final View target;
        final String title;
        final String message;
        final float radiusDp;

        public Step(View target, String title,
                    String message) {
            this(target, title, message, 18f);
        }

        public Step(View target, String title,
                    String message, float radiusDp) {
            this.target = target;
            this.title = title;
            this.message = message;
            this.radiusDp = radiusDp;
        }
    }

    // =========================================================
    // TOUR
    // =========================================================

    private final AppCompatActivity activity;
    private final ScrollView scrollView;
    private final List<Step> steps = new ArrayList<>();

    private FrameLayout overlay;
    private SpotlightView spotlight;
    private View tooltip;
    private TextView txtTitle;
    private TextView txtMessage;
    private TextView txtStep;
    private TextView btnSkip;
    private TextView btnBack;
    private Button btnNext;

    private int index = 0;
    private boolean showing = false;
    private OnBackPressedCallback backCallback;

    public CoachTour(AppCompatActivity activity,
                     ScrollView scrollView) {
        this.activity = activity;
        this.scrollView = scrollView;
    }

    public CoachTour add(Step step) {
        steps.add(step);
        return this;
    }

    public void start() {
        if (steps.isEmpty() || showing) return;
        showing = true;
        index = 0;

        overlay = new FrameLayout(activity);
        overlay.setLayoutParams(
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        overlay.setClickable(true);
        overlay.setFocusable(true);
        overlay.setAlpha(0f);

        spotlight = new SpotlightView(activity);
        spotlight.setLayoutParams(
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        overlay.addView(spotlight);

        tooltip = LayoutInflater.from(activity).inflate(
                R.layout.layout_coach_tooltip,
                overlay, false);
        FrameLayout.LayoutParams tipParams =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = dp(20);
        tipParams.leftMargin = margin;
        tipParams.rightMargin = margin;
        tipParams.gravity = Gravity.BOTTOM;
        tipParams.bottomMargin = dp(48);
        tooltip.setLayoutParams(tipParams);
        overlay.addView(tooltip);

        txtTitle = tooltip.findViewById(R.id.coachTitle);
        txtMessage = tooltip.findViewById(R.id.coachMessage);
        txtStep = tooltip.findViewById(R.id.coachStep);
        btnSkip = tooltip.findViewById(R.id.coachSkip);
        btnBack = tooltip.findViewById(R.id.coachBack);
        btnNext = tooltip.findViewById(R.id.coachNext);

        btnSkip.setOnClickListener(v -> finish(true));
        btnBack.setOnClickListener(v -> {
            if (index > 0) showStep(index - 1);
        });
        btnNext.setOnClickListener(v -> {
            if (index < steps.size() - 1) {
                showStep(index + 1);
            } else {
                finish(true);
            }
        });

        backCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (index > 0) {
                    showStep(index - 1);
                } else {
                    finish(true);
                }
            }
        };
        activity.getOnBackPressedDispatcher()
                .addCallback(activity, backCallback);

        ViewGroup decor =
                (ViewGroup) activity.getWindow()
                        .getDecorView();
        decor.addView(overlay);

        overlay.animate()
                .alpha(1f)
                .setDuration(250)
                .start();
        spotlight.fadeIn();

        showStep(0);
    }

    private void showStep(int i) {
        index = i;
        Step step = steps.get(i);

        txtTitle.setText(step.title);
        txtMessage.setText(step.message);
        txtStep.setText(
                (i + 1) + " of " + steps.size());
        btnBack.setVisibility(
                i == 0 ? View.INVISIBLE : View.VISIBLE);
        btnNext.setText(
                i == steps.size() - 1
                        ? "Finish"
                        : "Next");

        // pop animation on the card
        tooltip.setAlpha(0f);
        tooltip.setTranslationY(dp(18));
        tooltip.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(280)
                .start();

        if (step.target == null) {
            spotlight.setHole(null, 18f);
            centerTooltip();
            return;
        }

        scrollToTarget(step.target, () -> {
            if (!showing) return;
            RectF hole = holeFor(step.target, dp(12));
            if (hole == null) {
                spotlight.setHole(null, 18f);
                centerTooltip();
            } else {
                spotlight.setHole(hole, step.radiusDp);
                placeTooltip(hole);
            }
        });
    }

    // Scroll so the target sits in the visible area,
    // then run the callback.
    private void scrollToTarget(View target,
                               Runnable after) {
        if (scrollView == null) {
            target.post(after);
            return;
        }
        int[] targetLoc = new int[2];
        int[] scrollLoc = new int[2];
        target.getLocationOnScreen(targetLoc);
        scrollView.getLocationOnScreen(scrollLoc);

        int targetCenterY =
                targetLoc[1] + target.getHeight() / 2;
        int scrollCenterY =
                scrollLoc[1] + scrollView.getHeight() / 2;
        int dy = targetCenterY - scrollCenterY;

        if (Math.abs(dy) < 20) {
            target.post(after);
            return;
        }

        int dest = scrollView.getScrollY() + dy;
        if (dest < 0) dest = 0;
        scrollView.smoothScrollTo(0, dest);
        scrollView.postDelayed(after, 420);
    }

    private RectF holeFor(View target, int padding) {
        Rect r = new Rect();
        boolean visible =
                target.getGlobalVisibleRect(r);
        if (!visible
                || r.height() < 8
                || r.width() < 8) {
            return null;
        }
        Rect o = new Rect();
        overlay.getGlobalVisibleRect(o);
        return new RectF(
                r.left - o.left - padding,
                r.top - o.top - padding,
                r.right - o.left + padding,
                r.bottom - o.top + padding);
    }

    private void placeTooltip(RectF hole) {
        FrameLayout.LayoutParams params =
                (FrameLayout.LayoutParams)
                        tooltip.getLayoutParams();

        int overlayH = overlay.getHeight();
        if (overlayH == 0) {
            overlayH = overlay.getResources()
                    .getDisplayMetrics().heightPixels;
        }

        float gap = dp(16);
        float belowTop = hole.bottom + gap;
        float aboveHeight = hole.top - gap;

        // Estimate card height; use measured or fallback
        int cardH = tooltip.getMeasuredHeight();
        if (cardH == 0) cardH = dp(260);

        boolean fitsBelow =
                belowTop + cardH + dp(40) <= overlayH;

        params.gravity = Gravity.TOP;
        params.bottomMargin = 0;
        if (fitsBelow) {
            params.topMargin = (int) belowTop;
        } else if (aboveHeight >= cardH + dp(24)) {
            params.topMargin =
                    (int) (aboveHeight - cardH);
            if (params.topMargin < dp(24)) {
                params.topMargin = dp(24);
            }
        } else {
            // not enough room either side: pin to bottom
            params.gravity = Gravity.BOTTOM;
            params.topMargin = 0;
            params.bottomMargin = dp(48);
        }
        tooltip.setLayoutParams(params);
    }

    private void centerTooltip() {
        FrameLayout.LayoutParams params =
                (FrameLayout.LayoutParams)
                        tooltip.getLayoutParams();
        params.gravity = Gravity.CENTER_VERTICAL;
        params.topMargin = 0;
        params.bottomMargin = 0;
        tooltip.setLayoutParams(params);
    }

    private int dp(int value) {
        return (int) (value * overlay.getResources()
                .getDisplayMetrics().density);
    }

    private void finish(boolean save) {
        if (!showing) return;
        showing = false;
        if (save) markDone(activity);
        if (backCallback != null) {
            backCallback.remove();
        }
        if (overlay == null) return;
        overlay.animate()
                .alpha(0f)
                .setDuration(220)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(
                            Animator animation) {
                        try {
                            ViewGroup parent =
                                    (ViewGroup) overlay
                                            .getParent();
                            if (parent != null) {
                                parent.removeView(
                                        overlay);
                            }
                        } catch (Exception ignored) {
                        }
                        overlay = null;
                    }
                })
                .start();
    }
}
