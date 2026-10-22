package com.proyecto.tiendas.service;

import com.proyecto.tiendas.dto.CompraRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

@Service
public class CompraService {
    private final JdbcTemplate jdbc;

    public CompraService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> listar() {
        return jdbc.queryForList(
                "SELECT C.ID_COMPRA AS \"id\", C.NUMERO_DOCUMENTO AS \"numeroDocumento\", " +
                        "P.RAZON_SOCIAL AS \"proveedor\", S.NOMBRE AS \"sucursal\", " +
                        "E.NOMBRES || ' ' || E.APELLIDOS AS \"empleado\", " +
                        "TO_CHAR(C.FECHA, 'DD/MM/YYYY HH24:MI') AS \"fecha\", " +
                        "C.TOTAL AS \"total\", C.ESTADO AS \"estado\" " +
                        "FROM COMPRA C JOIN PROVEEDOR P ON P.ID_PROVEEDOR = C.ID_PROVEEDOR " +
                        "JOIN SUCURSAL S ON S.ID_SUCURSAL = C.ID_SUCURSAL " +
                        "JOIN EMPLEADO E ON E.ID_EMPLEADO = C.ID_EMPLEADO " +
                        "ORDER BY C.ID_COMPRA DESC");
    }

    public List<Map<String, Object>> detalle(Long idCompra) {
        return jdbc.queryForList(
                "SELECT D.ID_PRODUCTO AS \"idProducto\", P.NOMBRE AS \"producto\", " +
                        "D.CANTIDAD AS \"cantidad\", D.COSTO_UNITARIO AS \"costoUnitario\", D.SUBTOTAL AS \"subtotal\" "
                        +
                        "FROM DETALLE_COMPRA D JOIN PRODUCTO P ON P.ID_PRODUCTO = D.ID_PRODUCTO " +
                        "WHERE D.ID_COMPRA = ?",
                idCompra);
    }

    @Transactional
    public Long crear(CompraRequest r, String username) {
        if (r.idProveedor() == null || r.idSucursal() == null
                || r.numeroDocumento() == null || r.numeroDocumento().isBlank()) {
            throw new IllegalArgumentException("El proveedor, la sucursal y el número de documento son obligatorios");
        }
        if (r.lineas() == null || r.lineas().isEmpty()) {
            throw new IllegalArgumentException("Debe agregar al menos un producto");
        }
        BigDecimal total = BigDecimal.ZERO;
        for (CompraRequest.Linea l : r.lineas()) {
            if (l.cantidad() == null || l.cantidad() <= 0 || l.costoUnitario() == null
                    || l.costoUnitario().signum() < 0) {
                throw new IllegalArgumentException("Cada producto debe tener cantidad mayor a cero y costo válido");
            }
            total = total.add(l.costoUnitario().multiply(BigDecimal.valueOf(l.cantidad())));
        }

        Long idEmpleado = jdbc.queryForObject(
                "SELECT ID_EMPLEADO FROM USUARIO WHERE USERNAME = ?", Long.class, username);
        final BigDecimal totalFinal = total;

        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO COMPRA (ID_PROVEEDOR, ID_SUCURSAL, ID_EMPLEADO, NUMERO_DOCUMENTO, TOTAL, OBSERVACIONES) "
                            +
                            "VALUES (?, ?, ?, ?, ?, ?)",
                    new String[] { "ID_COMPRA" });
            ps.setLong(1, r.idProveedor());
            ps.setLong(2, r.idSucursal());
            ps.setLong(3, idEmpleado);
            ps.setString(4, r.numeroDocumento().trim());
            ps.setBigDecimal(5, totalFinal);
            ps.setString(6, r.observaciones());
            return ps;
        }, kh);
        Long idCompra = kh.getKey().longValue();

        for (CompraRequest.Linea l : r.lineas()) {
            jdbc.update(
                    "INSERT INTO DETALLE_COMPRA (ID_COMPRA, ID_PRODUCTO, CANTIDAD, COSTO_UNITARIO) VALUES (?, ?, ?, ?)",
                    idCompra, l.idProducto(), l.cantidad(), l.costoUnitario());
        }
        return idCompra;
    }

    @Transactional
    public void recibir(Long idCompra) {
        jdbc.update("BEGIN PRC_RECIBIR_COMPRA(?); END;", idCompra);
    }

    @Transactional
    public void cancelar(Long idCompra) {
        int n = jdbc.update("UPDATE COMPRA SET ESTADO = 'CANCELADA' WHERE ID_COMPRA = ? AND ESTADO = 'PENDIENTE'",
                idCompra);
        if (n == 0) {
            throw new IllegalArgumentException("Solo se pueden cancelar compras pendientes");
        }
    }
}