package com.proyecto.tiendas.dto;

public record SeguridadRequest() {

    public record UsuarioNuevo(Long idEmpleado, Long idRol, String username, String password) {}

    public record UsuarioCambio(Long idRol, String estado) {}

    public record Clave(String password) {}

    public record RolNuevo(String nombre, String descripcion) {}

    public record Permiso(Long idMenu, String lectura, String escritura, String edicion, String eliminacion) {}
}