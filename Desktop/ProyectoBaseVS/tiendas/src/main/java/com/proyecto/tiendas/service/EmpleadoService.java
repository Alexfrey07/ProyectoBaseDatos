package com.proyecto.tiendas.service;

import com.proyecto.tiendas.entity.Empleado;
import com.proyecto.tiendas.repository.EmpleadoRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EmpleadoService {
    private final EmpleadoRepository repo;

    public EmpleadoService(EmpleadoRepository repo) {
        this.repo = repo;
    }

    public List<Empleado> listar() {
        return repo.findAll();
    }

    public Empleado guardar(Empleado e) {
        return repo.save(e);
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }
}