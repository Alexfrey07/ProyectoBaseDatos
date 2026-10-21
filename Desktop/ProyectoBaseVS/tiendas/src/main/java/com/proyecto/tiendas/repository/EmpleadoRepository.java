package com.proyecto.tiendas.repository;

import com.proyecto.tiendas.entity.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
}