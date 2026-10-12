package org.example;

import java.awt.Graphics2D;

/** Продукт семейства: отрисовка инертного максимума. Значения – в долях шкалы [0..1]. */
public interface PeakPainter {
    void paint(Graphics2D g, int w, int h, double peakFraction, double levelFraction);
}