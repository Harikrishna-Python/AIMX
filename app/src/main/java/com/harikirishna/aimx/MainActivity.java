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
        float speedMultiplier = 1.0f;
        float maxDistance = 1.0f;
        float lastPointerProjection = 0f;

        int speedMode = 1;
        int distanceMode = 3;

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

        void updatePosition() {
            float dx = tx - sx;
            float dy = ty - sy;

            ax = sx + progress * dx;
            ay = sy + progress * dy;
        }

        void updateSensitivity(float x) {
            float minX = 30;
            float maxX = getWidth() - 30;

            float value = (x - minX) / (maxX - minX);
            value = Math.max(0f, Math.min(1f, value));

            sensitivity = 0.25f + value * 1.75f;
            invalidate();
        }

        void setSpeed(int mode) {
            speedMode = mode;

            if (mode == 0) {
                speedMultiplier = 0.5f;
            } else if (mode == 1) {
                speedMultiplier = 1.0f;
            } else {
                speedMultiplier = 1.5f;
            }

            invalidate();
        }

        void setMaxDistance(int mode) {
            distanceMode = mode;
            maxDistance = (mode + 1) * 0.25f;

            progress = Math.min(progress, maxDistance);
            updatePosition();
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

            // Drag speed selector
            text(c, "DRAG SPEED", 30, 315, 15, cyan);

            String[] speedNames = {
                    "SLOW 0.5x",
                    "NORMAL 1x",
                    "FAST 1.5x"
            };

            float speedTop = 330;
            float speedBottom = 380;
            float speedWidth = (getWidth() - 80) / 3f;

            for (int i = 0; i < 3; i++) {
                float left = 30 + i * (speedWidth + 10);
                float right = left + speedWidth;

                p.setStyle(Paint.Style.FILL);
                p.setColor(speedMode == i
                        ? 0xFF183F50 : 0xFF202A3A);

                c.drawRoundRect(left, speedTop,
                        right, speedBottom, 10, 10, p);

                p.setTextSize(12);
                p.setTypeface(
                        android.graphics.Typeface.DEFAULT_BOLD);

                float textWidth = p.measureText(speedNames[i]);

                text(c, speedNames[i],
                        (left + right - textWidth) / 2f,
                        speedTop + 30, 12,
                        speedMode == i ? cyan : white);
            }

            // Maximum distance selector
            text(c, "MAXIMUM DRAG DISTANCE: "
                    + (int)(maxDistance * 100) + "%",
                    30, 415, 15, cyan);

            String[] distanceNames = {
                    "25%", "50%", "75%", "100%"
            };

            float distanceTop = 430;
            float distanceBottom = 480;
            float distanceWidth = (getWidth() - 84) / 4f;

            for (int i = 0; i < 4; i++) {
                float left = 30 + i * (distanceWidth + 8);
                float right = left + distanceWidth;

                p.setStyle(Paint.Style.FILL);
                p.setColor(distanceMode == i
                        ? 0xFF183F50 : 0xFF202A3A);

                c.drawRoundRect(left, distanceTop,
                        right, distanceBottom, 10, 10, p);

                p.setTextSize(14);
                p.setTypeface(
                        android.graphics.Typeface.DEFAULT_BOLD);

                float textWidth = p.measureText(distanceNames[i]);

                text(c, distanceNames[i],
                        (left + right - textWidth) / 2f,
                        distanceTop + 30, 14,
                        distanceMode == i ? cyan : white);
            }

            // Selected movement path
            p.setColor(0x8846FF96);
            p.setStrokeWidth(3);
            p.setStyle(Paint.Style.STROKE);
            c.drawLine(sx, sy, tx, ty, p);

            // Maximum distance boundary
            float dx = tx - sx;
            float dy = ty - sy;

            float limitX = sx + maxDistance * dx;
            float limitY = sy + maxDistance * dy;

            p.setColor(0xFFFFB347);
            p.setStrokeWidth(2);
            p.setStyle(Paint.Style.STROKE);
            c.drawCircle(limitX, limitY, 13, p);

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
            float resetLeft = getWidth() / 2f - 75;
            float resetTop = getHeight() - 85;

            p.setStyle(Paint.Style.FILL);
            p.setColor(0xFF183448);
            c.drawRoundRect(resetLeft, resetTop,
                    resetLeft + 150, resetTop + 48,
                    12, 12, p);

            text(c, "RESET",
                    resetLeft + 48, resetTop + 31, 16, white);

            text(c, "MOVEMENT CANNOT PASS LIMIT",
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

                    // Drag speed buttons
                    if (y >= 330 && y <= 380) {
                        float buttonWidth =
                                (getWidth() - 80) / 3f;

                        int selected = (int)
                                ((x - 30) / (buttonWidth + 10));

                        if (selected >= 0 && selected <= 2) {
                            float left = 30
                                    + selected * (buttonWidth + 10);

                            if (x <= left + buttonWidth) {
                                setSpeed(selected);
                                return true;
                            }
                        }
                    }

                    // Maximum distance buttons
                    if (y >= 430 && y <= 480) {
                        float buttonWidth =
                                (getWidth() - 84) / 4f;

                        int selected = (int)
                                ((x - 30) / (buttonWidth + 8));

                        if (selected >= 0 && selected <= 3) {
                            float left = 30
                                    + selected * (buttonWidth + 8);

                            if (x <= left + buttonWidth) {
                                setMaxDistance(selected);
                                return true;
                            }
                        }
                    }

                    // Crosshair dragging
                    float distance = (float) Math.hypot(
                            x - ax, y - ay);

                    if (distance < 55) {
                        dragging = true;
                        lastPointerProjection =
                                getProjection(x, y);
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

                        progress += movement
                                * sensitivity * speedMultiplier;

                        progress = Math.max(0f,
                                Math.min(maxDistance, progress));

                        lastPointerProjection = pointerProjection;

                        updatePosition();
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
