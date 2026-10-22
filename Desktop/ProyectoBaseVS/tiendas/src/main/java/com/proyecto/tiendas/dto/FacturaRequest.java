package com.proyecto.tiendas.dto;

import java.math.BigDecimal;
import java.util.List;

public record FacturaRequest(
        Long idCliente,
        Long idSucursal,
        List<Linea> lineas,
        List<Pago> pagos) {

    public record Linea(Long idProducto, Integer cantidad) {}

    public record Pago(Long idTipoPago, BigDecimal monto, String referencia) {}
}