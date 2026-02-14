/*
 * Sistema de Control de Nómina
 * Desarrollado por: Braulio Cajas (GitHub: braulioc8)
 * Licencia: MIT
 */
package gestordatos;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 *
 * @author Braulio Cajas
 */
public class fechas {

    // FORMATOS
    private static final DateTimeFormatter FORMATO_HUMANO = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_SQL = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * MÉTODO 1: Obtener Fecha Actual para mostrar en pantalla
     * 
     * @return String ej: "24/01/2026"
     */
    public static String obtenerFechaActual() {
        LocalDate hoy = LocalDate.now();
        return hoy.format(FORMATO_HUMANO);
    }

    /**
     * MÉTODO 1.5: Obtener Fecha Actual lista para SQL (java.sql.Date)
     * Úsalo cuando quieras guardar "HOY" directo en la BD.
     * 
     * @return java.sql.Date
     */
    public static java.sql.Date obtenerFechaActualSQL() {
        return java.sql.Date.valueOf(LocalDate.now());
    }

    /**
     * MÉTODO 2: Convertir Texto de la Interfaz a Fecha SQL
     * Úsalo cuando leas de un JTextField (txtFecha) y quieras guardar en BD.
     * * @param fechaTexto String en formato "dd/MM/yyyy" (ej: 24/01/2026)
     * 
     * @return java.sql.Date listo para el PreparedStatement
     */
    public static java.sql.Date convertirStringASQL(String fechaTexto) {
        try {
            // 1. Convertimos el texto "24/01/2026" a un objeto LocalDate
            LocalDate fecha = LocalDate.parse(fechaTexto, FORMATO_HUMANO);

            // 2. Lo convertimos a formato SQL
            return java.sql.Date.valueOf(fecha);

        } catch (DateTimeParseException e) {
            System.err.println("Error: La fecha debe ser dd/MM/yyyy. " + e.getMessage());
            return null; // O devuelve la fecha actual por defecto
        }
    }

    /**
     * MÉTODO 3: Convertir Fecha de SQL a Texto para la Interfaz
     * Úsalo cuando traes datos de la BD y quieres mostrarlos en un JTextField o
     * Tabla.
     */
    public static String convertirSQLAString(java.sql.Date fechaSQL) {
        if (fechaSQL == null)
            return "";
        return fechaSQL.toLocalDate().format(FORMATO_HUMANO);
    }

}
