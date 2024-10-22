package com.fertilisense.fertilisense;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.Collection;

/**
 * Decorator that highlights the background of calendar days with a circular drawable.
 */
public class BackgroundColorDecorator implements DayViewDecorator {

    private final Drawable drawable;
    private final Collection<CalendarDay> dates;

    public BackgroundColorDecorator(Context context, int color, Collection<CalendarDay> dates) {
        this.dates = dates;
        // Create a circular drawable with the specified color
        GradientDrawable circleDrawable = new GradientDrawable();
        circleDrawable.setShape(GradientDrawable.OVAL);
        circleDrawable.setColor(color);
        circleDrawable.setSize(dpToPx(context, 40), dpToPx(context, 40)); // Adjust size as needed
        this.drawable = circleDrawable;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setBackgroundDrawable(drawable);
    }

    private int dpToPx(Context context, int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }
}
