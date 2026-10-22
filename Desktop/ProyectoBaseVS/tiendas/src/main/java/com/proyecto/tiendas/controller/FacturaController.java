package com.proyecto.tiendas.controller;

import com.proyecto.tiendas.dto.FacturaRequest;
import com.proyecto.tiendas.service.FacturaService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/facturas")
public class FacturaController {
    private final FacturaService service;

    public FacturaController(FacturaService service) { this.service = service; }

    @GetMapping
    public List<Map<String, Object>> listar() { return service.listar(); }

    @GetMapping("/{id}/detalle")
    public Map<String, Object> detalle(@PathVariable Long id) { return service.detalle(id); }

    @GetMapping("/productos")
    public List<Map<String, Object>> productos(@RequestParam Long idSucursal) {
        return service.productosDisponibles(idSucursal);
    }

    @GetMapping("/tipos-pago")
    public List<Map<String, Object>> tiposPago() { return service.tiposPago(); }

    @PostMapping
    public Map<String, Object> crear(@RequestBody FacturaRequest r, Authentication auth) {
        return service.crear(r, auth.getName());
    }
}