/*
 * Sistema de Control de Nómina
 * Desarrollado por: Braulio Cajas (GitHub: braulioc8)
 * Licencia: MIT
 */
package gestormodelo;

/**
 * Modelo para los trabajadores del sistema de raya.
 * Incluye campos de la tabla 'trabajadores' y auxiliares de 'cargos'.
 * @author Braulio Cajas
 */
public class trabajador {
    // Campos directos de la tabla 'trabajadores'
    private int id;
    private String nombre;
    private String cedula;
    private String telefono;
    private int idCargo;
    private boolean activo; // Soft delete (1 = Activo, 0 = Inactivo)

    // Campos auxiliares (Vienen del JOIN con la tabla 'cargos')
    private String area; 
    private String cargoNombre;
    private double salarioDiario;

    // Constructor Vacío (Obligatorio para el ResultSet del DAO)
    public trabajador() {
    }

    // Constructor para registrar nuevos (Generalmente usado en formularios)
    public trabajador(String nombre, String cedula, String telefono, int idCargo) {
        this.nombre = nombre;
        this.cedula = cedula;
        this.telefono = telefono;
        this.idCargo = idCargo;
        this.activo = true; 
    }

    // --- GETTERS Y SETTERS ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public int getIdCargo() { return idCargo; }
    public void setIdCargo(int idCargo) { this.idCargo = idCargo; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getCargoNombre() { return cargoNombre; }
    public void setCargoNombre(String cargoNombre) { this.cargoNombre = cargoNombre; }

    public double getSalarioDiario() { return salarioDiario; }
    public void setSalarioDiario(double salarioDiario) { this.salarioDiario = salarioDiario; }

    // Método toString para mostrar solo el nombre en ComboBoxes o Listas
    @Override
    public String toString() {
        return nombre;
    }
}