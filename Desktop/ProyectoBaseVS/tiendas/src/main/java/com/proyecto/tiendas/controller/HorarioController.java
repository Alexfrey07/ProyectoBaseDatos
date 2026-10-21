package com.proyecto.tiendas.controller;

import com.proyecto.tiendas.entity.Horario;
import com.proyecto.tiendas.service.HorarioService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/horarios")
public class HorarioController {
    private final HorarioService service;

    public HorarioController(HorarioService service) { this.service = service; }

    @GetMapping
    public List<Horario> listar() { return service.listar(); }

    @PostMapping
    public Horario crear(@RequestBody Horario h) { h.setId(null); return service.guardar(h); }

    @PutMapping("/{id}")
    public Horario actualizar(@PathVariable Long id, @RequestBody Horario h) { h.setId(id); return service.guardar(h); }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) { service.eliminar(id); }
}