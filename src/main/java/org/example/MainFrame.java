package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Главное окно: панель управления + индикатор + график.
 * Подписывается на события контроллера и отписывается при закрытии.
 */
public class MainFrame extends JFrame implements DecayEventListener {

    private static final int HISTORY_LIMIT = 5000;

    private final DecayController controller;
    private final IndicatorPanel indicatorPanel;
    private final ChartPanel chartPanel;

    // элементы управления
    private final JSpinner halfLifeSpinner = new JSpinner(
            new SpinnerNumberModel(DecayModel.DEFAULT_HALF_LIFE, 0.5, 120.0, 0.5));
    private final JSpinner atomsSpinner = new JSpinner(
            new SpinnerNumberModel(DecayModel.DEFAULT_ATOMS, 100, 20000, 100));
    private final JToggleButton pauseBtn = new JToggleButton("Пауза");

    // данные для экспорта
    private final List<Double> timeData = new ArrayList<>();
    private final List<Integer> remainingData = new ArrayList<>();
    private final List<Double> activityData = new ArrayList<>();

    public MainFrame(DecayController controller) {
        super("Радиоактивный распад — модель с подпиской на события");
        this.controller = controller;
        this.indicatorPanel = new IndicatorPanel();
        this.chartPanel = new ChartPanel();

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // ---------- панель управления ----------
        JPanel controlPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Управление"));

        controlPanel.add(new JLabel("Период полураспада (с):"));
        halfLifeSpinner.addChangeListener(e ->
                controller.setHalfLife(((Number) halfLifeSpinner.getValue()).doubleValue()));
        controlPanel.add(halfLifeSpinner);

        controlPanel.add(new JLabel("Число ядер (новый образец):"));
        atomsSpinner.addChangeListener(e ->
                controller.setSampleSize(((Number) atomsSpinner.getValue()).intValue()));
        controlPanel.add(atomsSpinner);

        controlPanel.add(new JLabel("Моделирование:"));
        pauseBtn.addActionListener(e -> controller.setRunning(!pauseBtn.isSelected()));
        controlPanel.add(pauseBtn);

        controlPanel.add(new JLabel("Образец:"));
        JButton resetBtn = new JButton("Загрузить заново");
        resetBtn.addActionListener(e -> controller.reset());
        controlPanel.add(resetBtn);

        controlPanel.add(new JLabel("Данные:"));
        JButton exportBtn = new JButton("Экспорт в Excel");
        exportBtn.addActionListener(e -> exportToExcel());
        controlPanel.add(exportBtn);

        // ---------- компоновка ----------
        add(controlPanel, BorderLayout.WEST);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        centerPanel.add(indicatorPanel);
        centerPanel.add(chartPanel);
        add(centerPanel, BorderLayout.CENTER);

        // ---------- подписка на события ----------
        controller.addListener(this);
        controller.addListener(indicatorPanel);
        controller.addListener(chartPanel);

        // ---------- отписка при закрытии ----------
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.removeListener(MainFrame.this);
                controller.removeListener(indicatorPanel);
                controller.removeListener(chartPanel);
                controller.stop();
                dispose();
                System.exit(0);
            }
        });

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ---------- реализация DecayEventListener ----------
    @Override
    public void onDataUpdated(double time, int remaining, double activity) {
        timeData.add(time);
        remainingData.add(remaining);
        activityData.add(activity);

        // ограничиваем историю
        if (timeData.size() > HISTORY_LIMIT) {
            timeData.remove(0);
            remainingData.remove(0);
            activityData.remove(0);
        }
    }

    @Override
    public void onSampleSizeChanged(int atoms) {
        // новый опыт — старые данные не нужны
        timeData.clear();
        remainingData.clear();
        activityData.clear();
    }

    @Override
    public void onRunningChanged(boolean running) {
        pauseBtn.setSelected(!running);
        pauseBtn.setText(running ? "Пауза" : "Продолжить");
    }

    // ---------- экспорт ----------
    private void exportToExcel() {
        try {
            ExcelExporter.export(timeData, remainingData, activityData, "decay_data.xlsx");
            JOptionPane.showMessageDialog(this,
                    "Данные экспортированы в decay_data.xlsx",
                    "Экспорт завершён", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка экспорта: " + ex.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
}
