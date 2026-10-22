package com.proyecto.tiendas.dto;

public record InventarioRequest() {

    public record Ajuste(
            Long idSucursal,
            Long idProducto,
            Integer nuevaExistencia,
            String justificacion) {}

    public record Minimo(Integer stockMinimo) {}
}