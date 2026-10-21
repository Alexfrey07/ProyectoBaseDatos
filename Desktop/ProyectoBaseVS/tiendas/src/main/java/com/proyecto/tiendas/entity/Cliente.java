package com.proyecto.tiendas.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "CLIENTE")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CLIENTE")
    private Long id;
    @Column(name = "NOMBRES")
    private String nombres;
    @Column(name = "APELLIDOS")
    private String apellidos;
    @Column(name = "NIT")
    private String nit = "CF";
    @Column(name = "DPI")
    private String dpi;
    @Column(name = "ES_CONSUMIDOR_FINAL")
    private String esConsumidorFinal = "N";
    @Column(name = "ESTADO")
    private String estado = "ACTIVO";

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getDpi() {
        return dpi;
    }

    public void setDpi(String dpi) {
        this.dpi = dpi;
    }

    public String getEsConsumidorFinal() {
        return esConsumidorFinal;
    }

    public void setEsConsumidorFinal(String esConsumidorFinal) {
        this.esConsumidorFinal = esConsumidorFinal;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}