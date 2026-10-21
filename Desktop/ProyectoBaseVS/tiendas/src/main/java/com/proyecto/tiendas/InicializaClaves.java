package com.proyecto.tiendas;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class InicializaClaves implements CommandLineRunner {
    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;

    public InicializaClaves(JdbcTemplate jdbc, PasswordEncoder encoder) {
        this.jdbc = jdbc;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        jdbc.update("UPDATE USUARIO SET PASSWORD_HASH = ? WHERE PASSWORD_HASH = 'CAMBIAR_POR_HASH_BCRYPT'",
                encoder.encode("Admin123"));
    }
}