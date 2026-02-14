/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gestormodelo;

import java.sql.Date;

/**
 *
 * @author braulioo
 */
public class asistencia {
    private int id;
    private int idTrabajador;
    private Date fecha;
    private String estado;
    private int horasExtras;
    private String observacion;
    private String area;
    private Double anticipo;

    public Double getAnticipo() {
        return anticipo;
    }

    public void setAnticipo(Double anticipo) {
        this.anticipo = anticipo;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    // Campos Auxiliares (Solo para mostrar en la tabla, vienen del JOIN)
    private String nombreTrabajador;
    private String nombreCargo;

    // Constructor Vacío
    public asistencia() {
    }

    // Constructor Completo
    public asistencia(int idTrabajador, Date fecha, String estado, int horasExtras, String observacion) {
        this.idTrabajador = idTrabajador;
        this.fecha = fecha;
        this.estado = estado;
        this.horasExtras = horasExtras;
        this.observacion = observacion;
    }

    // --- GETTERS Y SETTERS ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdTrabajador() { return idTrabajador; }
    public void setIdTrabajador(int idTrabajador) { this.idTrabajador = idTrabajador; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public int getHorasExtras() { return horasExtras; }
    public void setHorasExtras(int horasExtras) { this.horasExtras = horasExtras; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }

    // Auxiliares
    public String getNombreTrabajador() { return nombreTrabajador; }
    public void setNombreTrabajador(String nombreTrabajador) { this.nombreTrabajador = nombreTrabajador; }

    public String getNombreCargo() { return nombreCargo; }
    public void setNombreCargo(String nombreCargo) { this.nombreCargo = nombreCargo; }
}
