package com.proyecto.tiendas.service;

import com.proyecto.tiendas.entity.Departamento;
import com.proyecto.tiendas.repository.DepartamentoRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DepartamentoService {
    private final DepartamentoRepository repo;

    public DepartamentoService(DepartamentoRepository repo) { this.repo = repo; }

    public List<Departamento> listar() { return repo.findAll(); }
    public Departamento guardar(Departamento d) { return repo.save(d); }
    public void eliminar(Long id) { repo.deleteById(id); }
}