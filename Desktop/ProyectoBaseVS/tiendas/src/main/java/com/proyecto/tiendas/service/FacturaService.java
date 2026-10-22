package com.proyecto.tiendas.service;

import com.proyecto.tiendas.dto.FacturaRequest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FacturaService {
    private final JdbcTemplate jdbc;

    public FacturaService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> listar() {
        return jdbc.queryForList(
                "SELECT F.ID_FACTURA AS \"id\", F.SERIE AS \"serie\", F.NUMERO AS \"numero\", " +
                        "TO_CHAR(F.FECHA, 'DD/MM/YYYY HH24:MI') AS \"fecha\", " +
                        "TRIM(C.NOMBRES || ' ' || C.APELLIDOS) AS \"cliente\", S.NOMBRE AS \"sucursal\", " +
                        "E.NOMBRES || ' ' || E.APELLIDOS AS \"empleado\", F.TOTAL AS \"total\" " +
                        "FROM FACTURA F JOIN CLIENTE C ON C.ID_CLIENTE = F.ID_CLIENTE " +
                        "JOIN SUCURSAL S ON S.ID_SUCURSAL = F.ID_SUCURSAL " +
                        "JOIN EMPLEADO E ON E.ID_EMPLEADO = F.ID_EMPLEADO " +
                        "ORDER BY F.ID_FACTURA DESC");
    }

    public Map<String, Object> detalle(Long idFactura) {
        List<Map<String, Object>> lineas = jdbc.queryForList(
                "SELECT P.NOMBRE AS \"producto\", D.CANTIDAD AS \"cantidad\", " +
                        "D.PRECIO_UNITARIO AS \"precio\", D.SUBTOTAL AS \"subtotal\" " +
                        "FROM DETALLE_FACTURA D JOIN PRODUCTO P ON P.ID_PRODUCTO = D.ID_PRODUCTO " +
                        "WHERE D.ID_FACTURA = ?",
                idFactura);
        List<Map<String, Object>> pagos = jdbc.queryForList(
                "SELECT T.NOMBRE AS \"tipoPago\", P.MONTO AS \"monto\", P.REFERENCIA AS \"referencia\" " +
                        "FROM PAGO P JOIN TIPO_PAGO T ON T.ID_TIPO_PAGO = P.ID_TIPO_PAGO " +
                        "WHERE P.ID_FACTURA = ?",
                idFactura);
        return Map.of("lineas", lineas, "pagos", pagos);
    }

    public List<Map<String, Object>> productosDisponibles(Long idSucursal) {
        return jdbc.queryForList(
                "SELECT P.ID_PRODUCTO AS \"id\", P.CODIGO AS \"codigo\", P.NOMBRE AS \"nombre\", " +
                        "P.PRECIO_VENTA AS \"precio\", I.EXISTENCIA AS \"existencia\" " +
                        "FROM INVENTARIO I JOIN PRODUCTO P ON P.ID_PRODUCTO = I.ID_PRODUCTO " +
                        "WHERE I.ID_SUCURSAL = ? AND I.EXISTENCIA > 0 AND P.ESTADO = 'ACTIVO' " +
                        "ORDER BY P.NOMBRE",
                idSucursal);
    }

    public List<Map<String, Object>> tiposPago() {
        return jdbc.queryForList(
                "SELECT ID_TIPO_PAGO AS \"id\", NOMBRE AS \"nombre\" FROM TIPO_PAGO ORDER BY ID_TIPO_PAGO");
    }

    @Transactional
    public Map<String, Object> crear(FacturaRequest r, String username) {
        if (r.idCliente() == null || r.idSucursal() == null) {
            throw new IllegalArgumentException("El cliente y la sucursal son obligatorios");
        }
        if (r.lineas() == null || r.lineas().isEmpty()) {
            throw new IllegalArgumentException("Debe agregar al menos un producto");
        }
        if (r.pagos() == null || r.pagos().isEmpty()) {
            throw new IllegalArgumentException("Debe registrar al menos un pago");
        }

        BigDecimal total = BigDecimal.ZERO;
        Map<Long, BigDecimal> precios = new HashMap<>();
        for (FacturaRequest.Linea l : r.lineas()) {
            if (l.idProducto() == null || l.cantidad() == null || l.cantidad() <= 0) {
                throw new IllegalArgumentException("Cada producto debe tener una cantidad mayor a cero");
            }
            BigDecimal precio;
            try {
                precio = jdbc.queryForObject(
                        "SELECT PRECIO_VENTA FROM PRODUCTO WHERE ID_PRODUCTO = ?", BigDecimal.class, l.idProducto());
            } catch (EmptyResultDataAccessException e) {
                throw new IllegalArgumentException("El producto con ID " + l.idProducto() + " no existe");
            }
            precios.put(l.idProducto(), precio);
            total = total.add(precio.multiply(BigDecimal.valueOf(l.cantidad())));
        }
        total = total.setScale(2, RoundingMode.HALF_UP);

        BigDecimal pagado = BigDecimal.ZERO;
        for (FacturaRequest.Pago p : r.pagos()) {
            if (p.idTipoPago() == null || p.monto() == null || p.monto().signum() <= 0) {
                throw new IllegalArgumentException("Cada pago debe tener tipo y un monto mayor a cero");
            }
            pagado = pagado.add(p.monto());
        }
        if (pagado.setScale(2, RoundingMode.HALF_UP).compareTo(total) != 0) {
            throw new IllegalArgumentException(
                    "El total de los pagos (Q" + pagado + ") no coincide con el total de la factura (Q" + total + ")");
        }

        Long idEmpleado = jdbc.queryForObject(
                "SELECT ID_EMPLEADO FROM USUARIO WHERE USERNAME = ?", Long.class, username);
        BigDecimal iva = jdbc.queryForObject("SELECT FN_CALCULA_IVA(?) FROM DUAL", BigDecimal.class, total);
        BigDecimal subtotal = total.subtract(iva);
        Long numero = jdbc.queryForObject(
                "SELECT NVL(MAX(NUMERO), 0) + 1 FROM FACTURA WHERE SERIE = 'A'", Long.class);
        final BigDecimal totalFinal = total;
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO FACTURA (ID_CLIENTE, ID_SUCURSAL, ID_EMPLEADO, SERIE, NUMERO, SUBTOTAL, IVA, TOTAL) " +
                            "VALUES (?, ?, ?, 'A', ?, ?, ?, ?)",
                    new String[] { "ID_FACTURA" });
            ps.setLong(1, r.idCliente());
            ps.setLong(2, r.idSucursal());
            ps.setLong(3, idEmpleado);
            ps.setLong(4, numero);
            ps.setBigDecimal(5, subtotal);
            ps.setBigDecimal(6, iva);
            ps.setBigDecimal(7, totalFinal);
            return ps;
        }, kh);
        Long idFactura = kh.getKey().longValue();

        for (FacturaRequest.Linea l : r.lineas()) {
            BigDecimal precio = precios.get(l.idProducto());
            jdbc.update(
                    "INSERT INTO DETALLE_FACTURA (ID_FACTURA, ID_PRODUCTO, CANTIDAD, PRECIO_UNITARIO, SUBTOTAL) VALUES (?, ?, ?, ?, ?)",
                    idFactura, l.idProducto(), l.cantidad(), precio, precio.multiply(BigDecimal.valueOf(l.cantidad())));
        }

        for (FacturaRequest.Pago p : r.pagos()) {
            jdbc.update(
                    "INSERT INTO PAGO (ID_FACTURA, ID_TIPO_PAGO, MONTO, REFERENCIA) VALUES (?, ?, ?, ?)",
                    idFactura, p.idTipoPago(), p.monto(), p.referencia());
        }

        return Map.of("id", idFactura, "serie", "A", "numero", numero, "total", total);
    }
}