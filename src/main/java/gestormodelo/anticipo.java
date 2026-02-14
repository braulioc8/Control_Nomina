/*
 * Sistema de Control de Nómina
 * Desarrollado por: Braulio Cajas (GitHub: braulioc8)
 * Licencia: MIT
 */
package gestormodelo;

import java.sql.Date;

/**
 * Modelo para representar un adelanto de sueldo.
 * @author Braulio Cajas
 */
public class anticipo {
    private int idAnticipo;
    private int idTrabajador; // Relación con la tabla trabajadores
    private Date fecha;       // Cuándo se le dio la plata
    private double monto;     // Cuánto se le dio
    private String detalle;   // Para qué (ej: "Almuerzo", "Pasajes")

    // Constructor vacío
    public anticipo() {}

    // Constructor útil para el guardado rápido
    public anticipo(int idTrabajador, Date fecha, double monto, String detalle) {
        this.idTrabajador = idTrabajador;
        this.fecha = fecha;
        this.monto = monto;
        this.detalle = detalle;
    }

    // --- GETTERS Y SETTERS ---
    public int getIdAnticipo() { return idAnticipo; }
    public void setIdAnticipo(int idAnticipo) { this.idAnticipo = idAnticipo; }

    public int getIdTrabajador() { return idTrabajador; }
    public void setIdTrabajador(int idTrabajador) { this.idTrabajador = idTrabajador; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
}