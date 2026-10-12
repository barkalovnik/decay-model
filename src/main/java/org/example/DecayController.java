package org.example;

import javax.swing.SwingUtilities;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Контроллер — «сердце» системы.
 * Реализует интерфейс команд и работает в отдельном потоке.
 * Периодически обновляет модель и уведомляет всех подписчиков.
 *
 * Все обращения к модели (и шаги, и команды) выполняются в одном потоке
 * контроллера, поэтому модель не требует синхронизации.
 */
public class DecayController implements DecayCommand {

    private static final double DT = 0.2; // шаг модели, с (200 мс)

    private final DecayModel model = new DecayModel();
    private final List<DecayEventListener> listeners = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService executor =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "DecayController-Thread");
                t.setDaemon(true);
                return t;
            });

    private volatile boolean running = true;
    private boolean halfDecayed = false; // используется только в потоке контроллера

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
    public void addListener(DecayEventListener l) {
        listeners.add(l);
    }

    public void removeListener(DecayEventListener l) {
        listeners.remove(l);
    }

    // ---------- команды ----------
    @Override
    public void setHalfLife(double seconds) {
        submit(() -> {
            model.setHalfLife(seconds);
            double t = model.getHalfLife();
            fire(l -> l.onHalfLifeChanged(t));
        });
    }

    @Override
    public void setSampleSize(int atoms) {
        submit(() -> {
            model.load(atoms);
            afterReload();
        });
    }

    @Override
    public void reset() {
        submit(() -> {
            model.load(model.getInitial());
            afterReload();
        });
    }

    @Override
    public void setRunning(boolean running) {
        this.running = running;
        fire(l -> l.onRunningChanged(running));
    }

    // ---------- периодическое обновление модели ----------
    private void tick() {
        if (!running) return;

        model.step(DT);

        double time = model.getTime();
        int remaining = model.getRemaining();
        double activity = model.getActivity();
        fire(l -> l.onDataUpdated(time, remaining, activity));

        // событие «половина распалась» — только в момент смены состояния
        boolean half = model.isHalfDecayed();
        if (half != halfDecayed) {
            halfDecayed = half;
            fire(l -> l.onHalfDecayedChanged(half));
        }
    }

    // ---------- вспомогательное ----------
    private void afterReload() {
        halfDecayed = false;
        int atoms = model.getInitial();
        fire(l -> l.onSampleSizeChanged(atoms));
        fire(l -> l.onHalfDecayedChanged(false));
    }

    /** Выполнить команду в потоке контроллера. */
    private void submit(Runnable task) {
        if (!executor.isShutdown()) executor.execute(task);
    }

    /** Уведомить всех подписчиков (в потоке EDT для безопасности Swing). */
    private void fire(Consumer<DecayEventListener> action) {
        SwingUtilities.invokeLater(() -> {
            for (DecayEventListener l : listeners) action.accept(l);
        });
    }
}
