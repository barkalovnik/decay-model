package org.example;

/** Обработчик события превышения максимального порога. */
public interface ThresholdListener {
    void thresholdExceeded(ThresholdEvent e);
}