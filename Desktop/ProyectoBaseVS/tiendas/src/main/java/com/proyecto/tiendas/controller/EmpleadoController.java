package com.proyecto.tiendas.controller;

import com.proyecto.tiendas.entity.Empleado;
import com.proyecto.tiendas.service.EmpleadoService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/empleados")
public class EmpleadoController {
    private final EmpleadoService service;

    public EmpleadoController(EmpleadoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Empleado> listar() {
        return service.listar();
    }

    @PostMapping
    public Empleado crear(@RequestBody Empleado e) {
        e.setId(null);
        return service.guardar(e);
    }

    @PutMapping("/{id}")
    public Empleado actualizar(@PathVariable Long id, @RequestBody Empleado e) {
        e.setId(id);
        return service.guardar(e);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}