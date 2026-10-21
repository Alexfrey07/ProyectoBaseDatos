package com.proyecto.tiendas.service;

import com.proyecto.tiendas.entity.Sucursal;
import com.proyecto.tiendas.repository.SucursalRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SucursalService {
    private final SucursalRepository repo;

    public SucursalService(SucursalRepository repo) {
        this.repo = repo;
    }

    public List<Sucursal> listar() {
        return repo.findAll();
    }

    public Sucursal guardar(Sucursal s) {
        return repo.save(s);
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }
}