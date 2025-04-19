/*
 * Copyright (C) 2025 The LibreMobileOS Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.libremobileos.dynamicwallpaper;

import android.service.wallpaper.WallpaperService;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.view.SurfaceHolder;

import com.libremobileos.dynamicwallpaper.R;

import java.util.Calendar;

public class GLWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new GLEngine();
    }

    private class GLEngine extends Engine {
        private Handler handler = new Handler(Looper.getMainLooper());
        private Bitmap currentWallpaper, nextWallpaper;
        private float alpha = 0f; // Transition progress (0 = old wallpaper, 1 = new wallpaper)
        private Paint paint = new Paint();

        // Define time periods
        private static final int MORNING_START = 6;
        private static final int NOON_START = 11;
        private static final int EVENING_START = 16;
        private static final int NIGHT_START = 20;

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            setWallpaperBasedOnTime();
            startWallpaperUpdate();
        }

        private void setWallpaperBasedOnTime() {
            // Get the current time of day
            Calendar calendar = Calendar.getInstance();
            int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

            // Determine the next wallpaper
            int nextWallpaperRes;
            if (hourOfDay >= MORNING_START && hourOfDay < NOON_START) {
                nextWallpaperRes = R.drawable.morning;
            } else if (hourOfDay >= NOON_START && hourOfDay < EVENING_START) {
                nextWallpaperRes = R.drawable.noon;
            } else if (hourOfDay >= EVENING_START && hourOfDay < NIGHT_START) {
                nextWallpaperRes = R.drawable.evening;
            } else {
                nextWallpaperRes = R.drawable.night;
            }

            if (nextWallpaper == null || nextWallpaperRes != getCurrentWallpaperResId()) {
                nextWallpaper = BitmapFactory.decodeResource(getResources(), nextWallpaperRes);
                startCrossFadeAnimation();
            }
        }

        private int getCurrentWallpaperResId() {
            if (currentWallpaper == null) return -1;
            if (currentWallpaper.sameAs(BitmapFactory.decodeResource(getResources(), R.drawable.morning)))
                return R.drawable.morning;
            if (currentWallpaper.sameAs(BitmapFactory.decodeResource(getResources(), R.drawable.noon)))
                return R.drawable.noon;
            if (currentWallpaper.sameAs(BitmapFactory.decodeResource(getResources(), R.drawable.evening)))
                return R.drawable.evening;
            return R.drawable.night;
        }

        private void startCrossFadeAnimation() {
            alpha = 0f;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    alpha += 0.2f; // Faster transition
                    if (alpha >= 1f) {
                        alpha = 1f;
                        currentWallpaper = nextWallpaper;
                    } else {
                        handler.postDelayed(this, 1000 / 60); // 60 FPS smooth animation
                    }
                    drawWallpaper();
                }
            });
        }

        private void drawWallpaper() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = holder.lockCanvas();
            if (canvas != null) {
                int screenWidth = canvas.getWidth();
                int screenHeight = canvas.getHeight();

                Bitmap scaledCurrent = (currentWallpaper != null) ? scaleCenterCrop(currentWallpaper, screenWidth, screenHeight) : null;
                Bitmap scaledNext = (nextWallpaper != null) ? scaleCenterCrop(nextWallpaper, screenWidth, screenHeight) : null;

                if (scaledCurrent != null) {
                    canvas.drawBitmap(scaledCurrent, 0, 0, null);
                }
                if (scaledNext != null) {
                    paint.setAlpha((int) (alpha * 255));
                    canvas.drawBitmap(scaledNext, 0, 0, paint);
                }

                holder.unlockCanvasAndPost(canvas);
            }
        }

        private Bitmap scaleCenterCrop(Bitmap source, int newWidth, int newHeight) {
            if (source == null) return null;

            float scale;
            float dx = 0, dy = 0;

            if (source.getWidth() * newHeight > newWidth * source.getHeight()) {
                scale = (float) newHeight / (float) source.getHeight();
                dx = (newWidth - source.getWidth() * scale) * 0.5f;
            } else {
                scale = (float) newWidth / (float) source.getWidth();
                dy = (newHeight - source.getHeight() * scale) * 0.5f;
            }

            Matrix matrix = new Matrix();
            matrix.setScale(scale, scale);
            matrix.postTranslate(dx, dy);

            return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        }

        private void startWallpaperUpdate() {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setWallpaperBasedOnTime();
                    startWallpaperUpdate();
                }
            }, 60000); // Check time every minute
        }
    }
}
