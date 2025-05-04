package com.dam.kairos.utils;

import android.graphics.drawable.Drawable;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

public class EmotionDecorator implements DayViewDecorator {

    private final CalendarDay date;
    private final Drawable drawable;

    public EmotionDecorator(CalendarDay date, Drawable drawable) {
        this.date = date;
        this.drawable = drawable;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return day.getYear() == date.getYear() &&
                day.getMonth() == date.getMonth() &&
                day.getDay() == date.getDay();
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setBackgroundDrawable(drawable);
    }
}
