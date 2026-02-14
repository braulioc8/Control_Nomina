/*
 * Sistema de Control de Nómina
 * Desarrollado por: Braulio Cajas (GitHub: braulioc8)
 * Licencia: MIT
 */
package controlador;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import javax.swing.JPanel;
import java.awt.Color;
import java.util.List;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * @author Braulio Cajas
 */
public class graficavista {

    public javax.swing.JPanel obtenerGraficaGastos(double neto, double anticipos) {
        DefaultPieDataset dataset = new DefaultPieDataset();

        if (neto > 0 || anticipos > 0) {
            dataset.setValue("Neto a Pagar 💰", neto);
            dataset.setValue("Anticipos entregados 💸", anticipos);
        } else {
            dataset.setValue("Sin datos esta semana", 1);
        }

        JFreeChart chart = ChartFactory.createPieChart(
                "Distribución de Nómina Semanal",
                dataset, true, true, false);

        return new ChartPanel(chart);
    }

    public javax.swing.JPanel obtenerGraficaBarras(List<Object[]> data, boolean verAsistencias, boolean verHoras) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Object[] fila : data) {
            String nombre = fila[0].toString();

            if (verAsistencias) {
                dataset.addValue(Double.parseDouble(fila[3].toString()), "Días Asistidos", nombre);
            }
            if (verHoras) {
                dataset.addValue(Double.parseDouble(fila[4].toString()), "Horas Extras", nombre);
            }
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Rendimiento por Trabajador",
                "Trabajador",
                "Cantidad",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        return new ChartPanel(chart);
    }
}
