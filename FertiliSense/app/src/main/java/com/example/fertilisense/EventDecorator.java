package com.example.fertilisense;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.Collection;
import java.util.HashSet;

public class EventDecorator implements DayViewDecorator {

    private final int backgroundColor; // Background color for the date
    private final String labelText; // Text to display below the date
    private final int labelColor; // Color for the label text
    private final HashSet<CalendarDay> dates;
    private final Context context;

    public EventDecorator(Context context, int backgroundColor, String labelText, int labelColor, Collection<CalendarDay> dates) {
        this.context = context;
        this.backgroundColor = backgroundColor;
        this.labelText = labelText;
        this.labelColor = labelColor;
        this.dates = new HashSet<>(dates);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        // Create a circular background drawable
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(backgroundColor);
        drawable.setSize(50, 50); // Adjust size if necessary

        // Set the circular background drawable
        view.setBackgroundDrawable(drawable);

        // Add label/text below the date
        if (labelText != null) {
            view.addSpan(new TextBelowSpan(labelText, labelColor));
        }
    }
}
