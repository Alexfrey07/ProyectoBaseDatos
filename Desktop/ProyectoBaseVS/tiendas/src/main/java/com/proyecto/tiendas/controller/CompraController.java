package com.proyecto.tiendas.controller;

import com.proyecto.tiendas.dto.CompraRequest;
import com.proyecto.tiendas.service.CompraService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/compras")
public class CompraController {
    private final CompraService service;

    public CompraController(CompraService service) {
        this.service = service;
    }

    @GetMapping
    public List<Map<String, Object>> listar() {
        return service.listar();
    }

    @GetMapping("/{id}/detalle")
    public List<Map<String, Object>> detalle(@PathVariable Long id) {
        return service.detalle(id);
    }

    @PostMapping
    public Map<String, Object> crear(@RequestBody CompraRequest r, Authentication auth) {
        return Map.of("id", service.crear(r, auth.getName()));
    }

    @PutMapping("/{id}/recibir")
    public void recibir(@PathVariable Long id) {
        service.recibir(id);
    }

    @PutMapping("/{id}/cancelar")
    public void cancelar(@PathVariable Long id) {
        service.cancelar(id);
    }
}