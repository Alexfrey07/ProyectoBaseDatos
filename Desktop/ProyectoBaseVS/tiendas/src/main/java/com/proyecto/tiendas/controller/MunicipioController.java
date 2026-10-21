package com.proyecto.tiendas.controller;

import com.proyecto.tiendas.entity.Municipio;
import com.proyecto.tiendas.service.MunicipioService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/municipios")
public class MunicipioController {
    private final MunicipioService service;

    public MunicipioController(MunicipioService service) {
        this.service = service;
    }

    @GetMapping
    public List<Municipio> listar() {
        return service.listar();
    }

    @PostMapping
    public Municipio crear(@RequestBody Municipio m) {
        m.setId(null);
        return service.guardar(m);
    }

    @PutMapping("/{id}")
    public Municipio actualizar(@PathVariable Long id, @RequestBody Municipio m) {
        m.setId(id);
        return service.guardar(m);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}