/*
 * Sistema de Control de Nómina
 * Desarrollado por: Braulio Cajas (GitHub: braulioc8)
 * Licencia: MIT
 */
package conexsql;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;
/**
 *
 * @author Braulio Cajas
 */
public class conexion {
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=Dbobra;encrypt=true;trustServerCertificate=true;";
    
    private static final String USER = "SA"; 
    private static final String PASS = "NuevaClave123!";

    public static Connection getConexion() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            
            Connection con = DriverManager.getConnection(URL, USER, PASS);
            return con;
            
        } catch (ClassNotFoundException | SQLException e) {
            JOptionPane.showMessageDialog(null, "Error de Conexión: " + e.getMessage());
            return null;
        }}
    
}
