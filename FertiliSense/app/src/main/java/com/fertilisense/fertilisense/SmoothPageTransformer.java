package com.fertilisense.fertilisense;

import android.view.View;
import androidx.viewpager2.widget.ViewPager2;

public class SmoothPageTransformer implements ViewPager2.PageTransformer {

    @Override
    public void transformPage(View page, float position) {
        // No zoom or scale, just default smooth sliding
        page.setTranslationX(-position * page.getWidth());
        page.setAlpha(1 - Math.abs(position));
    }
}
