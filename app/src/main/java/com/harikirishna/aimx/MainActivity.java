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
        boolean ready = false, dragging = false;

        Trainer() {
            super(MainActivity.this);
        }

        void reset() {
            ax = sx;
            ay = sy;
            invalidate();
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

            float left = getWidth() / 2f - 75;
            float top = getHeight() - 85;

            switch (e.getAction()) {

                case MotionEvent.ACTION_DOWN:

                    if (x >= left && x <= left + 150
                            && y >= top && y <= top + 48) {
                        reset();
                        return true;
                    }

                    float distance = (float) Math.hypot(
                            x - ax, y - ay);

                    if (distance < 55) {
                        dragging = true;
                    } else {
                        tx = x;
                        ty = y;
                        reset();
                    }

                    invalidate();
                    return true;

                case MotionEvent.ACTION_MOVE:

                    if (dragging) {
                        float dx = tx - sx;
                        float dy = ty - sy;

                        float lengthSquared = dx * dx + dy * dy;

                        if (lengthSquared > 0) {
                            float projection =
                                ((x - sx) * dx + (y - sy) * dy)
                                / lengthSquared;

                            projection = Math.max(0,
                                    Math.min(1, projection));

                            ax = sx + projection * dx;
                            ay = sy + projection * dy;
                        }

                        invalidate();
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    dragging = false;
                    return true;
            }

            return true;
        }
    }
}
