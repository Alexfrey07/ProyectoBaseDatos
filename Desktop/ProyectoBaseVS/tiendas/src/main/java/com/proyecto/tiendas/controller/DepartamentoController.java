package com.proyecto.tiendas.controller;

import com.proyecto.tiendas.entity.Departamento;
import com.proyecto.tiendas.service.DepartamentoService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/departamentos")
public class DepartamentoController {
    private final DepartamentoService service;

    public DepartamentoController(DepartamentoService service) { this.service = service; }

    @GetMapping
    public List<Departamento> listar() { return service.listar(); }

    @PostMapping
    public Departamento crear(@RequestBody Departamento d) { d.setId(null); return service.guardar(d); }

    @PutMapping("/{id}")
    public Departamento actualizar(@PathVariable Long id, @RequestBody Departamento d) { d.setId(id); return service.guardar(d); }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) { service.eliminar(id); }
}