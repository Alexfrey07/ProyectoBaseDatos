package com.proyecto.tiendas;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class PermisosInterceptor implements HandlerInterceptor {
    private final JdbcTemplate jdbc;
    private final Map<String, String> rutas = new LinkedHashMap<>();
    private final List<String> consultaAbierta = List.of(
            "/api/categorias", "/api/departamentos", "/api/municipios", "/api/sucursales",
            "/api/productos", "/api/proveedores", "/api/clientes", "/api/puestos");

    public PermisosInterceptor(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        rutas.put("/api/categorias", "Categorias");
        rutas.put("/api/departamentos", "Departamentos");
        rutas.put("/api/municipios", "Municipios");
        rutas.put("/api/sucursales", "Sucursales");
        rutas.put("/api/productos", "Productos");
        rutas.put("/api/proveedores", "Proveedores");
        rutas.put("/api/clientes", "Clientes");
        rutas.put("/api/empleados", "Empleados");
        rutas.put("/api/puestos", "Puestos");
        rutas.put("/api/horarios", "Horarios");
        rutas.put("/api/compras", "Compras");
        rutas.put("/api/facturas", "Facturacion");
        rutas.put("/api/notas-credito", "Notas de Credito");
        rutas.put("/api/reclamos", "Reclamos de Garantia");
        rutas.put("/api/inventario", "Inventario");
        rutas.put("/api/usuarios", "Usuarios");
        rutas.put("/api/roles", "Roles y Permisos");
        rutas.put("/api/bitacora", "Bitacora");
        rutas.put("/api/reportes/dashboard", "Dashboard");
        rutas.put("/api/reportes/ventas-sucursal", "Ventas por Sucursal");
        rutas.put("/api/reportes/ventas-producto", "Ventas por Producto");
        rutas.put("/api/reportes/ventas-categoria", "Ventas por Categoria");
        rutas.put("/api/reportes/ventas-empleado", "Ventas por Empleado");
        rutas.put("/api/reportes/garantias", "Garantias y Cambios");
    }

    private boolean coincide(String path, String prefijo) {
        return path.equals(prefijo) || path.startsWith(prefijo + "/");
    }

    private boolean denegar(HttpServletResponse res) throws Exception {
        res.setStatus(403);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"mensaje\":\"No tiene permiso para realizar esta operación\"}");
        return false;
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        String metodo = req.getMethod();
        String path = req.getRequestURI();
        if ("OPTIONS".equals(metodo) || coincide(path, "/api/auth"))
            return true;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String opcion = null;
        String prefijo = null;
        for (Map.Entry<String, String> e : rutas.entrySet()) {
            if (coincide(path, e.getKey())) {
                opcion = e.getValue();
                prefijo = e.getKey();
                break;
            }
        }
        if (auth == null || opcion == null)
            return denegar(res);

        if ("GET".equals(metodo) && consultaAbierta.contains(prefijo))
            return true;

        String columna = switch (metodo) {
            case "GET" -> "PERMISO_LECTURA";
            case "POST" -> "PERMISO_ESCRITURA";
            case "PUT", "PATCH" -> "PERMISO_EDICION";
            case "DELETE" -> "PERMISO_ELIMINACION";
            default -> null;
        };
        if (columna == null)
            return denegar(res);

        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM VW_MENU_USUARIO WHERE USERNAME = ? AND NOMBRE_OPCION = ? AND " + columna
                        + " = 'S'",
                Integer.class, auth.getName(), opcion);
        if (n != null && n > 0)
            return true;
        return denegar(res);
    }
}