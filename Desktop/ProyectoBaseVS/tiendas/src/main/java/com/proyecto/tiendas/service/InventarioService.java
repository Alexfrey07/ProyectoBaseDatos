package com.proyecto.tiendas.service;

import com.proyecto.tiendas.dto.InventarioRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

@Service
public class InventarioService {
    private final JdbcTemplate jdbc;

    public InventarioService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> listar(Long idSucursal) {
        String base = "SELECT I.ID_INVENTARIO AS \"id\", I.ID_SUCURSAL AS \"idSucursal\", S.NOMBRE AS \"sucursal\", " +
                "I.ID_PRODUCTO AS \"idProducto\", P.CODIGO AS \"codigo\", P.NOMBRE AS \"producto\", " +
                "I.EXISTENCIA AS \"existencia\", I.STOCK_MINIMO AS \"stockMinimo\", " +
                "CASE WHEN I.EXISTENCIA <= I.STOCK_MINIMO THEN 'BAJO' ELSE 'NORMAL' END AS \"estadoStock\" " +
                "FROM INVENTARIO I JOIN SUCURSAL S ON S.ID_SUCURSAL = I.ID_SUCURSAL " +
                "JOIN PRODUCTO P ON P.ID_PRODUCTO = I.ID_PRODUCTO ";
        if (idSucursal == null) {
            return jdbc.queryForList(base + "ORDER BY S.NOMBRE, P.NOMBRE");
        }
        return jdbc.queryForList(base + "WHERE I.ID_SUCURSAL = ? ORDER BY P.NOMBRE", idSucursal);
    }

    public List<Map<String, Object>> kardex(Long idSucursal) {
        String base = "SELECT M.ID_MOVIMIENTO AS \"id\", TO_CHAR(M.FECHA_MOVIMIENTO, 'DD/MM/YYYY HH24:MI') AS \"fecha\", "
                +
                "S.NOMBRE AS \"sucursal\", P.NOMBRE AS \"producto\", M.TIPO_MOVIMIENTO AS \"tipo\", " +
                "M.CANTIDAD AS \"cantidad\", M.TIPO_ORIGEN AS \"origen\", M.ID_ORIGEN AS \"idOrigen\", " +
                "M.JUSTIFICACION AS \"justificacion\", M.CREADO_POR AS \"usuario\" " +
                "FROM MOVIMIENTO_INVENTARIO M JOIN SUCURSAL S ON S.ID_SUCURSAL = M.ID_SUCURSAL " +
                "JOIN PRODUCTO P ON P.ID_PRODUCTO = M.ID_PRODUCTO ";
        if (idSucursal == null) {
            return jdbc.queryForList(base + "ORDER BY M.ID_MOVIMIENTO DESC FETCH FIRST 200 ROWS ONLY");
        }
        return jdbc.queryForList(
                base + "WHERE M.ID_SUCURSAL = ? ORDER BY M.ID_MOVIMIENTO DESC FETCH FIRST 200 ROWS ONLY", idSucursal);
    }

    @Transactional
    public void ajustar(InventarioRequest.Ajuste a) {
        if (a.idSucursal() == null || a.idProducto() == null) {
            throw new IllegalArgumentException("La sucursal y el producto son obligatorios");
        }
        if (a.nuevaExistencia() == null || a.nuevaExistencia() < 0) {
            throw new IllegalArgumentException("La nueva existencia no puede ser negativa");
        }
        if (a.justificacion() == null || a.justificacion().isBlank()) {
            throw new IllegalArgumentException("La justificación del ajuste es obligatoria");
        }
        jdbc.update("BEGIN PRC_AJUSTE_INVENTARIO(?, ?, ?, ?); END;",
                a.idSucursal(), a.idProducto(), a.nuevaExistencia(), a.justificacion().trim());
    }

    @Transactional
    public void cambiarMinimo(Long id, InventarioRequest.Minimo m) {
        if (m.stockMinimo() == null || m.stockMinimo() < 0) {
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo");
        }
        int n = jdbc.update("UPDATE INVENTARIO SET STOCK_MINIMO = ? WHERE ID_INVENTARIO = ?", m.stockMinimo(), id);
        if (n == 0) {
            throw new IllegalArgumentException("El registro de inventario no existe");
        }
    }
}