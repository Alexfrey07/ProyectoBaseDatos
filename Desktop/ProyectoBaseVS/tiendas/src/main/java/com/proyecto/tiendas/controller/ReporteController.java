package com.proyecto.tiendas.controller;

import com.proyecto.tiendas.dto.ReporteDef;
import com.proyecto.tiendas.service.ExcelExporter;
import com.proyecto.tiendas.service.PdfExporter;
import com.proyecto.tiendas.service.ReporteService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {
    private final ReporteService service;

    public ReporteController(ReporteService service) {
        this.service = service;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        return service.dashboard();
    }

    @GetMapping("/{tipo}")
    public Map<String, Object> datos(@PathVariable String tipo) {
        return service.datos(tipo);
    }

    @GetMapping("/{tipo}/excel")
    public ResponseEntity<byte[]> excel(@PathVariable String tipo) throws Exception {
        ReporteDef d = service.definicion(tipo);
        byte[] bytes = ExcelExporter.generar(d, service.filas(d));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte-" + tipo + ".xlsx\"")
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @GetMapping("/{tipo}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable String tipo) throws Exception {
        ReporteDef d = service.definicion(tipo);
        byte[] bytes = PdfExporter.generar(d, service.filas(d));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte-" + tipo + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }
}