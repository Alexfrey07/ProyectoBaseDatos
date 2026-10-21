package com.proyecto.tiendas.service;

import com.proyecto.tiendas.entity.Horario;
import com.proyecto.tiendas.repository.HorarioRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class HorarioService {
    private final HorarioRepository repo;

    public HorarioService(HorarioRepository repo) {
        this.repo = repo;
    }

    public List<Horario> listar() {
        return repo.findAll();
    }

    public Horario guardar(Horario h) {
        return repo.save(h);
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }
}