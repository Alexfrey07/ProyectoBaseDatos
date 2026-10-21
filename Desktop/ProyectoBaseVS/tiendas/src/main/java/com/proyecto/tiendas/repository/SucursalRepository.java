package com.proyecto.tiendas.repository;

import com.proyecto.tiendas.entity.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SucursalRepository extends JpaRepository<Sucursal, Long> {
}