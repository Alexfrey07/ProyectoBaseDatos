package com.proyecto.tiendas.service;

import com.proyecto.tiendas.dto.ReporteDef;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ExcelExporter {

    public static byte[] generar(ReporteDef d, List<Map<String, Object>> filas) throws IOException {
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Sheet hoja = libro.createSheet("Reporte");

            CellStyle estiloCabecera = libro.createCellStyle();
            org.apache.poi.ss.usermodel.Font negrita = libro.createFont();
            negrita.setBold(true);
            estiloCabecera.setFont(negrita);

            Row cabecera = hoja.createRow(0);
            for (int i = 0; i < d.columnas().size(); i++) {
                Cell c = cabecera.createCell(i);
                c.setCellValue(d.columnas().get(i).titulo());
                c.setCellStyle(estiloCabecera);
            }

            int n = 1;
            for (Map<String, Object> fila : filas) {
                Row r = hoja.createRow(n++);
                for (int i = 0; i < d.columnas().size(); i++) {
                    Object v = fila.get(d.columnas().get(i).campo());
                    Cell c = r.createCell(i);
                    if (v instanceof Number num)
                        c.setCellValue(num.doubleValue());
                    else
                        c.setCellValue(v == null ? "" : v.toString());
                }
            }
            for (int i = 0; i < d.columnas().size(); i++)
                hoja.autoSizeColumn(i);

            libro.write(salida);
            return salida.toByteArray();
        }
    }
}