package com.proyecto.tiendas.controller;

import com.proyecto.tiendas.entity.Proveedor;
import com.proyecto.tiendas.service.ProveedorService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/proveedores")
public class ProveedorController {
    private final ProveedorService service;

    public ProveedorController(ProveedorService service) { this.service = service; }

    @GetMapping
    public List<Proveedor> listar() { return service.listar(); }

    @PostMapping
    public Proveedor crear(@RequestBody Proveedor p) { p.setId(null); return service.guardar(p); }

    @PutMapping("/{id}")
    public Proveedor actualizar(@PathVariable Long id, @RequestBody Proveedor p) { p.setId(id); return service.guardar(p); }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) { service.eliminar(id); }
}