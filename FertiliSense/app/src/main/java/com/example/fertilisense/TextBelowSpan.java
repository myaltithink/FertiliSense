package com.example.fertilisense;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.LineBackgroundSpan;

public class TextBelowSpan implements LineBackgroundSpan {

    private final String text;
    private final int textColor;

    public TextBelowSpan(String text, int textColor) {
        this.text = text;
        this.textColor = textColor;
    }

    @Override
    public void drawBackground(
            Canvas canvas, Paint paint,
            int left, int right, int top, int baseline,
            int bottom, CharSequence charSequence,
            int start, int end, int lineNum) {

        // Save the original paint style
        int oldColor = paint.getColor();

        // Set custom color
        paint.setColor(textColor);

        // Adjust the text position below the date
        float textX = (left + right) / 2 - paint.measureText(text) / 2;
        float textY = bottom + 20; // Adjust this value as needed for proper positioning

        // Draw the text below the date
        canvas.drawText(text, textX, textY, paint);

        // Restore original paint color
        paint.setColor(oldColor);
    }
}
