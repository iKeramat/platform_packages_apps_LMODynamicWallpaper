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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

import com.libremobileos.dynamicwallpaper.R;

import java.util.Calendar;

public class GLWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new GLEngine();
    }

    private class GLEngine extends Engine {
        private final Handler handler = new Handler(Looper.getMainLooper());
        private Bitmap currentWallpaper, nextWallpaper;
        private float alpha = 0f;
        private final Paint paint = new Paint();

        private static final int MORNING_START = 6;
        private static final int NOON_START = 11;
        private static final int EVENING_START = 16;
        private static final int NIGHT_START = 20;

        private int lastWidth = -1;
        private int lastHeight = -1;

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            setWallpaperBasedOnTime();
            startWallpaperUpdate();
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);

            if (width != lastWidth || height != lastHeight) {
                lastWidth = width;
                lastHeight = height;
                drawWallpaper();
            }
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            lastWidth = -1;
            lastHeight = -1;
        }

        private void setWallpaperBasedOnTime() {
            Calendar calendar = Calendar.getInstance();
            int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

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
                    alpha += 0.2f;
                    if (alpha >= 1f) {
                        alpha = 1f;
                        currentWallpaper = nextWallpaper;
                    } else {
                        handler.postDelayed(this, 1000 / 60);
                    }
                    drawWallpaper();
                }
            });
        }

        private void drawWallpaper() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = holder.lockCanvas();
            if (canvas != null) {
                int width = canvas.getWidth();
                int height = canvas.getHeight();

                Bitmap scaledCurrent = (currentWallpaper != null) ? scaleCenterCrop(currentWallpaper, width, height) : null;
                Bitmap scaledNext = (nextWallpaper != null) ? scaleCenterCrop(nextWallpaper, width, height) : null;

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
            if (source == null || source.isRecycled()) return null;

            float scale = Math.max(
                    (float) newWidth / source.getWidth(),
                    (float) newHeight / source.getHeight()
            );

            float scaledWidth = scale * source.getWidth();
            float scaledHeight = scale * source.getHeight();

            float dx = (newWidth - scaledWidth) / 2;
            float dy = (newHeight - scaledHeight) / 2;

            Matrix matrix = new Matrix();
            matrix.setScale(scale, scale);
            matrix.postTranslate(dx, dy);

            Bitmap outputBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(outputBitmap);
            Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
            canvas.drawBitmap(source, matrix, paint);

            return outputBitmap;
        }

        private void startWallpaperUpdate() {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setWallpaperBasedOnTime();
                    startWallpaperUpdate();
                }
            }, 60000);
        }
    }
}
