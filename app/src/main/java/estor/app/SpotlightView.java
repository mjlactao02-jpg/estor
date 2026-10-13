package estor.app;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class SpotlightView extends View {

    private final Paint dimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint clearPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private RectF hole;
    private RectF animFrom;
    private RectF animTo;
    private float cornerRadius;
    private int dimAlpha = 0;

    private ValueAnimator holeAnimator;
    private ValueAnimator fadeAnimator;

    public SpotlightView(Context context) {
        super(context);
        init();
    }

    public SpotlightView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        setWillNotDraw(false);

        dimPaint.setColor(Color.BLACK);
        dimPaint.setAlpha(0);

        clearPaint.setXfermode(
                new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStrokeWidth(
                3 * getResources().getDisplayMetrics().density);
        borderPaint.setAlpha(0);
    }

    public void fadeIn() {
        if (fadeAnimator != null) fadeAnimator.cancel();
        fadeAnimator = ValueAnimator.ofInt(dimAlpha, 185);
        fadeAnimator.setDuration(280);
        fadeAnimator.setInterpolator(new DecelerateInterpolator());
        fadeAnimator.addUpdateListener(a -> {
            dimAlpha = (int) a.getAnimatedValue();
            dimPaint.setAlpha(dimAlpha);
            borderPaint.setAlpha(
                    (int) (dimAlpha * 0.95f));
            invalidate();
        });
        fadeAnimator.start();
    }

    public void setHole(RectF target, float radiusDp) {
        float density =
                getResources().getDisplayMetrics().density;
        float radius = radiusDp * density;

        if (hole == null || animTo == null) {
            hole = target == null
                    ? null
                    : new RectF(target);
            cornerRadius = radius;
            animTo = hole == null
                    ? null
                    : new RectF(hole);
            invalidate();
            return;
        }

        if (holeAnimator != null) holeAnimator.cancel();
        animFrom = new RectF(hole);
        animTo = target == null ? null : new RectF(target);
        cornerRadius = radius;

        final RectF start = new RectF(animFrom);
        final RectF end = animTo == null
                ? null
                : new RectF(animTo);

        holeAnimator = ValueAnimator.ofFloat(0f, 1f);
        holeAnimator.setDuration(320);
        holeAnimator.setInterpolator(new DecelerateInterpolator());
        holeAnimator.addUpdateListener(a -> {
            float t = (float) a.getAnimatedValue();
            if (end == null) {
                // shrinking away: keep start, fade handled by dim
                hole = new RectF(start);
            } else if (start.left == 0 && start.top == 0
                    && start.right == 0 && start.bottom == 0) {
                hole = new RectF(end);
            } else {
                hole = new RectF(
                        lerp(start.left, end.left, t),
                        lerp(start.top, end.top, t),
                        lerp(start.right, end.right, t),
                        lerp(start.bottom, end.bottom, t));
            }
            invalidate();
        });
        holeAnimator.start();
    }

    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int saved = canvas.saveLayer(
                0, 0, getWidth(), getHeight(), null);

        // dim background
        canvas.drawRect(
                0, 0, getWidth(), getHeight(), dimPaint);

        // punch hole
        if (hole != null
                && hole.width() > 2
                && hole.height() > 2) {
            canvas.drawRoundRect(
                    hole, cornerRadius, cornerRadius,
                    clearPaint);
        }

        canvas.restoreToCount(saved);

        // white ring around the hole
        if (hole != null
                && hole.width() > 2
                && hole.height() > 2
                && borderPaint.getAlpha() > 0) {
            canvas.drawRoundRect(
                    hole, cornerRadius, cornerRadius,
                    borderPaint);
        }
    }
}
