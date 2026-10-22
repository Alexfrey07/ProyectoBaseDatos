package com.proyecto.tiendas.service;

import com.proyecto.tiendas.dto.ReporteDef;
import com.proyecto.tiendas.dto.ReporteDef.Columna;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReporteService {
    private final JdbcTemplate jdbc;
    private final Map<String, ReporteDef> definiciones = new LinkedHashMap<>();

    public ReporteService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;

        definiciones.put("ventas-sucursal", new ReporteDef("Ventas por sucursal",
                "SELECT SUCURSAL AS \"sucursal\", NUM_FACTURAS AS \"facturas\", TOTAL_VENTAS AS \"total\" " +
                        "FROM VW_VENTAS_SUCURSAL ORDER BY TOTAL_VENTAS DESC",
                List.of(new Columna("sucursal", "Sucursal"), new Columna("facturas", "Facturas"),
                        new Columna("total", "Total vendido"))));

        definiciones.put("ventas-producto", new ReporteDef("Ventas por producto",
                "SELECT CODIGO AS \"codigo\", PRODUCTO AS \"producto\", CATEGORIA AS \"categoria\", " +
                        "UNIDADES_VENDIDAS AS \"unidades\", TOTAL_VENDIDO AS \"total\" " +
                        "FROM VW_VENTAS_PRODUCTO ORDER BY TOTAL_VENDIDO DESC",
                List.of(new Columna("codigo", "Código"), new Columna("producto", "Producto"),
                        new Columna("categoria", "Categoría"), new Columna("unidades", "Unidades"),
                        new Columna("total", "Total vendido"))));

        definiciones.put("ventas-categoria", new ReporteDef("Ventas por categoría",
                "SELECT CATEGORIA AS \"categoria\", UNIDADES_VENDIDAS AS \"unidades\", TOTAL_VENDIDO AS \"total\" " +
                        "FROM VW_VENTAS_CATEGORIA ORDER BY TOTAL_VENDIDO DESC",
                List.of(new Columna("categoria", "Categoría"), new Columna("unidades", "Unidades"),
                        new Columna("total", "Total vendido"))));

        definiciones.put("ventas-empleado", new ReporteDef("Ventas por empleado",
                "SELECT EMPLEADO AS \"empleado\", SUCURSAL AS \"sucursal\", NUM_FACTURAS AS \"facturas\", " +
                        "TOTAL_VENTAS AS \"total\" FROM VW_VENTAS_EMPLEADO ORDER BY TOTAL_VENTAS DESC",
                List.of(new Columna("empleado", "Empleado"), new Columna("sucursal", "Sucursal"),
                        new Columna("facturas", "Facturas"), new Columna("total", "Total vendido"))));

        definiciones.put("garantias", new ReporteDef("Garantías y cambios",
                "SELECT TO_CHAR(FECHA_RECLAMO, 'DD/MM/YYYY') AS \"fecha\", PRODUCTO AS \"producto\", " +
                        "SUCURSAL AS \"sucursal\", CANTIDAD_RECLAMADA AS \"reclamada\", CANTIDAD_APROBADA AS \"aprobada\", "
                        +
                        "NVL(TIPO_RESOLUCION, 'PENDIENTE') AS \"resolucion\", ESTADO AS \"estado\" " +
                        "FROM VW_RECLAMOS_GARANTIA ORDER BY ID_RECLAMO DESC",
                List.of(new Columna("fecha", "Fecha"), new Columna("producto", "Producto"),
                        new Columna("sucursal", "Sucursal"), new Columna("reclamada", "Reclamada"),
                        new Columna("aprobada", "Aprobada"), new Columna("resolucion", "Resolución"),
                        new Columna("estado", "Estado"))));
    }

    public ReporteDef definicion(String tipo) {
        ReporteDef d = definiciones.get(tipo);
        if (d == null)
            throw new IllegalArgumentException("El reporte no existe");
        return d;
    }

    public List<Map<String, Object>> filas(ReporteDef d) {
        return jdbc.queryForList(d.sql());
    }

    public Map<String, Object> datos(String tipo) {
        ReporteDef d = definicion(tipo);
        return Map.of("titulo", d.titulo(), "columnas", d.columnas(), "filas", filas(d));
    }

    public Map<String, Object> dashboard() {
        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("totalVentas", jdbc.queryForObject("SELECT NVL(SUM(TOTAL), 0) FROM FACTURA", Number.class));
        kpis.put("facturas", jdbc.queryForObject("SELECT COUNT(*) FROM FACTURA", Number.class));
        kpis.put("notasCredito",
                jdbc.queryForObject("SELECT NVL(SUM(MONTO_TOTAL), 0) FROM NOTA_CREDITO", Number.class));
        kpis.put("stockBajo",
                jdbc.queryForObject("SELECT COUNT(*) FROM INVENTARIO WHERE EXISTENCIA <= STOCK_MINIMO", Number.class));
        kpis.put("reclamosAbiertos",
                jdbc.queryForObject("SELECT COUNT(*) FROM RECLAMO_GARANTIA WHERE ESTADO <> 'CERRADO'", Number.class));

        return Map.of(
                "kpis", kpis,
                "ventasSucursal", jdbc.queryForList(
                        "SELECT SUCURSAL AS \"etiqueta\", TOTAL_VENTAS AS \"valor\" FROM VW_VENTAS_SUCURSAL " +
                                "WHERE TOTAL_VENTAS > 0 ORDER BY TOTAL_VENTAS DESC FETCH FIRST 10 ROWS ONLY"),
                "ventasCategoria", jdbc.queryForList(
                        "SELECT CATEGORIA AS \"etiqueta\", TOTAL_VENDIDO AS \"valor\" FROM VW_VENTAS_CATEGORIA ORDER BY TOTAL_VENDIDO DESC"),
                "topProductos", jdbc.queryForList(
                        "SELECT PRODUCTO AS \"etiqueta\", UNIDADES_VENDIDAS AS \"valor\" FROM VW_VENTAS_PRODUCTO " +
                                "ORDER BY UNIDADES_VENDIDAS DESC FETCH FIRST 5 ROWS ONLY"),
                "reclamos", jdbc.queryForList(
                        "SELECT NVL(TIPO_RESOLUCION, 'PENDIENTE') AS \"etiqueta\", COUNT(*) AS \"valor\" " +
                                "FROM RECLAMO_GARANTIA GROUP BY NVL(TIPO_RESOLUCION, 'PENDIENTE')"));
    }
}