package com.proyecto.tiendas.controller;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> validacion(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("mensaje", ex.getMessage()));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, String>> baseDeDatos(DataAccessException ex) {
        String m = ex.getMostSpecificCause().getMessage();
        String mensaje = "Error en la base de datos";
        if (m != null) {
            Matcher x = Pattern.compile("ORA-200\\d\\d: ([^\\n]*)").matcher(m);
            if (x.find())
                mensaje = x.group(1);
            else if (m.contains("ORA-00001"))
                mensaje = "Ya existe un registro con esos datos";
            else if (m.contains("ORA-02292"))
                mensaje = "No se puede eliminar: tiene registros relacionados";
            else if (m.contains("ORA-02290"))
                mensaje = "Un valor no cumple las reglas de la tabla";
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("mensaje", mensaje));
    }
}