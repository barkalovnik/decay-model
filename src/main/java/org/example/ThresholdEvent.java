package org.example;

import java.util.EventObject;

/** Событие: превышен максимальный порог индикатора. */
public class ThresholdEvent extends EventObject {
    private static final long serialVersionUID = 1L;
    private final double value;
    private final double threshold;

    public ThresholdEvent(Object source, double value, double threshold) {
        super(source);
        this.value = value;
        this.threshold = threshold;
    }

    public double getValue() { return value; }
    public double getThreshold() { return threshold; }
}