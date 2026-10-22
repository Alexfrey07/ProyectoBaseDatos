package com.proyecto.tiendas.controller;

import com.proyecto.tiendas.dto.SeguridadRequest;
import com.proyecto.tiendas.service.SeguridadService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
public class SeguridadController {
    private final SeguridadService service;

    public SeguridadController(SeguridadService service) {
        this.service = service;
    }

    // Usuarios
    @GetMapping("/api/usuarios")
    public List<Map<String, Object>> usuarios() {
        return service.usuarios();
    }

    @GetMapping("/api/usuarios/empleados-libres")
    public List<Map<String, Object>> empleadosLibres() {
        return service.empleadosLibres();
    }

    @GetMapping("/api/usuarios/roles")
    public List<Map<String, Object>> rolesOpciones() {
        return service.rolesOpciones();
    }

    @PostMapping("/api/usuarios")
    public void crearUsuario(@RequestBody SeguridadRequest.UsuarioNuevo n) {
        service.crearUsuario(n);
    }

    @PutMapping("/api/usuarios/{id}")
    public void actualizarUsuario(@PathVariable Long id, @RequestBody SeguridadRequest.UsuarioCambio c,
            Authentication auth) {
        service.actualizarUsuario(id, c, auth.getName());
    }

    @PutMapping("/api/usuarios/{id}/clave")
    public void cambiarClave(@PathVariable Long id, @RequestBody SeguridadRequest.Clave c) {
        service.cambiarClave(id, c);
    }

    // Roles y permisos
    @GetMapping("/api/roles")
    public List<Map<String, Object>> roles() {
        return service.roles();
    }

    @PostMapping("/api/roles")
    public void crearRol(@RequestBody SeguridadRequest.RolNuevo r) {
        service.crearRol(r);
    }

    @DeleteMapping("/api/roles/{id}")
    public void eliminarRol(@PathVariable Long id) {
        service.eliminarRol(id);
    }

    @GetMapping("/api/roles/{id}/permisos")
    public List<Map<String, Object>> permisos(@PathVariable Long id) {
        return service.permisos(id);
    }

    @PutMapping("/api/roles/{id}/permisos")
    public void guardarPermisos(@PathVariable Long id, @RequestBody List<SeguridadRequest.Permiso> items) {
        service.guardarPermisos(id, items);
    }

    // Bitácora
    @GetMapping("/api/bitacora")
    public List<Map<String, Object>> bitacora(@RequestParam(required = false) String tabla,
            @RequestParam(required = false) String tipo) {
        return service.bitacora(tabla, tipo);
    }

    @GetMapping("/api/bitacora/tablas")
    public List<String> tablas() {
        return service.tablasBitacora();
    }
}