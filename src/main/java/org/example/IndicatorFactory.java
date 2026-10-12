package org.example;

import java.awt.Dimension;

/**
 * Абстрактная фабрика семейства «пиковый индикатор».
 * Конкретные фабрики создают согласованную пару: painter уровня + painter пика.
 */
public abstract class IndicatorFactory {
    public abstract LevelPainter createLevelPainter();
    public abstract PeakPainter createPeakPainter();

    /** Собирает индикатор из продуктов этой фабрики. */
    public PeakIndicator createIndicator(double min, double max, int inertiaMs, Dimension size) {
        return new PeakIndicator(min, max, inertiaMs, size, createLevelPainter(), createPeakPainter());
    }
}