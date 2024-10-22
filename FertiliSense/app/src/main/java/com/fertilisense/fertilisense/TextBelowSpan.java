package com.fertilisense.fertilisense;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.LineBackgroundSpan;

public class TextBelowSpan implements LineBackgroundSpan {

    private final String text;
    private final int textColor;
    private final float textSize; // Text size

    public TextBelowSpan(String text, int textColor, float textSize) {
        this.text = text;
        this.textColor = textColor;
        this.textSize = textSize; // Initialize text size
    }

    @Override
    public void drawBackground(
            Canvas canvas, Paint paint,
            int left, int right, int top, int baseline,
            int bottom, CharSequence charSequence,
            int start, int end, int lineNum) {

        // Save original paint color and text size
        int oldColor = paint.getColor();
        float oldTextSize = paint.getTextSize();

        // Set custom color and text size
        paint.setColor(textColor);
        paint.setTextSize(textSize); // Set the desired text size

        // Adjust the text position below the date
        float textX = (left + right) / 2 - paint.measureText(text) / 2;
        float textY = bottom + 20; // Adjust this value for vertical positioning

        // Draw the text below the date
        canvas.drawText(text, textX, textY, paint);

        // Restore original paint style
        paint.setColor(oldColor);
        paint.setTextSize(oldTextSize); // Restore the original text size
    }
}
