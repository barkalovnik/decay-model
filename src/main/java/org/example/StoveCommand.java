package org.example;

/**
 * Интерфейс команд, которые GUI может отправить контроллеру.
 * Контроллер реализует этот интерфейс.
 */
public interface StoveCommand {

    /** Установить уровень конфорки (0..10). */
    void setBurnerLevel(int level);

    /** Открыть/закрыть крышку. */
    void setLidOpen(boolean open);

    /** Установить объём содержимого (литры). */
    void setVolume(double volume);

    /** Запустить/остановить моделирование. */
    void setRunning(boolean running);
}