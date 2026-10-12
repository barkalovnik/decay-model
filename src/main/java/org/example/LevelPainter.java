package org.example;

import java.awt.Graphics2D;

/** Продукт семейства: отрисовка текущего уровня. fraction – уровень в долях шкалы [0..1]. */
public interface LevelPainter {
    void paint(Graphics2D g, int w, int h, double fraction);
}