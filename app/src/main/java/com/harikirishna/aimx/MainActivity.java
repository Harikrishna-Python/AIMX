package com.harikirishna.aimx;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.MotionEvent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(0xFF0C0E19);
        getWindow().setNavigationBarColor(0xFF0C0E19);
        setContentView(new Trainer());
    }

    class Trainer extends View {
        Paint p = new Paint(3);

        float sx, sy, tx, ty, ax, ay;
        float progress = 0f;
        float sensitivity = 1.0f;
        float lastPointerProjection = 0f;

        boolean ready = false;
        boolean dragging = false;
        boolean adjustingSensitivity = false;

        Trainer() {
            super(MainActivity.this);
        }

        void reset() {
            progress = 0f;
            ax = sx;
            ay = sy;
            invalidate();
        }

        void updateSensitivity(float x) {
            float minX = 30;
            float maxX = getWidth() - 30;

            float value = (x - minX) / (maxX - minX);
            value = Math.max(0f, Math.min(1f, value));

            sensitivity = 0.25f + value * 1.75f;
            invalidate();
        }

        float getProjection(float x, float y) {
            float dx = tx - sx;
            float dy = ty - sy;
            float lengthSquared = dx * dx + dy * dy;

            if (lengthSquared <= 0) return 0f;

            return ((x - sx) * dx + (y - sy) * dy)
                    / lengthSquared;
        }

        void text(Canvas c, String s, float x, float y,
                  int size, int color) {
            p.setColor(color);
            p.setTextSize(size);
            p.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            p.setStyle(Paint.Style.FILL);
            c.drawText(s, x, y, p);
        }

        @Override
        protected void onDraw(Canvas c) {
            super.onDraw(c);
            c.drawColor(0xFF0C0E19);

            if (!ready && getWidth() > 0) {
                sx = getWidth() / 2f;
                sy = getHeight() * 0.78f;
                tx = getWidth() * 0.7f;
                ty = getHeight() * 0.3f;
                reset();
                ready = true;
            }

            int cyan = 0xFF00DCFF;
            int green = 0xFF46FF96;
            int white = Color.WHITE;

            text(c, "AIMX", 30, 60, 34, cyan);
            text(c, "DRAG BOUNDARY TRAINER",
                    30, 90, 13, white);

            text(c, "TAP ANYWHERE TO SELECT TARGET",
                    30, 135, 13, white);
            text(c, "DRAG THE CROSSHAIR TOWARDS TARGET",
                    30, 160, 13, white);

            // Sensitivity control
            text(c, "SENSITIVITY: "
                    + String.format("%.2f", sensitivity) + "x",
                    30, 205, 15, cyan);

            float sliderY = 235;
            float sliderLeft = 30;
            float sliderRight = getWidth() - 30;
            float sliderPosition = sliderLeft
                    + ((sensitivity - 0.25f) / 1.75f)
                    * (sliderRight - sliderLeft);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(6);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setColor(0xFF354354);
            c.drawLine(sliderLeft, sliderY,
                    sliderRight, sliderY, p);

            p.setColor(cyan);
            c.drawLine(sliderLeft, sliderY,
                    sliderPosition, sliderY, p);

            p.setStyle(Paint.Style.FILL);
            c.drawCircle(sliderPosition, sliderY, 12, p);

            text(c, "0.25x", sliderLeft, 270, 11, white);
            text(c, "2.00x", sliderRight - 35, 270, 11, white);

            // Selected movement path
            p.setColor(0x8846FF96);
            p.setStrokeWidth(3);
            p.setStyle(Paint.Style.STROKE);
            c.drawLine(sx, sy, tx, ty, p);

            // Target
            p.setColor(green);
            p.setStrokeWidth(3);
            c.drawCircle(tx, ty, 20, p);
            c.drawLine(tx - 30, ty, tx + 30, ty, p);
            c.drawLine(tx, ty - 30, tx, ty + 30, p);

            text(c, "TARGET", tx + 25, ty - 20, 11, green);

            // Starting point
            p.setColor(white);
            c.drawCircle(sx, sy, 16, p);
            text(c, "START", sx + 22, sy + 5, 12, white);

            // Current crosshair
            p.setColor(cyan);
            p.setStrokeWidth(4);
            c.drawLine(ax - 17, ay, ax + 17, ay, p);
            c.drawLine(ax, ay - 17, ax, ay + 17, p);
            c.drawCircle(ax, ay, 9, p);

            // Reset button
            float left = getWidth() / 2f - 75;
            float top = getHeight() - 85;

            p.setStyle(Paint.Style.FILL);
            p.setColor(0xFF183448);
            c.drawRoundRect(left, top, left + 150,
                    top + 48, 12, 12, p);

            text(c, "RESET", left + 48, top + 31, 16, white);

            text(c, "MOVEMENT CANNOT PASS TARGET",
                    30, getHeight() - 110, 12, cyan);
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            float x = e.getX();
            float y = e.getY();

            float resetLeft = getWidth() / 2f - 75;
            float resetTop = getHeight() - 85;

            switch (e.getAction()) {

                case MotionEvent.ACTION_DOWN:

                    // Reset button
                    if (x >= resetLeft && x <= resetLeft + 150
                            && y >= resetTop && y <= resetTop + 48) {
                        reset();
                        return true;
                    }

                    // Sensitivity slider
                    if (y >= 215 && y <= 260) {
                        adjustingSensitivity = true;
                        updateSensitivity(x);
                        return true;
                    }

                    // Crosshair dragging
                    float distance = (float) Math.hypot(
                            x - ax, y - ay);

                    if (distance < 55) {
                        dragging = true;
                        lastPointerProjection = getProjection(x, y);
                    } else {
                        // Select a new target
                        tx = x;
                        ty = y;
                        reset();
                    }

                    invalidate();
                    return true;

                case MotionEvent.ACTION_MOVE:

                    if (adjustingSensitivity) {
                        updateSensitivity(x);
                    }

                    if (dragging) {
                        float pointerProjection =
                                getProjection(x, y);

                        float movement = pointerProjection
                                - lastPointerProjection;

                        progress += movement * sensitivity;
                        progress = Math.max(0f,
                                Math.min(1f, progress));

                        lastPointerProjection = pointerProjection;

                        float dx = tx - sx;
                        float dy = ty - sy;

                        ax = sx + progress * dx;
                        ay = sy + progress * dy;

                        invalidate();
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    dragging = false;
                    adjustingSensitivity = false;
                    return true;
            }

            return true;
        }
    }
}
