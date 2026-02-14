package controlador;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
import gestordatos.trabajadorDAO;
import gestormodelo.trabajador;
import javax.swing.UIManager;

/**
 *
 * @author braulioo
 */
public class Controldenomina {

    public static void main(String[] args) {
        try {
            com.formdev.flatlaf.FlatDarkLaf.setup();

            UIManager.put("Button.arc", 15);
            UIManager.put("TabbedPane.selectedBackground", new java.awt.Color(220, 221, 225));
            UIManager.put("TabbedPane.selectedForeground", new java.awt.Color(33, 115, 70));
            UIManager.put("TabbedPane.underlineColor", new java.awt.Color(33, 115, 70));

        } catch (Exception ex) {
            System.err.println("Error al iniciar FlatLaf");
        }

        java.awt.EventQueue.invokeLater(() -> {
            new Interface1().setVisible(true);
        });
    }
}
