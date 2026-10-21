package com.proyecto.tiendas.repository;

import com.proyecto.tiendas.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
}