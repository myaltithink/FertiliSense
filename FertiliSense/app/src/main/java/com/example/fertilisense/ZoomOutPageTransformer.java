package com.example.fertilisense;

import android.view.View;
import androidx.viewpager2.widget.ViewPager2;

public class ZoomOutPageTransformer implements ViewPager2.PageTransformer {
    private static final float MIN_SCALE = 0.90f; // Increased minimum scale for smoother effect
    private static final float MIN_ALPHA = 0.7f;  // Increased minimum alpha for smoother fade

    @Override
    public void transformPage(View page, float position) {
        int pageWidth = page.getWidth();

        if (position < -1) { // This page is way off-screen to the left.
            page.setAlpha(0f);
        } else if (position <= 1) { // [-1,1]
            // Scale the page down smoothly (between MIN_SCALE and 1)
            float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
            float vertMargin = pageWidth * (1 - scaleFactor) / 2;
            float horzMargin = pageWidth * (1 - scaleFactor) / 2;

            if (position < 0) {
                page.setTranslationX(horzMargin - vertMargin / 2);
            } else {
                page.setTranslationX(-horzMargin + vertMargin / 2);
            }

            // Smooth scaling effect
            page.setScaleX(scaleFactor);
            page.setScaleY(scaleFactor);

            // Smooth fade effect (alpha change)
            page.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));
        } else { // This page is way off-screen to the right.
            page.setAlpha(0f);
        }
    }
}
