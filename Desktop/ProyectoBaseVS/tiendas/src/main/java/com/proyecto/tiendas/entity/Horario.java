package com.proyecto.tiendas.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "HORARIO")
public class Horario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_HORARIO")
    private Long id;
    @Column(name = "NOMBRE_HORARIO")
    private String nombreHorario;
    @Column(name = "HORA_ENTRADA")
    private String horaEntrada;
    @Column(name = "HORA_SALIDA")
    private String horaSalida;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreHorario() {
        return nombreHorario;
    }

    public void setNombreHorario(String nombreHorario) {
        this.nombreHorario = nombreHorario;
    }

    public String getHoraEntrada() {
        return horaEntrada;
    }

    public void setHoraEntrada(String horaEntrada) {
        this.horaEntrada = horaEntrada;
    }

    public String getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(String horaSalida) {
        this.horaSalida = horaSalida;
    }
}