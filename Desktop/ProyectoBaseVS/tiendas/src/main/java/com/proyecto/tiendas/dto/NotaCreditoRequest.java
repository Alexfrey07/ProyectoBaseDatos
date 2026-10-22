package com.proyecto.tiendas.dto;

import java.util.List;

public record NotaCreditoRequest(
        Long idFactura,
        String motivo,
        List<Linea> lineas) {

    public record Linea(Long idDetalleFactura, Integer cantidad, String reingresa) {}
}