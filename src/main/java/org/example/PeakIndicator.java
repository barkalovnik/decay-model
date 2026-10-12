package org.example;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Пиковый индикатор уровня: вертикальная полоска.
 *  - уровень и инертный максимум (линейно снижающийся со временем) рисуют
 *    painter-ы, созданные абстрактной фабрикой IndicatorFactory;
 *  - при превышении максимального порога генерируется ThresholdEvent.
 * Накапливает полученные данные (getHistory) и возвращает их.
 */
public class PeakIndicator extends JPanel implements DataListener {
    private static final long serialVersionUID = 1L;
    private double min, max;                 // пороги / границы шкалы
    private int inertiaMs;                   // время падения пика от max до min
    private LevelPainter levelPainter;       // продукты абстрактной фабрики
    private PeakPainter  peakPainter;
    private int historyLimit = 10000;

    private double level;                    // текущий уровень
    private double peak;                     // положение красной полоски
    private boolean above;                   // был ли превышен порог на прошлом шаге
    private long lastTime;

    private final List<Double> history = new ArrayList<>();
    private final List<ThresholdListener> listeners = new ArrayList<>();
    private final Timer decayTimer;

    /** Индикатор классического вида (для совместимости). */
    public PeakIndicator(double min, double max, int inertiaMs, Dimension scaleSize) {
        this(min, max, inertiaMs, scaleSize,
                new ClassicIndicatorFactory().createLevelPainter(),
                new ClassicIndicatorFactory().createPeakPainter());
    }

    public PeakIndicator(double min, double max, int inertiaMs, Dimension scaleSize,
                         LevelPainter levelPainter, PeakPainter peakPainter) {
        this.levelPainter = levelPainter;
        this.peakPainter = peakPainter;
        if (min >= max) throw new IllegalArgumentException("min must be < max");
        this.min = min;
        this.max = max;
        this.inertiaMs = inertiaMs;
        this.level = this.peak = min;
        setPreferredSize(scaleSize);
        setBackground(Color.WHITE);
        setOpaque(true);

        lastTime = System.nanoTime();
        decayTimer = new Timer(20, e -> decay());
        decayTimer.start();
    }

    // ---------- приём данных ----------
    @Override
    public void onNextValue(double value) {
        history.add(value);
        if (history.size() > historyLimit) history.remove(0);

        level = clamp(value);
        if (level > peak) peak = level;              // новый пик

        boolean nowAbove = value > max;
        if (nowAbove && !above) fireThresholdExceeded(value);   // только в момент пересечения
        above = nowAbove;
        repaint();
    }

    /** Линейное снижение красной полоски: за inertiaMs она проходит всю шкалу. */
    private void decay() {
        long now = System.nanoTime();
        double dtMs = (now - lastTime) / 1e6;
        lastTime = now;
        if (peak > level) {
            peak = Math.max(level, peak - (max - min) * dtMs / inertiaMs);
            repaint();
        }
    }

    private double clamp(double v) { return Math.max(min, Math.min(max, v)); }

    // ---------- события ----------
    public void addThresholdListener(ThresholdListener l)    { listeners.add(l); }
    public void removeThresholdListener(ThresholdListener l) { listeners.remove(l); }

    private void fireThresholdExceeded(double value) {
        ThresholdEvent e = new ThresholdEvent(this, value, max);
        for (ThresholdListener l : new ArrayList<>(listeners)) l.thresholdExceeded(e);
    }

    // ---------- накопленные данные ----------
    public List<Double> getHistory() { return Collections.unmodifiableList(history); }
    public double getLevel() { return level; }
    public double getPeak()  { return peak; }
    public void clearHistory() { history.clear(); }

    // ---------- параметры ----------
    public double getMin() { return min; }
    public double getMax() { return max; }
    public void setRange(double min, double max) {
        if (min >= max) throw new IllegalArgumentException("min must be < max");
        this.min = min; this.max = max; repaint();
    }
    public int  getInertiaMs() { return inertiaMs; }
    public void setInertiaMs(int ms) { this.inertiaMs = Math.max(1, ms); }
    public void setLevelPainter(LevelPainter p) { this.levelPainter = p; repaint(); }
    public void setPeakPainter(PeakPainter p)   { this.peakPainter = p; repaint(); }
    public void setHistoryLimit(int n) { this.historyLimit = n; }

    // ---------- отрисовка ----------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth(), h = getHeight();

        levelPainter.paint(g2, w, h, fraction(level));
        peakPainter.paint(g2, w, h, fraction(peak), fraction(level));

        g2.setColor(Color.DARK_GRAY);
        g2.drawRect(0, 0, w - 1, h - 1);
        g2.dispose();
    }

    private double fraction(double v) { return (v - min) / (max - min); }
}