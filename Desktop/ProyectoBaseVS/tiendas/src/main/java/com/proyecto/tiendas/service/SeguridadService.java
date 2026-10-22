package com.proyecto.tiendas.service;

import com.proyecto.tiendas.dto.SeguridadRequest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SeguridadService {
    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;

    public SeguridadService(JdbcTemplate jdbc, PasswordEncoder encoder) {
        this.jdbc = jdbc;
        this.encoder = encoder;
    }

    // ---------------- USUARIOS ----------------

    public List<Map<String, Object>> usuarios() {
        return jdbc.queryForList(
                "SELECT U.ID_USUARIO AS \"id\", U.USERNAME AS \"username\", " +
                        "E.NOMBRES || ' ' || E.APELLIDOS AS \"empleado\", U.ID_ROL AS \"idRol\", " +
                        "R.NOMBRE AS \"rol\", U.ESTADO AS \"estado\" " +
                        "FROM USUARIO U JOIN EMPLEADO E ON E.ID_EMPLEADO = U.ID_EMPLEADO " +
                        "JOIN ROL R ON R.ID_ROL = U.ID_ROL ORDER BY U.ID_USUARIO");
    }

    public List<Map<String, Object>> empleadosLibres() {
        return jdbc.queryForList(
                "SELECT E.ID_EMPLEADO AS \"id\", E.NOMBRES || ' ' || E.APELLIDOS AS \"nombre\" " +
                        "FROM EMPLEADO E WHERE E.ESTADO = 'ACTIVO' " +
                        "AND NOT EXISTS (SELECT 1 FROM USUARIO U WHERE U.ID_EMPLEADO = E.ID_EMPLEADO) " +
                        "ORDER BY E.NOMBRES");
    }

    public List<Map<String, Object>> rolesOpciones() {
        return jdbc.queryForList("SELECT ID_ROL AS \"id\", NOMBRE AS \"nombre\" FROM ROL ORDER BY NOMBRE");
    }

    private void validarClave(String clave) {
        if (clave == null || clave.length() < 8) {
            throw new IllegalArgumentException("La clave debe tener al menos 8 caracteres");
        }
    }

    @Transactional
    public void crearUsuario(SeguridadRequest.UsuarioNuevo n) {
        if (n.idEmpleado() == null || n.idRol() == null) {
            throw new IllegalArgumentException("El empleado y el rol son obligatorios");
        }
        if (n.username() == null || !n.username().trim().matches("[A-Za-z0-9._-]{3,50}")) {
            throw new IllegalArgumentException(
                    "El usuario debe tener de 3 a 50 caracteres (letras, números, punto o guion)");
        }
        validarClave(n.password());
        jdbc.update("INSERT INTO USUARIO (ID_EMPLEADO, ID_ROL, USERNAME, PASSWORD_HASH) VALUES (?, ?, ?, ?)",
                n.idEmpleado(), n.idRol(), n.username().trim(), encoder.encode(n.password()));
    }

    @Transactional
    public void actualizarUsuario(Long id, SeguridadRequest.UsuarioCambio c, String usuarioActual) {
        if (c.idRol() == null || c.estado() == null || !List.of("ACTIVO", "INACTIVO").contains(c.estado())) {
            throw new IllegalArgumentException("El rol y el estado son obligatorios");
        }
        Map<String, Object> fila;
        try {
            fila = jdbc.queryForMap("SELECT USERNAME, ID_ROL FROM USUARIO WHERE ID_USUARIO = ?", id);
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("El usuario no existe");
        }
        if (usuarioActual.equals(fila.get("USERNAME"))) {
            long rolActual = ((Number) fila.get("ID_ROL")).longValue();
            if ("INACTIVO".equals(c.estado())) {
                throw new IllegalArgumentException("No puede desactivar su propio usuario");
            }
            if (rolActual != c.idRol()) {
                throw new IllegalArgumentException("No puede cambiar su propio rol");
            }
        }
        jdbc.update("UPDATE USUARIO SET ID_ROL = ?, ESTADO = ? WHERE ID_USUARIO = ?", c.idRol(), c.estado(), id);
    }

    @Transactional
    public void cambiarClave(Long id, SeguridadRequest.Clave c) {
        validarClave(c.password());
        int n = jdbc.update("UPDATE USUARIO SET PASSWORD_HASH = ?, INTENTOS_FALLIDOS = 0 WHERE ID_USUARIO = ?",
                encoder.encode(c.password()), id);
        if (n == 0)
            throw new IllegalArgumentException("El usuario no existe");
    }

    // ---------------- ROLES Y PERMISOS ----------------

    public List<Map<String, Object>> roles() {
        return jdbc.queryForList(
                "SELECT R.ID_ROL AS \"id\", R.NOMBRE AS \"nombre\", R.DESCRIPCION AS \"descripcion\", " +
                        "(SELECT COUNT(*) FROM USUARIO U WHERE U.ID_ROL = R.ID_ROL) AS \"usuarios\" " +
                        "FROM ROL R ORDER BY R.ID_ROL");
    }

    @Transactional
    public void crearRol(SeguridadRequest.RolNuevo r) {
        if (r.nombre() == null || r.nombre().isBlank() || r.nombre().trim().length() > 50) {
            throw new IllegalArgumentException("El nombre del rol es obligatorio (máximo 50 caracteres)");
        }
        jdbc.update("INSERT INTO ROL (NOMBRE, DESCRIPCION) VALUES (?, ?)",
                r.nombre().trim().toUpperCase(), r.descripcion());
    }

    private String nombreRol(Long idRol) {
        try {
            return jdbc.queryForObject("SELECT NOMBRE FROM ROL WHERE ID_ROL = ?", String.class, idRol);
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("El rol no existe");
        }
    }

    @Transactional
    public void eliminarRol(Long idRol) {
        String nombre = nombreRol(idRol);
        if ("ADMINISTRADOR".equals(nombre)) {
            throw new IllegalArgumentException("El rol ADMINISTRADOR no se puede eliminar");
        }
        Integer usuarios = jdbc.queryForObject("SELECT COUNT(*) FROM USUARIO WHERE ID_ROL = ?", Integer.class, idRol);
        if (usuarios != null && usuarios > 0) {
            throw new IllegalArgumentException("No se puede eliminar: hay " + usuarios + " usuario(s) con este rol");
        }
        jdbc.update("DELETE FROM ROL_MENU WHERE ID_ROL = ?", idRol);
        jdbc.update("DELETE FROM ROL WHERE ID_ROL = ?", idRol);
    }

    public List<Map<String, Object>> permisos(Long idRol) {
        return jdbc.queryForList(
                "SELECT M.ID_MENU AS \"idMenu\", P.NOMBRE_OPCION AS \"grupo\", M.NOMBRE_OPCION AS \"opcion\", " +
                        "NVL(RM.PERMISO_LECTURA, 'N') AS \"lectura\", NVL(RM.PERMISO_ESCRITURA, 'N') AS \"escritura\", "
                        +
                        "NVL(RM.PERMISO_EDICION, 'N') AS \"edicion\", NVL(RM.PERMISO_ELIMINACION, 'N') AS \"eliminacion\" "
                        +
                        "FROM MENU M JOIN MENU P ON P.ID_MENU = M.ID_MENU_PADRE " +
                        "LEFT JOIN ROL_MENU RM ON RM.ID_MENU = M.ID_MENU AND RM.ID_ROL = ? " +
                        "ORDER BY P.ORDEN, M.ORDEN",
                idRol);
    }

    @Transactional
    public void guardarPermisos(Long idRol, List<SeguridadRequest.Permiso> items) {
        String nombre = nombreRol(idRol);
        if ("ADMINISTRADOR".equals(nombre)) {
            throw new IllegalArgumentException("Los permisos del administrador no se pueden modificar");
        }
        jdbc.update("DELETE FROM ROL_MENU WHERE ID_ROL = ?", idRol);

        Set<Long> padres = new LinkedHashSet<>();
        for (SeguridadRequest.Permiso p : new ArrayList<>(items == null ? List.of() : items)) {
            boolean e = "S".equals(p.escritura());
            boolean ed = "S".equals(p.edicion());
            boolean el = "S".equals(p.eliminacion());
            boolean l = "S".equals(p.lectura()) || e || ed || el;
            if (!l)
                continue;

            Long padre;
            try {
                padre = jdbc.queryForObject("SELECT ID_MENU_PADRE FROM MENU WHERE ID_MENU = ?", Long.class, p.idMenu());
            } catch (EmptyResultDataAccessException ex) {
                throw new IllegalArgumentException("Una de las opciones de menú no existe");
            }
            if (padre == null) {
                throw new IllegalArgumentException("Solo se pueden asignar pantallas, no grupos del menú");
            }
            padres.add(padre);
            jdbc.update(
                    "INSERT INTO ROL_MENU (ID_ROL, ID_MENU, PERMISO_LECTURA, PERMISO_ESCRITURA, PERMISO_EDICION, PERMISO_ELIMINACION) "
                            +
                            "VALUES (?, ?, 'S', ?, ?, ?)",
                    idRol, p.idMenu(), e ? "S" : "N", ed ? "S" : "N", el ? "S" : "N");
        }
        for (Long padre : padres) {
            jdbc.update(
                    "INSERT INTO ROL_MENU (ID_ROL, ID_MENU, PERMISO_LECTURA, PERMISO_ESCRITURA, PERMISO_EDICION, PERMISO_ELIMINACION) "
                            +
                            "VALUES (?, ?, 'S', 'N', 'N', 'N')",
                    idRol, padre);
        }
    }

    // ---------------- BITACORA ----------------

    public List<Map<String, Object>> bitacora(String tabla, String tipo) {
        StringBuilder sql = new StringBuilder(
                "SELECT IDBITACORA AS \"id\", TO_CHAR(FECHA_REGISTRO, 'DD/MM/YYYY HH24:MI:SS') AS \"fecha\", " +
                        "TABLA AS \"tabla\", CAMPO AS \"campo\", LLAVE AS \"llave\", TIPO_MOVIMIENTO AS \"tipo\", " +
                        "VALOR_ANTERIOR AS \"anterior\", VALOR_NUEVO AS \"nuevo\", CREADO_POR AS \"usuario\", " +
                        "IP AS \"ip\", NOMBRE_EQUIPO AS \"equipo\" FROM BITACORA WHERE 1 = 1");
        List<Object> args = new ArrayList<>();
        if (tabla != null && !tabla.isBlank()) {
            sql.append(" AND TABLA = ?");
            args.add(tabla);
        }
        if (tipo != null && !tipo.isBlank()) {
            sql.append(" AND TIPO_MOVIMIENTO = ?");
            args.add(tipo);
        }
        sql.append(" ORDER BY IDBITACORA DESC FETCH FIRST 300 ROWS ONLY");
        return jdbc.queryForList(sql.toString(), args.toArray());
    }

    public List<String> tablasBitacora() {
        return jdbc.queryForList("SELECT DISTINCT TABLA FROM BITACORA ORDER BY TABLA", String.class);
    }
}