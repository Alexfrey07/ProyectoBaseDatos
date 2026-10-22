package com.proyecto.tiendas.service;

import com.proyecto.tiendas.dto.NotaCreditoRequest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class NotaCreditoService {
    private final JdbcTemplate jdbc;

    public NotaCreditoService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<Map<String, Object>> listar() {
        return jdbc.queryForList(
            "SELECT N.ID_NOTA_CREDITO AS \"id\", N.SERIE AS \"serie\", N.NUMERO AS \"numero\", " +
            "TO_CHAR(N.FECHA_EMISION, 'DD/MM/YYYY HH24:MI') AS \"fecha\", " +
            "F.SERIE || '-' || F.NUMERO AS \"factura\", " +
            "TRIM(C.NOMBRES || ' ' || C.APELLIDOS) AS \"cliente\", " +
            "N.MONTO_TOTAL AS \"total\", N.MOTIVO AS \"motivo\", " +
            "E.NOMBRES || ' ' || E.APELLIDOS AS \"empleado\" " +
            "FROM NOTA_CREDITO N JOIN FACTURA F ON F.ID_FACTURA = N.ID_FACTURA " +
            "JOIN CLIENTE C ON C.ID_CLIENTE = F.ID_CLIENTE " +
            "JOIN EMPLEADO E ON E.ID_EMPLEADO = N.ID_EMPLEADO " +
            "ORDER BY N.ID_NOTA_CREDITO DESC");
    }

    public List<Map<String, Object>> detalle(Long idNota) {
        return jdbc.queryForList(
            "SELECT P.NOMBRE AS \"producto\", D.CANTIDAD AS \"cantidad\", " +
            "D.PRECIO_UNITARIO AS \"precio\", D.SUBTOTAL AS \"subtotal\", " +
            "D.REINGRESA_INVENTARIO AS \"reingresa\" " +
            "FROM DETALLE_NOTA_CREDITO D " +
            "JOIN DETALLE_FACTURA DF ON DF.ID_DETALLE_FACTURA = D.ID_DETALLE_FACTURA " +
            "JOIN PRODUCTO P ON P.ID_PRODUCTO = DF.ID_PRODUCTO " +
            "WHERE D.ID_NOTA_CREDITO = ?", idNota);
    }

    public List<Map<String, Object>> lineasFactura(Long idFactura) {
        return jdbc.queryForList(
            "SELECT D.ID_DETALLE_FACTURA AS \"idDetalleFactura\", P.NOMBRE AS \"producto\", " +
            "D.CANTIDAD AS \"facturada\", D.PRECIO_UNITARIO AS \"precio\", " +
            "NVL((SELECT SUM(N.CANTIDAD) FROM DETALLE_NOTA_CREDITO N " +
            "WHERE N.ID_DETALLE_FACTURA = D.ID_DETALLE_FACTURA), 0) AS \"devuelta\" " +
            "FROM DETALLE_FACTURA D JOIN PRODUCTO P ON P.ID_PRODUCTO = D.ID_PRODUCTO " +
            "WHERE D.ID_FACTURA = ? ORDER BY D.ID_DETALLE_FACTURA", idFactura);
    }

    @Transactional
    public Map<String, Object> crear(NotaCreditoRequest r, String username) {
        if (r.idFactura() == null) {
            throw new IllegalArgumentException("Debe elegir la factura");
        }
        if (r.motivo() == null || r.motivo().isBlank()) {
            throw new IllegalArgumentException("El motivo es obligatorio");
        }

        List<NotaCreditoRequest.Linea> lineas = new ArrayList<>();
        if (r.lineas() != null) {
            for (NotaCreditoRequest.Linea l : r.lineas()) {
                if (l.cantidad() != null && l.cantidad() > 0) lineas.add(l);
            }
        }
        if (lineas.isEmpty()) {
            throw new IllegalArgumentException("Indique la cantidad a devolver de al menos un producto");
        }

        BigDecimal total = BigDecimal.ZERO;
        List<BigDecimal> precios = new ArrayList<>();
        for (NotaCreditoRequest.Linea l : lineas) {
            Map<String, Object> fila;
            try {
                fila = jdbc.queryForMap(
                    "SELECT D.ID_FACTURA, D.CANTIDAD, D.PRECIO_UNITARIO, " +
                    "NVL((SELECT SUM(N.CANTIDAD) FROM DETALLE_NOTA_CREDITO N " +
                    "WHERE N.ID_DETALLE_FACTURA = D.ID_DETALLE_FACTURA), 0) AS DEVUELTA " +
                    "FROM DETALLE_FACTURA D WHERE D.ID_DETALLE_FACTURA = ?", l.idDetalleFactura());
            } catch (EmptyResultDataAccessException e) {
                throw new IllegalArgumentException("La línea de factura con ID " + l.idDetalleFactura() + " no existe");
            }
            long idFacturaLinea = ((Number) fila.get("ID_FACTURA")).longValue();
            if (idFacturaLinea != r.idFactura()) {
                throw new IllegalArgumentException("Una de las líneas no pertenece a la factura elegida");
            }
            int disponible = ((Number) fila.get("CANTIDAD")).intValue() - ((Number) fila.get("DEVUELTA")).intValue();
            if (l.cantidad() > disponible) {
                throw new IllegalArgumentException("La cantidad a devolver supera lo disponible (" + disponible + ")");
            }
            BigDecimal precio = new BigDecimal(fila.get("PRECIO_UNITARIO").toString());
            precios.add(precio);
            total = total.add(precio.multiply(BigDecimal.valueOf(l.cantidad())));
        }

        final BigDecimal totalFinal = total;
        Long idEmpleado = jdbc.queryForObject(
            "SELECT ID_EMPLEADO FROM USUARIO WHERE USERNAME = ?", Long.class, username);
        Long numero = jdbc.queryForObject(
            "SELECT NVL(MAX(NUMERO), 0) + 1 FROM NOTA_CREDITO WHERE SERIE = 'NC'", Long.class);

        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO NOTA_CREDITO (ID_FACTURA, ID_EMPLEADO, SERIE, NUMERO, MONTO_TOTAL, MOTIVO) " +
                "VALUES (?, ?, 'NC', ?, ?, ?)", new String[] { "ID_NOTA_CREDITO" });
            ps.setLong(1, r.idFactura());
            ps.setLong(2, idEmpleado);
            ps.setLong(3, numero);
            ps.setBigDecimal(4, totalFinal);
            ps.setString(5, r.motivo().trim());
            return ps;
        }, kh);
        Long idNota = kh.getKey().longValue();

        for (int i = 0; i < lineas.size(); i++) {
            NotaCreditoRequest.Linea l = lineas.get(i);
            BigDecimal precio = precios.get(i);
            String reingresa = "S".equals(l.reingresa()) ? "S" : "N";
            jdbc.update(
                "INSERT INTO DETALLE_NOTA_CREDITO (ID_NOTA_CREDITO, ID_DETALLE_FACTURA, CANTIDAD, PRECIO_UNITARIO, SUBTOTAL, REINGRESA_INVENTARIO) " +
                "VALUES (?, ?, ?, ?, ?, ?)",
                idNota, l.idDetalleFactura(), l.cantidad(), precio,
                precio.multiply(BigDecimal.valueOf(l.cantidad())), reingresa);
        }

        return Map.of("id", idNota, "serie", "NC", "numero", numero, "total", totalFinal);
    }
}