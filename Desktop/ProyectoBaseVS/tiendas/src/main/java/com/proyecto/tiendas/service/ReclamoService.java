package com.proyecto.tiendas.service;

import com.proyecto.tiendas.dto.ReclamoRequest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Types;
import java.util.List;
import java.util.Map;

@Service
public class ReclamoService {
    private final JdbcTemplate jdbc;

    public ReclamoService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<Map<String, Object>> listar() {
        return jdbc.queryForList(
            "SELECT R.ID_RECLAMO AS \"id\", TO_CHAR(R.FECHA_RECLAMO, 'DD/MM/YYYY HH24:MI') AS \"fecha\", " +
            "F.SERIE || '-' || F.NUMERO AS \"factura\", P.NOMBRE AS \"producto\", " +
            "S.NOMBRE AS \"sucursal\", R.ID_SUCURSAL AS \"idSucursal\", " +
            "R.DESCRIPCION_FALLA AS \"descripcion\", R.CANTIDAD_RECLAMADA AS \"reclamada\", " +
            "R.CANTIDAD_APROBADA AS \"aprobada\", R.TIPO_RESOLUCION AS \"resolucion\", " +
            "R.ESTADO AS \"estado\", PR.NOMBRE AS \"reemplazo\" " +
            "FROM RECLAMO_GARANTIA R " +
            "JOIN GARANTIA G ON G.ID_GARANTIA = R.ID_GARANTIA " +
            "JOIN DETALLE_FACTURA D ON D.ID_DETALLE_FACTURA = G.ID_DETALLE_FACTURA " +
            "JOIN FACTURA F ON F.ID_FACTURA = D.ID_FACTURA " +
            "JOIN PRODUCTO P ON P.ID_PRODUCTO = D.ID_PRODUCTO " +
            "JOIN SUCURSAL S ON S.ID_SUCURSAL = R.ID_SUCURSAL " +
            "LEFT JOIN PRODUCTO PR ON PR.ID_PRODUCTO = R.ID_PRODUCTO_REEMPLAZO " +
            "ORDER BY R.ID_RECLAMO DESC");
    }

    public List<Map<String, Object>> garantias() {
        return jdbc.queryForList(
            "SELECT G.ID_GARANTIA AS \"id\", G.SERIE || '-' || G.NUMERO AS \"factura\", " +
            "G.CLIENTE AS \"cliente\", G.PRODUCTO AS \"producto\", " +
            "TO_CHAR(G.FECHA_INICIO, 'DD/MM/YYYY') AS \"inicio\", TO_CHAR(G.FECHA_FIN, 'DD/MM/YYYY') AS \"fin\", " +
            "G.CANTIDAD_CUBIERTA AS \"cubierta\", " +
            "NVL((SELECT SUM(R.CANTIDAD_RECLAMADA) FROM RECLAMO_GARANTIA R " +
            "WHERE R.ID_GARANTIA = G.ID_GARANTIA AND (R.TIPO_RESOLUCION IS NULL OR R.TIPO_RESOLUCION <> 'RECHAZADO')), 0) AS \"reclamada\", " +
            "G.ESTADO_GARANTIA AS \"estado\" " +
            "FROM VW_GARANTIAS G ORDER BY G.ID_GARANTIA DESC");
    }

    @Transactional
    public void crear(ReclamoRequest r, String username) {
        if (r.idGarantia() == null || r.idSucursal() == null) {
            throw new IllegalArgumentException("La garantía y la sucursal son obligatorias");
        }
        if (r.descripcion() == null || r.descripcion().isBlank()) {
            throw new IllegalArgumentException("Debe describir la falla");
        }
        if (r.cantidad() == null || r.cantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad reclamada debe ser mayor a cero");
        }

        Integer cubierta;
        try {
            cubierta = jdbc.queryForObject(
                "SELECT CANTIDAD_CUBIERTA FROM GARANTIA WHERE ID_GARANTIA = ?", Integer.class, r.idGarantia());
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("La garantía no existe");
        }
        Integer reclamada = jdbc.queryForObject(
            "SELECT NVL(SUM(CANTIDAD_RECLAMADA), 0) FROM RECLAMO_GARANTIA " +
            "WHERE ID_GARANTIA = ? AND (TIPO_RESOLUCION IS NULL OR TIPO_RESOLUCION <> 'RECHAZADO')",
            Integer.class, r.idGarantia());
        int quedan = cubierta - reclamada;
        if (r.cantidad() > quedan) {
            throw new IllegalArgumentException("La cantidad supera lo cubierto por la garantía (quedan " + quedan + ")");
        }

        Long idEmpleado = jdbc.queryForObject(
            "SELECT ID_EMPLEADO FROM USUARIO WHERE USERNAME = ?", Long.class, username);
        jdbc.update(
            "INSERT INTO RECLAMO_GARANTIA (ID_GARANTIA, ID_SUCURSAL, ID_EMPLEADO, DESCRIPCION_FALLA, CANTIDAD_RECLAMADA) " +
            "VALUES (?, ?, ?, ?, ?)",
            r.idGarantia(), r.idSucursal(), idEmpleado, r.descripcion().trim(), r.cantidad());
    }

    @Transactional
    public void enProceso(Long id) {
        int n = jdbc.update(
            "UPDATE RECLAMO_GARANTIA SET ESTADO = 'EN_PROCESO' WHERE ID_RECLAMO = ? AND ESTADO = 'RECIBIDO'", id);
        if (n == 0) {
            throw new IllegalArgumentException("Solo los reclamos recibidos pueden pasar a en proceso");
        }
    }

    @Transactional
    public void cerrar(Long id, ReclamoRequest.Cierre c) {
        Map<String, Object> fila;
        try {
            fila = jdbc.queryForMap(
                "SELECT ESTADO, CANTIDAD_RECLAMADA FROM RECLAMO_GARANTIA WHERE ID_RECLAMO = ?", id);
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("El reclamo no existe");
        }
        if ("CERRADO".equals(fila.get("ESTADO"))) {
            throw new IllegalArgumentException("El reclamo ya está cerrado");
        }
        int reclamada = ((Number) fila.get("CANTIDAD_RECLAMADA")).intValue();

        String tipo = c.tipoResolucion();
        if (tipo == null || !List.of("REPARACION", "CAMBIO", "RECHAZADO").contains(tipo)) {
            throw new IllegalArgumentException("Elija la resolución: reparación, cambio o rechazado");
        }
        boolean rechazado = "RECHAZADO".equals(tipo);
        int aprobada = rechazado ? 0 : (c.cantidadAprobada() == null ? 0 : c.cantidadAprobada());
        if (!rechazado && (aprobada <= 0 || aprobada > reclamada)) {
            throw new IllegalArgumentException("La cantidad aprobada debe ser mayor a cero y no superar lo reclamado (" + reclamada + ")");
        }
        if ("CAMBIO".equals(tipo) && c.idProductoReemplazo() == null) {
            throw new IllegalArgumentException("Elija el producto de reemplazo");
        }
        Long reemplazo = "CAMBIO".equals(tipo) ? c.idProductoReemplazo() : null;

        jdbc.update("BEGIN PKG_GARANTIAS.CERRAR_RECLAMO(?, ?, ?, ?); END;",
            new Object[] { id, tipo, aprobada, reemplazo },
            new int[] { Types.NUMERIC, Types.VARCHAR, Types.NUMERIC, Types.NUMERIC });
    }
}