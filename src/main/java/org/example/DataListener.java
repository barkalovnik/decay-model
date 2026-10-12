package org.example;

/** Слушатель источника данных: получает очередное значение с заданной частотой. */
public interface DataListener {
    void onNextValue(double value);
}