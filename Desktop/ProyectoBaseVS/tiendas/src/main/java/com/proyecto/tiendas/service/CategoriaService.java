package com.proyecto.tiendas.service;

import com.proyecto.tiendas.entity.CategoriaProducto;
import com.proyecto.tiendas.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoriaService {
    private final CategoriaRepository repo;

    public CategoriaService(CategoriaRepository repo) {
        this.repo = repo;
    }

    public List<CategoriaProducto> listar() {
        return repo.findAll();
    }

    public CategoriaProducto obtener(Long id) {
        return repo.findById(id).orElseThrow();
    }

    public CategoriaProducto guardar(CategoriaProducto c) {
        return repo.save(c);
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }
}
