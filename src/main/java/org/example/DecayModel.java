package org.example;

import java.util.Random;

/**
 * Физическая модель радиоактивного распада образца.
 * Каждое ядро за интервал dt распадается независимо с вероятностью
 * p = 1 - 2^(-dt / T½), поэтому число нераспавшихся ядер N(t) следует закону
 * N0 · 2^(-t / T½), но со случайными (пуассоновскими) флуктуациями.
 *
 * Модель не потокобезопасна: к ней обращается только поток контроллера.
 */
public class DecayModel {

    public static final int DEFAULT_ATOMS = 1000;        // число ядер в образце по умолчанию
    public static final double DEFAULT_HALF_LIFE = 10.0; // период полураспада по умолчанию, с

    // --- состояние ---
    private final Random random = new Random();
    private int initial = DEFAULT_ATOMS;       // ядер в образце в момент t = 0
    private int remaining = DEFAULT_ATOMS;     // нераспавшихся ядер
    private double halfLife = DEFAULT_HALF_LIFE; // период полураспада, с
    private double time = 0;                   // время с начала опыта, с
    private int lastDecayed = 0;               // распадов на последнем шаге
    private double lastDt = 1;                 // длительность последнего шага, с

    // --- геттеры ---
    public int getInitial() { return initial; }
    public int getRemaining() { return remaining; }
    public double getHalfLife() { return halfLife; }
    public double getTime() { return time; }

    /** Активность образца на последнем шаге, распадов в секунду (Бк). */
    public double getActivity() { return lastDecayed / lastDt; }

    /** Распалась ли уже половина исходных ядер. */
    public boolean isHalfDecayed() { return remaining <= initial / 2.0; }

    // --- команды ---
    public void setHalfLife(double seconds) {
        this.halfLife = Math.max(0.1, seconds);
    }

    /** Загрузить новый образец из n ядер и начать опыт заново. */
    public void load(int atoms) {
        this.initial = Math.max(1, atoms);
        this.remaining = initial;
        this.time = 0;
        this.lastDecayed = 0;
    }

    /** Один шаг моделирования за интервал dt (секунды). */
    public void step(double dt) {
        // вероятность распада одного ядра за dt
        double p = 1.0 - Math.pow(2.0, -dt / halfLife);

        int decayed = 0;
        for (int i = 0; i < remaining; i++) {
            if (random.nextDouble() < p) decayed++;
        }

        remaining -= decayed;
        lastDecayed = decayed;
        lastDt = dt;
        time += dt;
    }
}
