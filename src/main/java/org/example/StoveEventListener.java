package org.example;

/**
 * Слушатель событий, генерируемых контроллером.
 * GUI-классы реализуют этот интерфейс и подписываются на события.
 */
public interface StoveEventListener {

    /** Температура содержимого изменилась. */
    void onTemperatureChanged(double temperature);

    /** Состояние кипения изменилось. */
    void onBoilingStateChanged(boolean boiling);

    /** Состояние крышки изменилось. */
    void onLidStateChanged(boolean lidOpen);

    /** Уровень конфорки изменился. */
    void onBurnerLevelChanged(int level);

    /** Объём содержимого изменился. */
    void onVolumeChanged(double volume);
}