package com.proyecto.tiendas.repository;

import com.proyecto.tiendas.entity.Puesto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PuestoRepository extends JpaRepository<Puesto, Long> {
}