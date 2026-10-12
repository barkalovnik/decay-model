package org.example;

/**
 * Физическая модель кастрюли на плите.
 * Учитывает нагрев от конфорки и теплоотдачу через крышку/открытую поверхность.
 */
public class PotModel {

    // --- физические параметры ---
    private static final double AMBIENT_TEMP = 20.0;      // температура окружающей среды, °C
    private static final double BOILING_TEMP = 100.0;     // температура кипения воды, °C
    private static final double MAX_BURNER_POWER = 2000.0; // максимальная мощность конфорки, Вт
    private static final double HEAT_CAPACITY = 4200.0;   // удельная теплоёмкость воды, Дж/(кг·°C)
    private static final double DENSITY = 1.0;            // плотность воды, кг/л

    // --- состояние ---
    private double temperature = AMBIENT_TEMP; // текущая температура, °C
    private double volume = 2.0;               // объём воды, л
    private int burnerLevel = 0;               // уровень конфорки 0..10
    private boolean lidOpen = true;            // крышка открыта?
    private boolean boiling = false;           // кипит ли вода?

    // --- геттеры ---
    public double getTemperature() { return temperature; }
    public double getVolume() { return volume; }
    public int getBurnerLevel() { return burnerLevel; }
    public boolean isLidOpen() { return lidOpen; }
    public boolean isBoiling() { return boiling; }

    // --- сеттеры (команды) ---
    public void setBurnerLevel(int level) {
        this.burnerLevel = Math.max(0, Math.min(10, level));
    }

    public void setLidOpen(boolean open) {
        this.lidOpen = open;
    }

    public void setVolume(double volume) {
        this.volume = Math.max(0.1, volume);
    }

    /**
     * Один шаг моделирования за интервал dt (секунды).
     */
    public void step(double dt) {
        // 1. Подводимая мощность от конфорки
        double burnerPower = MAX_BURNER_POWER * (burnerLevel / 10.0);

        // 2. Теплоотдача: зависит от состояния крышки
        double coolingCoeff = lidOpen ? 25.0 : 8.0; // Вт/°C
        double heatLoss = coolingCoeff * (temperature - AMBIENT_TEMP);

        // 3. Если вода кипит, дополнительно тратится энергия на парообразование
        double boilingLoss = 0;
        if (boiling) {
            boilingLoss = 500.0; // упрощённо: 500 Вт на испарение
        }

        // 4. Изменение температуры: Q = m * c * ΔT
        double mass = volume * DENSITY; // кг
        double netPower = burnerPower - heatLoss - boilingLoss;
        double dT = netPower * dt / (mass * HEAT_CAPACITY);
        temperature += dT;

        // 5. Ограничения
        if (temperature < AMBIENT_TEMP) temperature = AMBIENT_TEMP;

        // 6. Определение кипения
        if (temperature >= BOILING_TEMP) {
            temperature = BOILING_TEMP; // не выше 100°C при нормальных условиях
            boiling = true;
        } else {
            boiling = false;
        }
    }
}