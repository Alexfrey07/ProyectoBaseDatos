package com.proyecto.tiendas.controller;

import com.proyecto.tiendas.entity.Puesto;
import com.proyecto.tiendas.service.PuestoService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/puestos")
public class PuestoController {
    private final PuestoService service;

    public PuestoController(PuestoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Puesto> listar() {
        return service.listar();
    }

    @PostMapping
    public Puesto crear(@RequestBody Puesto p) {
        p.setId(null);
        return service.guardar(p);
    }

    @PutMapping("/{id}")
    public Puesto actualizar(@PathVariable Long id, @RequestBody Puesto p) {
        p.setId(id);
        return service.guardar(p);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}