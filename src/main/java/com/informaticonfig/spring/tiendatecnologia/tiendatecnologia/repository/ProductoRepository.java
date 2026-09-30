package com.informaticonfig.spring.tiendatecnologia.tiendatecnologia.repository;

import com.informaticonfig.spring.tiendatecnologia.tiendatecnologia.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
