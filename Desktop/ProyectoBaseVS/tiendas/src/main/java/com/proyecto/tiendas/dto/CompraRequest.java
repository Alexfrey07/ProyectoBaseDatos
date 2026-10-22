package com.proyecto.tiendas.dto;

import java.math.BigDecimal;
import java.util.List;

public record CompraRequest(
        Long idProveedor,
        Long idSucursal,
        String numeroDocumento,
        String observaciones,
        List<Linea> lineas) {

    public record Linea(Long idProducto, Integer cantidad, BigDecimal costoUnitario) {}
}