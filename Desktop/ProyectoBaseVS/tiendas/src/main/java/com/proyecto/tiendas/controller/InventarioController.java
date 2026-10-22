package com.proyecto.tiendas.controller;

import com.proyecto.tiendas.dto.InventarioRequest;
import com.proyecto.tiendas.service.InventarioService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {
    private final InventarioService service;

    public InventarioController(InventarioService service) { this.service = service; }

    @GetMapping
    public List<Map<String, Object>> listar(@RequestParam(required = false) Long idSucursal) {
        return service.listar(idSucursal);
    }

    @GetMapping("/kardex")
    public List<Map<String, Object>> kardex(@RequestParam(required = false) Long idSucursal) {
        return service.kardex(idSucursal);
    }

    @PutMapping("/ajustar")
    public void ajustar(@RequestBody InventarioRequest.Ajuste a) { service.ajustar(a); }

    @PutMapping("/{id}/minimo")
    public void cambiarMinimo(@PathVariable Long id, @RequestBody InventarioRequest.Minimo m) {
        service.cambiarMinimo(id, m);
    }
}