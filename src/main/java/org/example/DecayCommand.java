package org.example;

/**
 * Интерфейс команд, которые GUI может отправить контроллеру.
 * Контроллер реализует этот интерфейс.
 */
public interface DecayCommand {

    /** Установить период полураспада (секунды). */
    void setHalfLife(double seconds);

    /** Загрузить новый образец из n ядер (опыт начинается заново). */
    void setSampleSize(int atoms);

    /** Перезагрузить образец того же размера (опыт начинается заново). */
    void reset();

    /** Запустить/приостановить моделирование. */
    void setRunning(boolean running);
}
