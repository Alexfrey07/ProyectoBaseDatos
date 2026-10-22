package com.proyecto.tiendas.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.proyecto.tiendas.dto.ReporteDef;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PdfExporter {

    public static byte[] generar(ReporteDef d, List<Map<String, Object>> filas) throws Exception {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        PdfWriter.getInstance(doc, salida);
        doc.open();

        doc.add(new Paragraph(d.titulo(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
        doc.add(new Paragraph(
                "Generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                FontFactory.getFont(FontFactory.HELVETICA, 9)));
        doc.add(new Paragraph(" "));

        PdfPTable tabla = new PdfPTable(d.columnas().size());
        tabla.setWidthPercentage(100);

        for (ReporteDef.Columna c : d.columnas()) {
            PdfPCell celda = new PdfPCell(
                    new Phrase(c.titulo(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
            celda.setBackgroundColor(Color.DARK_GRAY);
            celda.setPadding(5);
            tabla.addCell(celda);
        }
        for (Map<String, Object> fila : filas) {
            for (ReporteDef.Columna c : d.columnas()) {
                Object v = fila.get(c.campo());
                String texto;
                if (v == null)
                    texto = "";
                else if (v instanceof Number n && c.campo().equals("total"))
                    texto = String.format(Locale.US, "Q %,.2f", n.doubleValue());
                else
                    texto = v.toString();
                PdfPCell celda = new PdfPCell(new Phrase(texto, FontFactory.getFont(FontFactory.HELVETICA, 9)));
                celda.setPadding(4);
                if (v instanceof Number)
                    celda.setHorizontalAlignment(Element.ALIGN_RIGHT);
                tabla.addCell(celda);
            }
        }
        doc.add(tabla);
        doc.close();
        return salida.toByteArray();
    }
}