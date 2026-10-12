package org.example;

/**
 * Слушатель событий, генерируемых контроллером.
 * GUI-классы реализуют этот интерфейс и подписываются на события.
 * Методы по умолчанию пустые: подписчик переопределяет только нужные ему.
 */
public interface DecayEventListener {

    /**
     * Периодические данные (каждый шаг модели).
     * @param time      время с начала опыта, с
     * @param remaining число нераспавшихся ядер
     * @param activity  активность на последнем шаге, распадов/с
     */
    default void onDataUpdated(double time, int remaining, double activity) { }

    /** Распалась половина исходных ядер (true) или опыт начат заново (false). */
    default void onHalfDecayedChanged(boolean halfDecayed) { }

    /** Период полураспада изменился. */
    default void onHalfLifeChanged(double halfLifeSeconds) { }

    /** Загружен новый образец — опыт начался заново, подписчикам нужно сбросить данные. */
    default void onSampleSizeChanged(int atoms) { }

    /** Моделирование запущено или приостановлено. */
    default void onRunningChanged(boolean running) { }
}
