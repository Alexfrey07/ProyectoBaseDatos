package com.proyecto.tiendas.dto;

public record ReclamoRequest(
        Long idGarantia,
        Long idSucursal,
        String descripcion,
        Integer cantidad) {

    public record Cierre(
            String tipoResolucion,
            Integer cantidadAprobada,
            Long idProductoReemplazo) {}
}