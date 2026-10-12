package org.example;

import java.awt.Color;

/** Классический вид: синяя полоска уровня + красная полоска пика. */
public class ClassicIndicatorFactory extends IndicatorFactory {
    private final Color levelColor, peakColor;
    private final int peakThickness;

    public ClassicIndicatorFactory() { this(Color.BLUE, Color.RED, 4); }

    public ClassicIndicatorFactory(Color levelColor, Color peakColor, int peakThickness) {
        this.levelColor = levelColor;
        this.peakColor = peakColor;
        this.peakThickness = peakThickness;
    }

    @Override public LevelPainter createLevelPainter() {
        return (g, w, h, f) -> {
            int lh = (int) Math.round(f * h);
            g.setColor(levelColor);
            g.fillRect(0, h - lh, w, lh);
        };
    }

    @Override public PeakPainter createPeakPainter() {
        return (g, w, h, pf, lf) -> {
            int ph = (int) Math.round(pf * h);
            g.setColor(peakColor);
            g.fillRect(0, Math.max(0, h - ph - peakThickness / 2), w, peakThickness);
        };
    }
}