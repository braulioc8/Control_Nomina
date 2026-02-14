/*
 * Sistema de Control de Nómina
 * Desarrollado por: Braulio Cajas (GitHub: braulioc8)
 * Licencia: MIT
 */
package gestordatos;

import conexsql.conexion;
import gestormodelo.asistencia;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Braulio Cajas
 */
public class asistenciaDAO {

    public boolean guardarOActualizar(asistencia a) {
        String sql = """
                IF EXISTS (SELECT 1 FROM asistencia WHERE id_trabajador = ? AND fecha = ?)
                     BEGIN
                         UPDATE asistencia
                         SET estado = ?, horas_extras = ?, observacion = ?
                         WHERE id_trabajador = ? AND fecha = ?
                     END
                     ELSE
                     BEGIN
                         INSERT INTO asistencia (id_trabajador, fecha, estado, horas_extras, observacion)
                         VALUES (?, ?, ?, ?, ?)
                     END
                 """;

        try (Connection con = conexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, a.getIdTrabajador());
            ps.setDate(2, a.getFecha());

            ps.setString(3, a.getEstado());
            ps.setInt(4, a.getHorasExtras());
            ps.setString(5, a.getObservacion());
            ps.setInt(6, a.getIdTrabajador());
            ps.setDate(7, a.getFecha());

            ps.setInt(8, a.getIdTrabajador());
            ps.setDate(9, a.getFecha());
            ps.setString(10, a.getEstado());
            ps.setInt(11, a.getHorasExtras());
            ps.setString(12, a.getObservacion());

            ps.execute();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al guardar asistencia: " + e.getMessage());
            return false;
        }
    }

    public List<asistencia> listarPorFecha(Date fecha) {
        List<asistencia> lista = new ArrayList<>();
        String sql = """
                SELECT a.id_asistencia, a.id_trabajador, a.fecha, a.estado, a.horas_extras, a.observacion,
                       t.nombre, c.nombre_cargo, c.area,
                       ISNULL(ant.monto, 0) AS monto_anticipo
                FROM asistencia a
                INNER JOIN trabajadores t ON a.id_trabajador = t.id_trabajador
                INNER JOIN cargos c ON t.id_cargo = c.id_cargo
                LEFT JOIN anticipos ant ON a.id_trabajador = ant.id_trabajador AND a.fecha = ant.fecha
                WHERE a.fecha = ?
                """;

        try (Connection con = conexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, fecha);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                asistencia a = new asistencia();
                a.setId(rs.getInt("id_asistencia"));
                a.setIdTrabajador(rs.getInt("id_trabajador"));
                a.setFecha(rs.getDate("fecha"));
                a.setEstado(rs.getString("estado"));
                a.setHorasExtras(rs.getInt("horas_extras"));
                a.setObservacion(rs.getString("observacion"));
                a.setArea(rs.getString("area"));

                a.setNombreTrabajador(rs.getString("nombre"));
                a.setNombreCargo(rs.getString("nombre_cargo"));

                a.setAnticipo(rs.getDouble("monto_anticipo"));

                lista.add(a);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar asistencia con anticipos: " + e.getMessage());
        }
        return lista;
    }

    public boolean existeAsistenciaDeFecha(Date fecha) {
        String sql = "SELECT COUNT(*) FROM asistencia WHERE fecha = ?";
        try (Connection con = conexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, fecha);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error verificando existencia: " + e.getMessage());
        }
        return false;
    }
}
