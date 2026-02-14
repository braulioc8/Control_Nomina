package gestormodelo;

/**
 * @author braulioo
 */
public class cargo {

    private int id;
    private String nombre;
    private double sueldo;
    private String area;
    private double topeAnticipo;

    public cargo() {
    }

    public cargo(int id, String nombre, double sueldo, String area, double topeAnticipo) {
        this.id = id;
        this.nombre = nombre;
        this.sueldo = sueldo;
        this.area = area;
        this.topeAnticipo = topeAnticipo;
    }

    @Override
    public String toString() {
        return nombre;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getSueldo() {
        return sueldo;
    }

    public void setSueldo(double sueldo) {
        this.sueldo = sueldo;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public double getTopeAnticipo() {
        return topeAnticipo;
    }

    public void setTopeAnticipo(double topeAnticipo) {
        this.topeAnticipo = topeAnticipo;
    }
}