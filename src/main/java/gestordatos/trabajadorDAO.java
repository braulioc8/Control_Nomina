package gestordatos;

import gestormodelo.trabajador;
import conexsql.conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class trabajadorDAO {

    public boolean registrar(trabajador t) {
        String sql = "INSERT INTO trabajadores (nombre, cedula, telefono, id_cargo, activo) VALUES (?, ?, ?, ?, 1)";

        try (Connection con = conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, t.getNombre());
            ps.setString(2, t.getCedula());
            ps.setString(3, t.getTelefono());
            ps.setInt(4, t.getIdCargo());

            int resultado = ps.executeUpdate();
            return resultado > 0;
        } catch (SQLException e) {
            System.err.println("Error al registrar trabajador: " + e.getMessage());
            return false;
        }
    }

    public List<trabajador> listarActivos() {
        List<trabajador> lista = new ArrayList<>();
        String sql = """
                SELECT t.id_trabajador, t.nombre, t.cedula, t.telefono, t.id_cargo,
                       c.nombre_cargo, c.sueldo_diario, c.area
                FROM trabajadores t
                INNER JOIN cargos c ON t.id_cargo = c.id_cargo
                WHERE t.activo = 1
                ORDER BY t.nombre ASC
                """;

        try (Connection con = conexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                trabajador t = new trabajador();
                t.setId(rs.getInt("id_trabajador"));
                t.setNombre(rs.getString("nombre"));
                t.setCedula(rs.getString("cedula"));
                t.setTelefono(rs.getString("telefono"));
                t.setIdCargo(rs.getInt("id_cargo"));
                t.setActivo(true);

                t.setCargoNombre(rs.getString("nombre_cargo"));
                t.setSalarioDiario(rs.getDouble("sueldo_diario"));
                t.setArea(rs.getString("area"));

                lista.add(t);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar activos en DAO: " + e.getMessage());
        }
        return lista;
    }

    public List<trabajador> listarTodos() {
        List<trabajador> lista = new ArrayList<>();
        String sql = """
                SELECT t.id_trabajador, t.nombre, t.cedula, t.telefono, t.activo, t.id_cargo,
                       c.nombre_cargo, c.sueldo_diario, c.area
                FROM trabajadores t
                INNER JOIN cargos c ON t.id_cargo = c.id_cargo
                ORDER BY t.activo DESC, t.nombre ASC
                """;

        try (Connection con = conexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                trabajador t = new trabajador();
                t.setId(rs.getInt("id_trabajador"));
                t.setNombre(rs.getString("nombre"));
                t.setCedula(rs.getString("cedula"));
                t.setTelefono(rs.getString("telefono"));
                t.setActivo(rs.getBoolean("activo"));
                t.setIdCargo(rs.getInt("id_cargo"));

                t.setCargoNombre(rs.getString("nombre_cargo"));
                t.setSalarioDiario(rs.getDouble("sueldo_diario"));
                t.setArea(rs.getString("area"));

                lista.add(t);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar todos en DAO: " + e.getMessage());
        }
        return lista;
    }

    public List<Object[]> obtenerReporteNomina(java.sql.Date fechaInicio, java.sql.Date fechaFin, String areaFiltro) {
        List<Object[]> lista = new ArrayList<>();

        String sql = """
                SELECT
                    t.id_trabajador, t.nombre, c.nombre_cargo, c.sueldo_diario,
                    (SELECT COUNT(*) FROM asistencia a WHERE a.id_trabajador = t.id_trabajador AND a.fecha BETWEEN ? AND ? AND a.estado = 'ASISTIO') AS dias_asistidos,
                    (SELECT ISNULL(SUM(horas_extras), 0) FROM asistencia a WHERE a.id_trabajador = t.id_trabajador AND a.fecha BETWEEN ? AND ?) AS total_horas_extra,
                    (SELECT ISNULL(SUM(monto), 0) FROM anticipos ant WHERE ant.id_trabajador = t.id_trabajador AND ant.fecha BETWEEN ? AND ?) AS total_anticipos
                FROM trabajadores t
                INNER JOIN cargos c ON t.id_cargo = c.id_cargo
                WHERE t.activo = 1
                """;

        if (!areaFiltro.equalsIgnoreCase("TODOS")) {
            sql += " AND c.area = ? ";
        }

        sql += " ORDER BY t.nombre ASC";

        try (Connection con = conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, fechaInicio);
            ps.setDate(2, fechaFin);
            ps.setDate(3, fechaInicio);
            ps.setDate(4, fechaFin);
            ps.setDate(5, fechaInicio);
            ps.setDate(6, fechaFin);

            if (!areaFiltro.equals("TODOS")) {
                ps.setString(7, areaFiltro);
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                double sueldoD = rs.getDouble("sueldo_diario");
                int dias = rs.getInt("dias_asistidos");
                int hExtra = rs.getInt("total_horas_extra");
                double anticipos = rs.getDouble("total_anticipos");

                double valorHoraExtra = (sueldoD / 8) * 1.5;
                double totalGanado = (dias * sueldoD) + (hExtra * valorHoraExtra);
                double netoAPagar = totalGanado - anticipos;

                lista.add(new Object[] {
                        rs.getString("nombre"),
                        rs.getString("nombre_cargo"),
                        sueldoD,
                        dias,
                        hExtra,
                        Math.round(totalGanado * 100.0) / 100.0,
                        anticipos,
                        Math.round(netoAPagar * 100.0) / 100.0
                });
            }
        } catch (SQLException e) {
            System.err.println("Error en reporte nómina: " + e.getMessage());
        }
        return lista;
    }

    public boolean eliminar(int id) {
        String sql = "UPDATE trabajadores SET activo = 0 WHERE id_trabajador = ?";
        try (Connection con = conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar trabajador: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizar(trabajador t) {
        String sql = "UPDATE trabajadores SET nombre = ?, cedula = ?, telefono = ?, id_cargo = ?, activo = ? "
                + "WHERE id_trabajador = ?";

        try (Connection con = conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, t.getNombre());
            ps.setString(2, t.getCedula());
            ps.setString(3, t.getTelefono());
            ps.setInt(4, t.getIdCargo());
            ps.setBoolean(5, t.isActivo());
            ps.setInt(6, t.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar trabajador: " + e.getMessage());
            return false;
        }
    }

}
