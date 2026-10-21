package com.proyecto.tiendas.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "SUCURSAL")
public class Sucursal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_SUCURSAL")
    private Long id;
    @Column(name = "ID_MUNICIPIO")
    private Long idMunicipio;
    @Column(name = "NOMBRE")
    private String nombre;
    @Column(name = "ES_CABECERA")
    private String esCabecera = "N";
    @Column(name = "ESTADO")
    private String estado = "ACTIVO";

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdMunicipio() {
        return idMunicipio;
    }

    public void setIdMunicipio(Long idMunicipio) {
        this.idMunicipio = idMunicipio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEsCabecera() {
        return esCabecera;
    }

    public void setEsCabecera(String esCabecera) {
        this.esCabecera = esCabecera;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}