package org.example;

import javax.swing.*;
import java.awt.*;

/**
 * Панель с пиковым индикатором активности образца (из л.р.3).
 * Подписывается на события контроллера.
 */
public class IndicatorPanel extends JPanel implements DecayEventListener {

    private final PeakIndicator indicator;
    private final JLabel halfLabel = new JLabel();
    private final JLabel remainingLabel = new JLabel();

    private int atoms = DecayModel.DEFAULT_ATOMS;
    private double halfLife = DecayModel.DEFAULT_HALF_LIFE;

    public IndicatorPanel() {
        setLayout(new BorderLayout());

        // Пиковый индикатор: шкала с запасом относительно начальной активности,
        // инерция пика 5 секунд
        indicator = new PeakIndicator(0, expectedMaxActivity(), 5000, new Dimension(80, 300));
        add(indicator, BorderLayout.CENTER);

        halfLabel.setHorizontalAlignment(SwingConstants.CENTER);
        halfLabel.setFont(new Font("Arial", Font.BOLD, 14));
        remainingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        resetLabels();

        JPanel south = new JPanel(new GridLayout(2, 1));
        south.add(remainingLabel);
        south.add(halfLabel);
        add(south, BorderLayout.SOUTH);

        setBorder(BorderFactory.createTitledBorder("Активность (расп./с)"));
    }

    /** Ожидаемая начальная активность A0 = N0·ln2/T½ с двукратным запасом по шкале. */
    private double expectedMaxActivity() {
        return 2.0 * atoms * Math.log(2) / halfLife;
    }

    private void updateScale() {
        indicator.setRange(0, expectedMaxActivity());
    }

    private void resetLabels() {
        remainingLabel.setText("Осталось ядер: " + atoms);
        halfLabel.setText("Распалось меньше половины");
        halfLabel.setForeground(Color.BLACK);
    }

    @Override
    public void onDataUpdated(double time, int remaining, double activity) {
        indicator.onNextValue(activity);
        remainingLabel.setText("Осталось ядер: " + remaining);
    }

    @Override
    public void onHalfDecayedChanged(boolean halfDecayed) {
        halfLabel.setText(halfDecayed ? "ПОЛОВИНА РАСПАЛАСЬ!" : "Распалось меньше половины");
        halfLabel.setForeground(halfDecayed ? Color.RED : Color.BLACK);
    }

    @Override
    public void onHalfLifeChanged(double halfLifeSeconds) {
        this.halfLife = halfLifeSeconds;
        updateScale();
    }

    @Override
    public void onSampleSizeChanged(int atoms) {
        this.atoms = atoms;
        updateScale();
        indicator.clearHistory();
        resetLabels();
    }
}
