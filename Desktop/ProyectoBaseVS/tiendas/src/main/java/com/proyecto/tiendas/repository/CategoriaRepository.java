package com.proyecto.tiendas.repository;



import com.proyecto.tiendas.entity.CategoriaProducto;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CategoriaRepository  extends JpaRepository<CategoriaProducto, Long> {
    
}
