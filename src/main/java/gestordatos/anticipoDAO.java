/*
 * Sistema de Control de Nómina
 * Desarrollado por: Braulio Cajas (GitHub: braulioc8)
 * Licencia: MIT
 */
package gestordatos;

import conexsql.conexion;
import gestormodelo.anticipo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * DAO para gestionar los adelantos de dinero (anticipos) de los trabajadores.
 * 
 * @author Braulio Cajas
 */
public class anticipoDAO {

    /**
     * Guarda un anticipo o lo actualiza si ya existe uno para ese trabajador en esa
     * fecha.
     * Esto permite corregir montos desde la tabla principal sin duplicar registros.
     */
    public boolean guardarOActualizar(anticipo ant) {
        // SQL corregido con la columna 'motivo'
        String sql = """
                IF EXISTS (SELECT 1 FROM anticipos WHERE id_trabajador = ? AND fecha = ?)
                    BEGIN
                        UPDATE anticipos
                        SET monto = ?, motivo = ?
                        WHERE id_trabajador = ? AND fecha = ?
                    END
                ELSE
                    BEGIN
                        INSERT INTO anticipos (id_trabajador, fecha, monto, motivo)
                        VALUES (?, ?, ?, ?)
                    END
                """;

        try (Connection con = conexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {

            // 1. Condición IF EXISTS
            ps.setInt(1, ant.getIdTrabajador());
            ps.setDate(2, ant.getFecha());

            // 2. Parámetros UPDATE
            ps.setDouble(3, ant.getMonto());
            ps.setString(4, ant.getDetalle()); // Aquí usamos el String que tengas en el objeto
            ps.setInt(5, ant.getIdTrabajador());
            ps.setDate(6, ant.getFecha());

            // 3. Parámetros INSERT
            ps.setInt(7, ant.getIdTrabajador());
            ps.setDate(8, ant.getFecha());
            ps.setDouble(9, ant.getMonto());
            ps.setString(10, ant.getDetalle());

            ps.execute();
            return true;

        } catch (SQLException e) {
            System.err.println("Error en anticipoDAO (motivo): " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un anticipo si el monto se pone en 0 (Opcional, para limpieza)
     */
    public boolean eliminarSiEsCero(int idTrabajador, java.sql.Date fecha) {
        String sql = "DELETE FROM anticipos WHERE id_trabajador = ? AND fecha = ? AND monto = 0";
        try (Connection con = conexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idTrabajador);
            ps.setDate(2, fecha);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
}