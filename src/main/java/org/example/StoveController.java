package org.example;

import javax.swing.SwingUtilities;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Контроллер — «сердце» системы.
 * Реализует интерфейс команд и работает в отдельном потоке.
 * Периодически обновляет модель и уведомляет всех подписчиков.
 */
public class StoveController implements StoveCommand {

    private final PotModel model = new PotModel();
    private final List<StoveEventListener> listeners = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService executor =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "StoveController-Thread");
                t.setDaemon(true);
                return t;
            });

    private volatile boolean running = true;

    /** Запуск контроллера в отдельном потоке. */
    public void start() {
        executor.scheduleAtFixedRate(this::tick, 0, 200, TimeUnit.MILLISECONDS);
    }

    /** Остановка контроллера. */
    public void stop() {
        running = false;
        executor.shutdown();
    }

    // ---------- подписка / отписка ----------
    public void addListener(StoveEventListener l) {
        listeners.add(l);
    }

    public void removeListener(StoveEventListener l) {
        listeners.remove(l);
    }

    // ---------- команды ----------
    @Override
    public void setBurnerLevel(int level) {
        model.setBurnerLevel(level);
        notifyBurnerLevelChanged(level);
    }

    @Override
    public void setLidOpen(boolean open) {
        model.setLidOpen(open);
        notifyLidStateChanged(open);
    }

    @Override
    public void setVolume(double volume) {
        model.setVolume(volume);
        notifyVolumeChanged(volume);
    }

    @Override
    public void setRunning(boolean running) {
        this.running = running;
    }

    // ---------- периодическое обновление модели ----------
    private void tick() {
        if (!running) return;

        double dt = 0.2; // интервал 200 мс
        model.step(dt);

        // Уведомляем подписчиков (в потоке EDT для безопасности Swing)
        double temp = model.getTemperature();
        boolean boiling = model.isBoiling();

        SwingUtilities.invokeLater(() -> {
            for (StoveEventListener l : listeners) {
                l.onTemperatureChanged(temp);
                l.onBoilingStateChanged(boiling);
            }
        });
    }

    // ---------- уведомления об изменении команд ----------
    private void notifyBurnerLevelChanged(int level) {
        SwingUtilities.invokeLater(() -> {
            for (StoveEventListener l : listeners) l.onBurnerLevelChanged(level);
        });
    }

    private void notifyLidStateChanged(boolean open) {
        SwingUtilities.invokeLater(() -> {
            for (StoveEventListener l : listeners) l.onLidStateChanged(open);
        });
    }

    private void notifyVolumeChanged(double volume) {
        SwingUtilities.invokeLater(() -> {
            for (StoveEventListener l : listeners) l.onVolumeChanged(volume);
        });
    }
}