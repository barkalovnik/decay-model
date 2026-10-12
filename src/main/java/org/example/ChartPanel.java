package org.example;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Панель с графиком числа нераспавшихся ядер в реальном времени (XChart).
 * Рядом с измеренной (случайной) кривой рисуется теоретическая N0·2^(-t/T½).
 * Методы вызываются в потоке EDT (контроллер доставляет события через invokeLater).
 */
public class ChartPanel extends JPanel implements DecayEventListener {

    private static final String MEASURED = "Измерено";
    private static final String THEORY = "Теория";
    private static final int MAX_POINTS = 300; // 60 секунд истории

    private final XYChart chart;
    private final List<Double> timeData = new ArrayList<>();
    private final List<Double> measuredData = new ArrayList<>();
    private final List<Double> theoryData = new ArrayList<>();

    private double halfLife = DecayModel.DEFAULT_HALF_LIFE;
    private double lastTime = 0;
    private double theoryValue = DecayModel.DEFAULT_ATOMS;

    public ChartPanel() {
        setLayout(new BorderLayout());

        chart = new XYChartBuilder()
                .width(500).height(300)
                .title("Число нераспавшихся ядер")
                .xAxisTitle("Время (с)")
                .yAxisTitle("Ядер")
                .theme(Styler.ChartTheme.GGPlot2)
                .build();

        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
        chart.getStyler().setYAxisMin(0.0);
        chart.getStyler().setYAxisMax(DecayModel.DEFAULT_ATOMS * 1.05);

        // начальная точка — и в серии, и в наших списках
        timeData.add(0.0);
        measuredData.add((double) DecayModel.DEFAULT_ATOMS);
        theoryData.add((double) DecayModel.DEFAULT_ATOMS);
        chart.addSeries(MEASURED, timeData, measuredData).setMarker(SeriesMarkers.NONE);
        chart.addSeries(THEORY, timeData, theoryData).setMarker(SeriesMarkers.NONE);

        add(new XChartPanel<>(chart), BorderLayout.CENTER);
        setBorder(BorderFactory.createTitledBorder("График"));
    }

    @Override
    public void onDataUpdated(double time, int remaining, double activity) {
        // теоретическая кривая считается шагами, чтобы учитывать смену периода полураспада
        theoryValue *= Math.pow(2.0, -(time - lastTime) / halfLife);
        lastTime = time;

        timeData.add(time);
        measuredData.add((double) remaining);
        theoryData.add(theoryValue);

        // ограничиваем историю
        if (timeData.size() > MAX_POINTS) {
            timeData.remove(0);
            measuredData.remove(0);
            theoryData.remove(0);
        }

        refreshSeries();
    }

    @Override
    public void onHalfLifeChanged(double halfLifeSeconds) {
        this.halfLife = halfLifeSeconds;
    }

    @Override
    public void onSampleSizeChanged(int atoms) {
        timeData.clear();
        measuredData.clear();
        theoryData.clear();

        lastTime = 0;
        theoryValue = atoms;
        timeData.add(0.0);
        measuredData.add((double) atoms);
        theoryData.add((double) atoms);

        chart.getStyler().setYAxisMax(atoms * 1.05);
        refreshSeries();
    }

    private void refreshSeries() {
        chart.updateXYSeries(MEASURED, timeData, measuredData, null);
        chart.updateXYSeries(THEORY, timeData, theoryData, null);
        repaint();
    }
}
