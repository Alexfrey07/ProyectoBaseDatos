package com.proyecto.tiendas.controller;

import com.proyecto.tiendas.dto.NotaCreditoRequest;
import com.proyecto.tiendas.service.NotaCreditoService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notas-credito")
public class NotaCreditoController {
    private final NotaCreditoService service;

    public NotaCreditoController(NotaCreditoService service) { this.service = service; }

    @GetMapping
    public List<Map<String, Object>> listar() { return service.listar(); }

    @GetMapping("/{id}/detalle")
    public List<Map<String, Object>> detalle(@PathVariable Long id) { return service.detalle(id); }

    @GetMapping("/factura/{idFactura}/lineas")
    public List<Map<String, Object>> lineas(@PathVariable Long idFactura) { return service.lineasFactura(idFactura); }

    @PostMapping
    public Map<String, Object> crear(@RequestBody NotaCreditoRequest r, Authentication auth) {
        return service.crear(r, auth.getName());
    }
}