/*
 * Sistema de Control de Nómina
 * Desarrollado por: Braulio Cajas (GitHub: braulioc8)
 * Licencia: MIT
 */
package gestordatos;

import conexsql.conexion;
import gestormodelo.cargo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Braulio Cajas
 */
public class cargoDAO {

    public boolean registrar(cargo c) {
        String sql = "INSERT INTO cargos (nombre_cargo, sueldo_diario, area, tope_anticipo) VALUES (?, ?, ?, ?)";

        try (Connection con = conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getNombre());
            ps.setDouble(2, c.getSueldo());
            ps.setString(3, c.getArea());
            ps.setDouble(4, c.getTopeAnticipo());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al registrar cargo: " + e.getMessage());
            return false;
        }
    }

    public List<String> listarAreasUnicas() {
        List<String> areas = new ArrayList<>();
        String sql = "SELECT DISTINCT area FROM cargos ORDER BY area ASC";

        try (Connection con = conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                areas.add(rs.getString("area"));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar áreas: " + e.getMessage());
        }
        return areas;
    }

    public List<cargo> listarCargosPorArea(String areaFiltro) {
        List<cargo> lista = new ArrayList<>();
        String sql = "SELECT * FROM cargos WHERE area = ? ORDER BY nombre_cargo ASC";

        try (Connection con = conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, areaFiltro);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                cargo c = new cargo();
                c.setId(rs.getInt("id_cargo"));
                c.setNombre(rs.getString("nombre_cargo"));
                c.setSueldo(rs.getDouble("sueldo_diario"));
                c.setArea(rs.getString("area"));
                c.setTopeAnticipo(rs.getDouble("tope_anticipo"));
                lista.add(c);
            }
        } catch (SQLException e) {
            System.err.println("Error al filtrar cargos: " + e.getMessage());
        }
        return lista;
    }

    public boolean editar(String nombreAnterior, String nombreNuevo) {
        String sql = "UPDATE cargos SET area = ? WHERE area = ?";

        try (Connection con = conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombreNuevo);
            ps.setString(2, nombreAnterior);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al editar nombre de área: " + e.getMessage());
            return false;
        }
    }

    public boolean insertar(String nombreArea) {
        String sql = "INSERT INTO cargos (nombre_cargo, sueldo_diario, area, tope_anticipo) VALUES (?, ?, ?, ?)";

        try (Connection con = conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "CONFIG_AREA");
            ps.setDouble(2, 0.0);
            ps.setString(3, nombreArea);
            ps.setDouble(4, 0.0);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al insertar área en la tabla cargos: " + e.getMessage());
            return false;
        }
    }

    public List<cargo> listarTodosLosCargos() {
        List<cargo> lista = new ArrayList<>();
        String sql = "SELECT * FROM cargos ORDER BY area, nombre_cargo ASC";

        try (Connection con = conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                cargo c = new cargo();
                c.setId(rs.getInt("id_cargo"));
                c.setNombre(rs.getString("nombre_cargo"));
                c.setSueldo(rs.getDouble("sueldo_diario"));
                c.setArea(rs.getString("area"));
                c.setTopeAnticipo(rs.getDouble("tope_anticipo"));
                lista.add(c);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar todos: " + e.getMessage());
        }
        return lista;
    }

    public int obtenerIdPorNombreYArea(String nombreCargo, String area) {
        String sql = "SELECT id_cargo FROM cargos WHERE nombre_cargo = ? AND area = ?";
        try (Connection con = conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombreCargo);
            ps.setString(2, area);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("id_cargo");
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar ID de cargo: " + e.getMessage());
        }
        return -1; // Si no lo encuentra
    }

    public boolean actualizar(cargo c) {
        String sql = "UPDATE cargos SET nombre_cargo = ?, sueldo_diario = ?, area = ?, tope_anticipo = ? WHERE id_cargo = ?";

        try (Connection con = conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getNombre());
            ps.setDouble(2, c.getSueldo());
            ps.setString(3, c.getArea());
            ps.setDouble(4, c.getTopeAnticipo());
            ps.setInt(5, c.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar cargo: " + e.getMessage());
            return false;
        }
    }

}
