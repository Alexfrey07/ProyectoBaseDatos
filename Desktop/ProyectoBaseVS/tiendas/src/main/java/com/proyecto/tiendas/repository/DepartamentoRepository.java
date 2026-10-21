package com.proyecto.tiendas.repository;

import com.proyecto.tiendas.entity.Departamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {
}