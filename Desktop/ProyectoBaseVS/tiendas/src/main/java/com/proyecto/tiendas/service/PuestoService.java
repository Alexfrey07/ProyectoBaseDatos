package com.proyecto.tiendas.service;

import com.proyecto.tiendas.entity.Puesto;
import com.proyecto.tiendas.repository.PuestoRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PuestoService {
    private final PuestoRepository repo;

    public PuestoService(PuestoRepository repo) {
        this.repo = repo;
    }

    public List<Puesto> listar() {
        return repo.findAll();
    }

    public Puesto guardar(Puesto p) {
        return repo.save(p);
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }
}