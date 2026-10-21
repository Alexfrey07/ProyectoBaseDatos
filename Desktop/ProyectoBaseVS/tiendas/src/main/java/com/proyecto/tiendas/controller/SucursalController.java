package com.proyecto.tiendas.controller;

import com.proyecto.tiendas.entity.Sucursal;
import com.proyecto.tiendas.service.SucursalService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sucursales")
public class SucursalController {
    private final SucursalService service;

    public SucursalController(SucursalService service) {
        this.service = service;
    }

    @GetMapping
    public List<Sucursal> listar() {
        return service.listar();
    }

    @PostMapping
    public Sucursal crear(@RequestBody Sucursal s) {
        s.setId(null);
        return service.guardar(s);
    }

    @PutMapping("/{id}")
    public Sucursal actualizar(@PathVariable Long id, @RequestBody Sucursal s) {
        s.setId(id);
        return service.guardar(s);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}