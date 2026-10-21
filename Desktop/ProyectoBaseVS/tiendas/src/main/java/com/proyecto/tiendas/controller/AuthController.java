package com.proyecto.tiendas.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final JdbcTemplate jdbc;

    public AuthController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping("/menu")
    public List<Map<String, Object>> menu(Authentication auth) {
        return jdbc.queryForList(
            "SELECT ROL, ID_MENU, ID_MENU_PADRE, NOMBRE_OPCION, URL_PANTALLA, ICONO, ORDEN, " +
            "PERMISO_LECTURA, PERMISO_ESCRITURA, PERMISO_EDICION, PERMISO_ELIMINACION " +
            "FROM VW_MENU_USUARIO WHERE USERNAME = ? ORDER BY ORDEN", auth.getName());
    }
}