package info.javaway.sudoku.ui.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

/**
 * Конфетти после победы. Живёт три секунды, ничего не перехватывает и само себя выключает:
 * пока бумажек нет, View прозрачен и не рисует ни пикселя.
 */
public class ConfettiView extends View {

    private static final int COUNT = 120;
    private static final int LIFE = 3200;
    private static final float GRAVITY = 0.00025f;

    /** Цвета взяты из палитры приложения, а не случайные: праздник не повод для радуги. */
    private static final int[] COLORS = {0xFFFFD33E, 0xFF1A5FBF, 0xFF7FB2FF, 0xFFB22525};

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();

    private float[] x;
    private float[] y;
    private float[] speedX;
    private float[] speedY;
    private float[] spin;
    private float[] side;
    private int[] color;

    private long start;
    private boolean running;

    public ConfettiView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
    }

    public void launch() {
        if (getWidth() == 0) {
            // Размеры ещё не известны: запускаем, как только View разложат.
            post(this::launch);
            return;
        }
        x = new float[COUNT];
        y = new float[COUNT];
        speedX = new float[COUNT];
        speedY = new float[COUNT];
        spin = new float[COUNT];
        side = new float[COUNT];
        color = new int[COUNT];

        float unit = getWidth() / 400f;
        for (int i = 0; i < COUNT; i++) {
            x[i] = random.nextFloat() * getWidth();
            y[i] = -random.nextFloat() * getHeight() * 0.5f;
            speedX[i] = (random.nextFloat() - 0.5f) * 0.25f * unit;
            speedY[i] = (0.25f + random.nextFloat() * 0.35f) * unit;
            spin[i] = (random.nextFloat() - 0.5f) * 0.01f;
            side[i] = (4f + random.nextFloat() * 5f) * unit;
            color[i] = COLORS[random.nextInt(COLORS.length)];
        }
        start = SystemClock.uptimeMillis();
        running = true;
        invalidate();
    }

    public void stop() {
        running = false;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!running) return;
        long passed = SystemClock.uptimeMillis() - start;
        if (passed > LIFE) {
            running = false;
            return;
        }

        for (int i = 0; i < COUNT; i++) {
            float at = x[i] + speedX[i] * passed;
            float down = y[i] + speedY[i] * passed + GRAVITY * passed * passed;
            if (down - side[i] > getHeight()) continue;

            paint.setColor(color[i]);
            canvas.save();
            canvas.rotate(spin[i] * passed * 180 / (float) Math.PI, at, down);
            canvas.drawRect(at - side[i] / 2, down - side[i] / 2,
                    at + side[i] / 2, down + side[i] / 2, paint);
            canvas.restore();
        }
        postInvalidateOnAnimation();
    }
}
