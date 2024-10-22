package com.fertilisense.fertilisense;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.Collection;

public class EventDecorator implements DayViewDecorator {

    private final int color;
    private final Collection<CalendarDay> dates;
    private final String text; // Add a text field

    int textColor = Color.BLACK; // Change this to your desired color
    float textSize = 16f;


    public EventDecorator(Context context, int colorResId, Collection<CalendarDay> dates, String text) {
        this.color = context.getResources().getColor(colorResId);
        this.dates = dates;
        this.text = text; // Initialize the text
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setBackgroundDrawable(new ColorDrawable(color));

        // Use TextBelowSpan to draw text below the date
        if (text != null && !text.isEmpty()) {
            view.addSpan(new TextBelowSpan(text, textColor, textSize)); // Now passing three arguments
        }
    }
}
