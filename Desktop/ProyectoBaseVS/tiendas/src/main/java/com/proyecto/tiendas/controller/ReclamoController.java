package com.proyecto.tiendas.controller;

import com.proyecto.tiendas.dto.ReclamoRequest;
import com.proyecto.tiendas.service.ReclamoService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reclamos")
public class ReclamoController {
    private final ReclamoService service;

    public ReclamoController(ReclamoService service) { this.service = service; }

    @GetMapping
    public List<Map<String, Object>> listar() { return service.listar(); }

    @GetMapping("/garantias")
    public List<Map<String, Object>> garantias() { return service.garantias(); }

    @PostMapping
    public void crear(@RequestBody ReclamoRequest r, Authentication auth) {
        service.crear(r, auth.getName());
    }

    @PutMapping("/{id}/proceso")
    public void enProceso(@PathVariable Long id) { service.enProceso(id); }

    @PutMapping("/{id}/cerrar")
    public void cerrar(@PathVariable Long id, @RequestBody ReclamoRequest.Cierre c) {
        service.cerrar(id, c);
    }
}