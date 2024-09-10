package com.example.fertilisense;

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

    public EventDecorator(Context context, int colorResId, Collection<CalendarDay> dates) {
        this.color = context.getResources().getColor(colorResId);
        this.dates = dates;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setBackgroundDrawable(new ColorDrawable(color));
    }
}
