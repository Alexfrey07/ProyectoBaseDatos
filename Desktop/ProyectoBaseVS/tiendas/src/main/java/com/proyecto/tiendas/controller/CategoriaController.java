package com.proyecto.tiendas.controller;

import com.proyecto.tiendas.entity.CategoriaProducto;
import com.proyecto.tiendas.service.CategoriaService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {
    private final CategoriaService service;

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }

    @GetMapping
    public List<CategoriaProducto> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public CategoriaProducto obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    public CategoriaProducto crear(@RequestBody CategoriaProducto c) {
        c.setId(null);
        return service.guardar(c);
    }

    @PutMapping("/{id}")
    public CategoriaProducto actualizar(@PathVariable Long id, @RequestBody CategoriaProducto c) {
        c.setId(id);
        return service.guardar(c);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
