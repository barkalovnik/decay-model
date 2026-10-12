package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Экспорт накопленных данных в Excel.
 */
public class ExcelExporter {

    public static void export(List<Double> time, List<Integer> remaining,
                              List<Double> activity, String fileName) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Радиоактивный распад");

            // заголовки
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Время (с)");
            header.createCell(1).setCellValue("Осталось ядер");
            header.createCell(2).setCellValue("Активность (расп./с)");

            // данные
            for (int i = 0; i < time.size(); i++) {
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(time.get(i));
                row.createCell(1).setCellValue(remaining.get(i));
                row.createCell(2).setCellValue(activity.get(i));
            }

            // авто-ширина
            for (int i = 0; i < 3; i++) sheet.autoSizeColumn(i);

            try (FileOutputStream fos = new FileOutputStream(fileName)) {
                workbook.write(fos);
            }
        }
    }
}
