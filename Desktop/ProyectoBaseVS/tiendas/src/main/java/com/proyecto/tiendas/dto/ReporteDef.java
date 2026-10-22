package com.proyecto.tiendas.dto;

import java.util.List;

public record ReporteDef(String titulo, String sql, List<Columna> columnas) {

    public record Columna(String campo, String titulo) {}
}