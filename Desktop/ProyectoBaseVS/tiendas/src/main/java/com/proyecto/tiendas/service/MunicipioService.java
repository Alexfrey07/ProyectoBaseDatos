package com.proyecto.tiendas.service;

import com.proyecto.tiendas.entity.Municipio;
import com.proyecto.tiendas.repository.MunicipioRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MunicipioService {
    private final MunicipioRepository repo;

    public MunicipioService(MunicipioRepository repo) {
        this.repo = repo;
    }

    public List<Municipio> listar() {
        return repo.findAll();
    }

    public Municipio guardar(Municipio m) {
        return repo.save(m);
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }
}