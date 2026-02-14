package controlador;

import gestordatos.anticipoDAO;
import gestordatos.asistenciaDAO;
import gestordatos.cargoDAO;
import gestordatos.trabajadorDAO;
import java.awt.Dimension;
import java.awt.BorderLayout;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import javax.swing.JOptionPane;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author braulioo
 */
public class Interface1 extends javax.swing.JFrame {

    String nombreObra = "";
    String nombreAreaParaEditar = "";
    int idCargoSeleccionado = -1;
    int idTrabajadorSeleccionado = -1;

    private void formatearTabla(javax.swing.JTable tabla) {
        tabla.setRowHeight(35);
        tabla.setShowGrid(true);
        tabla.setGridColor(new java.awt.Color(80, 80, 80));

        java.awt.Font fuenteEncabezado = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13);
        tabla.getTableHeader().setFont(fuenteEncabezado);

        tabla.getTableHeader().setOpaque(false);
        tabla.getTableHeader().setBackground(new java.awt.Color(45, 52, 54));
        tabla.getTableHeader().setForeground(java.awt.Color.WHITE);

        if (tabla.getParent() instanceof javax.swing.JViewport) {
            javax.swing.JScrollPane scroll = (javax.swing.JScrollPane) tabla.getParent().getParent();
            scroll.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        }
    }

    private void limpiarFormularioCargo() {
        nom_cargo.setText("");
        if (cbxarea_cargo.getItemCount() > 0) {
            cbxarea_cargo.setSelectedIndex(0);
        }
        guardar_cargo.setText("Guardar");
        lista_cargos.clearSelection();
    }

    public void exportarExcel(javax.swing.JTable tabla, String nombreObra) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Archivos de Excel", "xlsx"));
        chooser.setDialogTitle("Guardar Reporte de Nómina");

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String ruta = chooser.getSelectedFile().toString();
            if (!ruta.endsWith(".xlsx")) {
                ruta += ".xlsx";
            }

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Reporte");

                CellStyle titleStyle = workbook.createCellStyle();
                Font titleFont = workbook.createFont();
                titleFont.setBold(true);
                titleFont.setFontHeightInPoints((short) 16);
                titleStyle.setFont(titleFont);

                CellStyle headerStyle = workbook.createCellStyle();
                headerStyle.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                headerStyle.setBorderBottom(BorderStyle.THIN);
                headerStyle.setAlignment(HorizontalAlignment.CENTER);
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerFont.setColor(IndexedColors.WHITE.getIndex());
                headerStyle.setFont(headerFont);

                CellStyle currencyStyle = workbook.createCellStyle();
                DataFormat df = workbook.createDataFormat();
                currencyStyle.setDataFormat(df.getFormat("$ #,##0.00"));
                currencyStyle.setBorderBottom(BorderStyle.DOTTED);

                CellStyle zebraStyle = workbook.createCellStyle();
                zebraStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                zebraStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                Row rowTitulo = sheet.createRow(0);
                Cell cellTitulo = rowTitulo.createCell(0);
                cellTitulo.setCellValue(nombreObra.toUpperCase());
                cellTitulo.setCellStyle(titleStyle);

                Row rowSub = sheet.createRow(1);
                rowSub.createCell(0).setCellValue("Reporte generado el: " + new java.util.Date());

                Row rowEncabezado = sheet.createRow(3);
                for (int i = 0; i < tabla.getColumnCount(); i++) {
                    Cell cell = rowEncabezado.createCell(i);
                    cell.setCellValue(tabla.getColumnName(i));
                    cell.setCellStyle(headerStyle);
                }

                for (int i = 0; i < tabla.getRowCount(); i++) {
                    Row row = sheet.createRow(i + 4);
                    for (int j = 0; j < tabla.getColumnCount(); j++) {
                        Object valor = tabla.getValueAt(i, j);
                        Cell cell = row.createCell(j);

                        if (i % 2 == 0) {
                            cell.setCellStyle(zebraStyle);
                        }

                        if (valor instanceof Number) {
                            cell.setCellValue(((Number) valor).doubleValue());
                            if (tabla.getColumnName(j).toLowerCase().contains("sueldo")
                                    || tabla.getColumnName(j).toLowerCase().contains("anticipo")
                                    || tabla.getColumnName(j).toLowerCase().contains("total")) {
                                cell.setCellStyle(currencyStyle);
                            }
                        } else {
                            cell.setCellValue(valor != null ? valor.toString() : "");
                        }
                    }
                }

                for (int i = 0; i < tabla.getColumnCount(); i++) {
                    sheet.autoSizeColumn(i);
                }

                try (FileOutputStream out = new FileOutputStream(new File(ruta))) {
                    workbook.write(out);
                    JOptionPane.showMessageDialog(this, "Reporte Profesional generado.");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void cargarComboAreasPersonal() {
        cargoDAO dao = new cargoDAO();
        List<String> areas = dao.listarAreasUnicas();
        cbx_area_emp.removeAllItems();

        cbx_area_emp.addItem("TODOS");
        for (String a : areas) {
            cbx_area_emp.addItem(a);
        }
    }

    private void filtrarTablaPersonalPorArea(String area) {
        trabajadorDAO dao = new trabajadorDAO();
        List<gestormodelo.trabajador> listaTotal = dao.listarTodos();
        List<gestormodelo.trabajador> listaFiltrada;

        if (area.equalsIgnoreCase("TODOS")) {
            listaFiltrada = listaTotal;
        } else {
            listaFiltrada = listaTotal.stream()
                    .filter(t -> t.getArea() != null && t.getArea().equalsIgnoreCase(area))
                    .collect(java.util.stream.Collectors.toList());
        }

        DefaultTableModel modelo = (DefaultTableModel) tabla_gestion_personal.getModel();
        modelo.setRowCount(0);

        for (gestormodelo.trabajador t : listaFiltrada) {
            modelo.addRow(new Object[] {
                    t.getId(),
                    t.getNombre(),
                    t.getCedula(),
                    t.getCargoNombre(),
                    "$" + t.getSalarioDiario(),
                    t.isActivo() ? "ACTIVO" : "INACTIVO"
            });
        }
    }

    public void listarAreas() {
        gestordatos.cargoDAO dao = new gestordatos.cargoDAO();
        List<String> lista = dao.listarAreasUnicas();

        DefaultTableModel modelo = new DefaultTableModel(new Object[] { "ID", "Nombre Área" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        int tempId = 1;
        for (String area : lista) {
            modelo.addRow(new Object[] {
                    tempId++,
                    area
            });
        }
        tabla_area.setModel(modelo);
    }

    private java.util.List<Integer> idsTrabajadoresEnTabla = new java.util.ArrayList<>();

    private void limpiarFormularioTrabajador() {
        txt_nombre_emp.setText("");
        txt_cedula_emp.setText("");
        chk_activo_emp.setSelected(true);
        idTrabajadorSeleccionado = -1;
        jButton2.setText("Guardar");
        tabla_gestion_personal.clearSelection();
    }

    public void cargarComboAsistencia() {
        cargoDAO dao = new cargoDAO();
        List<String> areas = dao.listarAreasUnicas();

        cbxarea_asiste.removeAllItems();
        cbxarea_asiste.addItem("TODOS");

        for (String area : areas) {
            cbxarea_asiste.addItem(area);
        }
    }

    public void cargarComboNomina() {
        cargoDAO dao = new cargoDAO();
        List<String> areas = dao.listarAreasUnicas();

        cbxarea_nomina.removeAllItems();
        cbxarea_nomina.addItem("TODOS");

        for (String area : areas) {
            cbxarea_nomina.addItem(area);
        }
    }

    private void cargarTablaAsistencia(String areaFiltro) {
        try {
            java.util.Date dateSelected = jdFechaRegistro.getDate();
            if (dateSelected == null) {
                return;
            }
            java.sql.Date fechaSQL = new java.sql.Date(dateSelected.getTime());

            DefaultTableModel modelo = new DefaultTableModel() {
                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    switch (columnIndex) {
                        case 0:
                        case 1:
                        case 2:
                            return String.class; // Nombre, Cargo, Área
                        case 3:
                            return Boolean.class; // ¿Asistió?
                        case 4:
                            return Integer.class; // Horas Extra
                        case 5:
                            return Double.class; // Anticipo$ (AQUÍ ESTABA EL LÍO)
                        case 6:
                            return String.class; // Observación
                        default:
                            return String.class;
                    }
                }

                @Override
                public boolean isCellEditable(int row, int column) {
                    return column >= 3;
                }
            };

            modelo.addColumn("Nombre");
            modelo.addColumn("Cargo");
            modelo.addColumn("Área");
            modelo.addColumn("¿Asistió?");
            modelo.addColumn("Horas Extra");
            modelo.addColumn("Anticipo$");
            modelo.addColumn("Observación");

            asistenciaDAO daoAsistencia = new asistenciaDAO();
            trabajadorDAO daoTrabajador = new trabajadorDAO();
            idsTrabajadoresEnTabla.clear();

            if (daoAsistencia.existeAsistenciaDeFecha(fechaSQL)) {
                List<gestormodelo.asistencia> listaHoy = daoAsistencia.listarPorFecha(fechaSQL);
                for (gestormodelo.asistencia a : listaHoy) {
                    if (areaFiltro.equalsIgnoreCase("TODOS") || a.getArea().equalsIgnoreCase(areaFiltro)) {
                        modelo.addRow(new Object[] {
                                a.getNombreTrabajador(),
                                a.getNombreCargo(),
                                a.getArea(),
                                a.getEstado().equals("ASISTIO"),
                                a.getHorasExtras(),
                                a.getAnticipo(),
                                a.getObservacion()
                        });
                        idsTrabajadoresEnTabla.add(a.getIdTrabajador());
                    }
                }
            } else {
                List<gestormodelo.trabajador> activos = daoTrabajador.listarActivos();
                for (gestormodelo.trabajador t : activos) {
                    if (areaFiltro.equals("TODOS") || t.getArea().equalsIgnoreCase(areaFiltro)) {
                        modelo.addRow(new Object[] {
                                t.getNombre(),
                                t.getCargoNombre(),
                                t.getArea(),
                                true,
                                0,
                                0.0,
                                ""
                        });
                        idsTrabajadoresEnTabla.add(t.getId());
                    }
                }
            }

            tblAsistencia.setModel(modelo);

        } catch (Exception e) {
            System.err.println("Error fatal cargando tabla: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static final java.util.logging.Logger logger = java.util.logging.Logger
            .getLogger(Interface1.class.getName());

    private void configurarAnchoColumnasNomina() {
        tabla_anticipo.getColumnModel().getColumn(0).setPreferredWidth(150);
        tabla_anticipo.getColumnModel().getColumn(1).setPreferredWidth(100);
        tabla_anticipo.getColumnModel().getColumn(7).setPreferredWidth(120);
    }

    public void listarCargos() {
        if (cbxarea_cargo.getSelectedItem() == null) {
            return;
        }

        String areaSeleccionada = cbxarea_cargo.getSelectedItem().toString();
        cargoDAO dao = new cargoDAO();
        List<gestormodelo.cargo> lista;

        if (areaSeleccionada.equals("TODOS")) {
            lista = dao.listarTodosLosCargos();
        } else {
            lista = dao.listarCargosPorArea(areaSeleccionada);
        }

        String[] titulos = { "ID", "Puesto / Cargo", "Área", "Sueldo", "Tope" };
        DefaultTableModel modelo = new DefaultTableModel(null, titulos);

        for (gestormodelo.cargo c : lista) {
            if (!c.getNombre().equals("CONFIG_AREA")) {
                modelo.addRow(new Object[] {
                        c.getId(),
                        c.getNombre(),
                        c.getArea(),
                        "$" + c.getSueldo(),
                        "$" + c.getTopeAnticipo()
                });
            }
        }
        lista_cargos.setModel(modelo);
    }

    public void cargarComboAreasConfig() {
        cargoDAO dao = new cargoDAO();
        List<String> areas = dao.listarAreasUnicas();

        cbxarea_cargo.removeAllItems();
        cbxarea_cargo.addItem("TODOS");

        for (String area : areas) {
            cbxarea_cargo.addItem(area);
        }
    }

    private void cbx_area_personalItemStateChanged(java.awt.event.ItemEvent evt) {
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            String area = cbx_area_emp.getSelectedItem().toString();
            cargoDAO dao = new cargoDAO();
            List<gestormodelo.cargo> cargos = dao.listarCargosPorArea(area);

            cbx_cargo_emp.removeAllItems();
            for (gestormodelo.cargo c : cargos) {
                cbx_cargo_emp.addItem(c.getNombre());
            }
        }
    }

    public void listarTrabajadores() {
        try {
            gestordatos.trabajadorDAO dao = new gestordatos.trabajadorDAO();
            List<gestormodelo.trabajador> lista = dao.listarTodos();

            String[] titulos = { "ID", "Nombre Completo", "Cédula/RUC", "Puesto/Cargo", "Salario Diario",
                    "Estado Actual" };

            DefaultTableModel modelo = new DefaultTableModel(null, titulos) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            for (gestormodelo.trabajador t : lista) {
                String estadoStr = t.isActivo() ? "ACTIVO" : "INACTIVO";

                modelo.addRow(new Object[] {
                        t.getId(),
                        t.getNombre(),
                        t.getCedula(),
                        t.getCargoNombre(),
                        "$" + t.getSalarioDiario(),
                        estadoStr
                });
            }

            tabla_gestion_personal.setModel(modelo);

        } catch (Exception e) {
            System.err.println("Error al listar trabajadores: " + e.getMessage());
        }
    }

    public void actualizarGraficaNomina() {
        if (dcInicio.getDate() == null || dcFin.getDate() == null) {
            return;
        }

        java.sql.Date inicio = new java.sql.Date(dcInicio.getDate().getTime());
        java.sql.Date fin = new java.sql.Date(dcFin.getDate().getTime());
        List<Object[]> reporte = new trabajadorDAO().obtenerReporteNomina(inicio, fin,
                cbxarea_nomina.getSelectedItem().toString());

        boolean verAsis = chkAsistencias.isSelected();
        boolean verHoras = chkHoras.isSelected();

        graficavista fabrica = new graficavista();
        JPanel panelGrafico = fabrica.obtenerGraficaBarras(reporte, verAsis, verHoras);

        grafica_asistencia.getContentPane().removeAll();

        grafica_asistencia.setLayout(new java.awt.BorderLayout());

        grafica_asistencia.setVisible(true);

        grafica_asistencia.add(panelGrafico, java.awt.BorderLayout.CENTER);
        grafica_asistencia.revalidate();
        grafica_asistencia.repaint();
    }

    public String solicitarNombreObra() {

        while (nombreObra.trim().isEmpty()) {
            nombreObra = JOptionPane.showInputDialog(this,
                    "Ingrese el nombre de la Obra / Proyecto:",
                    "Identificación del Reporte",
                    JOptionPane.QUESTION_MESSAGE);

            // Si el usuario da a cancelar, devolvemos un valor por defecto o nulo
            if (nombreObra == null) {
                return "PROYECTO GENERAL";
            }

            if (nombreObra.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre de la obra es obligatorio para el reporte.");
            }
        }
        return nombreObra.toUpperCase(); // Lo devolvemos en mayúsculas para que luzca pro
    }

    public Interface1() {

        initComponents();

        // --- ESTILO DE PESTAÑAS (JTabbedPane) ---
        // Opcional: Color de la línea de subrayado de la pestaña
        // UIManager.put("TabbedPane.underlineColor", new Color(41, 128, 185));
        // UIManager.put("TabbedPane.inactiveUnderlineColor", new Color(95, 158, 160));
        Calcular.setBackground(new java.awt.Color(41, 128, 185));
        Calcular.setForeground(java.awt.Color.WHITE);
        excel_asistencia.setBackground(new java.awt.Color(33, 115, 70));
        UIManager.put("jInternalFrame.selectedBackground", new java.awt.Color(52, 152, 219));
        UIManager.put("jInternalFrame.selectedForeground", java.awt.Color.WHITE);
        UIManager.put("jInternalFrame.underlineColor", new java.awt.Color(41, 128, 185));

        UIManager.put("TabbedPane.underlineColor", new java.awt.Color(52, 152, 219));
        UIManager.put("TabbedPane.selectedBackground", new java.awt.Color(45, 52, 54));
        UIManager.put("TabbedPane.selectedForeground", java.awt.Color.WHITE);
        UIManager.put("TabbedPane.hoverColor", new java.awt.Color(60, 63, 65));
        UIManager.put("TabbedPane.underlineHeight", 4);

        formatearTabla(tblAsistencia);
        formatearTabla(tabla_anticipo);
        formatearTabla(tabla_gestion_personal);
        formatearTabla(tabla_area);
        formatearTabla(lista_cargos);

        txt_nombre_emp.setPreferredSize(new java.awt.Dimension(200, 30));
        txt_cedula_emp.setPreferredSize(new java.awt.Dimension(150, 30));
        nom_area.setPreferredSize(new java.awt.Dimension(150, 30));
        nom_cargo.setPreferredSize(new java.awt.Dimension(150, 30));
        cbxarea_cargo.setPreferredSize(new java.awt.Dimension(150, 30));
        cbx_cargo_emp.setPreferredSize(new java.awt.Dimension(150, 30));

        solicitarNombreObra();
        txt_nombre_emp.setText("");
        txt_cedula_emp.setText("");
        nom_area.setText("");
        nom_cargo.setText("");
        nombre_obra1.setText(nombreObra);
        nombre_obra2.setText(nombreObra);

        listarAreas();
        listarCargos();
        cargarComboAreasConfig();
        cargarComboAsistencia();
        cargarComboAreasPersonal();

        jInternalFrame3.setLayout(new java.awt.BorderLayout());
        javax.swing.plaf.basic.BasicInternalFrameUI ui = (javax.swing.plaf.basic.BasicInternalFrameUI) jInternalFrame3
                .getUI();
        ui.setNorthPane(null);
        jInternalFrame3.setBorder(null);

        actualizarGraficaNomina();

        if (cbxarea_asiste.getSelectedItem() != null) {
            cargarTablaAsistencia(cbxarea_asiste.getSelectedItem().toString());
        }
        this.setLocationRelativeTo(null);

        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                int index = jTabbedPane1.getSelectedIndex();

                switch (index) {
                    case 0:
                        cargarComboAsistencia();
                        if (cbxarea_asiste.getSelectedItem() != null) {
                            cargarTablaAsistencia(cbxarea_asiste.getSelectedItem().toString());
                        }
                        break;

                    case 1:
                        actualizarGraficaNomina();
                        cargarComboNomina();

                        break;

                    case 2:
                        jInternalFrame3.repaint();
                        jInternalFrame3.revalidate();
                        break;

                    case 3:
                        listarTrabajadores();
                        listarAreas();
                        cargarComboAreasConfig();
                        listarCargos();
                        break;

                    case 4:
                        cargarComboAreasPersonal();
                        if (cbx_area_emp.getSelectedItem() != null) {
                            filtrarTablaPersonalPorArea(cbx_area_emp.getSelectedItem().toString());
                        }
                        break;
                }
            }
        });

        jdFechaRegistro.setDate(new java.util.Date());

        java.util.Calendar cal = java.util.Calendar.getInstance();
        dcFin.setDate(cal.getTime());

        cal.add(java.util.Calendar.DAY_OF_YEAR, -6);
        dcInicio.setDate(cal.getTime());

        cargarTablaAsistencia("TODOS");

        trabajadorDAO dao = new trabajadorDAO();

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jInternalFrame1 = new javax.swing.JInternalFrame();
        jButton1 = new javax.swing.JButton();
        nombre_obra2 = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblAsistencia = new javax.swing.JTable();
        jLabel5 = new javax.swing.JLabel();
        todosasisten = new javax.swing.JButton();
        cbxarea_asiste = new javax.swing.JComboBox<>();
        jdFechaRegistro = new com.toedter.calendar.JDateChooser();
        excel_asistencia = new javax.swing.JButton();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jInternalFrame2 = new javax.swing.JInternalFrame();
        nombre_obra1 = new javax.swing.JTextField();
        Calcular = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tabla_anticipo = new javax.swing.JTable();
        dcInicio = new com.toedter.calendar.JDateChooser();
        dcFin = new com.toedter.calendar.JDateChooser();
        jLabel4 = new javax.swing.JLabel();
        excel_pagos = new javax.swing.JButton();
        cbxarea_nomina = new javax.swing.JComboBox<>();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jInternalFrame3 = new javax.swing.JInternalFrame();
        chkAsistencias = new javax.swing.JCheckBox();
        chkHoras = new javax.swing.JCheckBox();
        grafica_asistencia = new javax.swing.JInternalFrame();
        jLabel6 = new javax.swing.JLabel();
        jInternalFrame5 = new javax.swing.JInternalFrame();
        cbx_area_emp = new javax.swing.JComboBox<>();
        cbx_cargo_emp = new javax.swing.JComboBox<>();
        jScrollPane5 = new javax.swing.JScrollPane();
        tabla_gestion_personal = new javax.swing.JTable();
        txt_nombre_emp = new javax.swing.JTextField();
        txt_cedula_emp = new javax.swing.JTextField();
        chk_activo_emp = new javax.swing.JCheckBox();
        jButton2 = new javax.swing.JButton();
        cancelar_emp = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        excel_trabajadores = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jInternalFrame4 = new javax.swing.JInternalFrame();
        guardar_area = new javax.swing.JButton();
        nom_area = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        tabla_area = new javax.swing.JTable();
        cbxarea_cargo = new javax.swing.JComboBox<>();
        nom_cargo = new javax.swing.JTextField();
        guardar_cargo = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        lista_cargos = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        cancelar_area = new javax.swing.JButton();
        excel_cargos = new javax.swing.JButton();
        cancelar_cargo = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTabbedPane1.setBackground(new java.awt.Color(52, 152, 219));
        jTabbedPane1.setForeground(new java.awt.Color(33, 37, 41));

        jInternalFrame1.setResizable(true);
        jInternalFrame1.setVisible(true);

        jButton1.setBackground(new java.awt.Color(46, 204, 113));
        jButton1.setForeground(new java.awt.Color(0, 0, 0));
        jButton1.setText("Guardar");
        jButton1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jButton1.addActionListener(this::jButton1ActionPerformed);

        nombre_obra2.setEditable(false);
        nombre_obra2.setText("Nomobre Obra");
        nombre_obra2.addActionListener(this::nombre_obra2ActionPerformed);

        tblAsistencia.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {
                        { null, null, null, null },
                        { null, null, null, null },
                        { null, null, null, null },
                        { null, null, null, null }
                },
                new String[] {
                        "Title 1", "Title 2", "Title 3", "Title 4"
                }));
        jScrollPane1.setViewportView(tblAsistencia);

        jLabel5.setText("Control diario");

        todosasisten.setText("Marcar Todos");
        todosasisten.addActionListener(this::todosasistenActionPerformed);

        cbxarea_asiste.addItemListener(this::cbxarea_asisteItemStateChanged);
        cbxarea_asiste.addActionListener(this::cbxarea_asisteActionPerformed);

        jdFechaRegistro.addPropertyChangeListener(this::jdFechaRegistroPropertyChange);

        excel_asistencia.setBackground(new java.awt.Color(33, 115, 70));
        excel_asistencia.setForeground(new java.awt.Color(220, 221, 225));
        excel_asistencia.setText("Excel");
        excel_asistencia.addActionListener(this::excel_asistenciaActionPerformed);

        jLabel19.setText("Área");

        jLabel20.setText("Fecha del informe:");

        javax.swing.GroupLayout jInternalFrame1Layout = new javax.swing.GroupLayout(jInternalFrame1.getContentPane());
        jInternalFrame1.getContentPane().setLayout(jInternalFrame1Layout);
        jInternalFrame1Layout.setHorizontalGroup(
                jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jInternalFrame1Layout.createSequentialGroup()
                                .addGap(33, 33, 33)
                                .addGroup(jInternalFrame1Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(jInternalFrame1Layout.createSequentialGroup()
                                                .addGap(0, 38, Short.MAX_VALUE)
                                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 581,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(39, 39, 39))
                                        .addGroup(jInternalFrame1Layout.createSequentialGroup()
                                                .addGroup(jInternalFrame1Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(jInternalFrame1Layout.createSequentialGroup()
                                                                .addGap(238, 238, 238)
                                                                .addComponent(jLabel5))
                                                        .addGroup(jInternalFrame1Layout.createSequentialGroup()
                                                                .addGroup(jInternalFrame1Layout.createParallelGroup(
                                                                        javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(nombre_obra2,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addGroup(jInternalFrame1Layout
                                                                                .createSequentialGroup()
                                                                                .addGap(49, 49, 49)
                                                                                .addGroup(jInternalFrame1Layout
                                                                                        .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                        .addGroup(jInternalFrame1Layout
                                                                                                .createSequentialGroup()
                                                                                                .addComponent(
                                                                                                        excel_asistencia)
                                                                                                .addGap(18, 18, 18)
                                                                                                .addComponent(jButton1))
                                                                                        .addGroup(jInternalFrame1Layout
                                                                                                .createSequentialGroup()
                                                                                                .addComponent(jLabel19)
                                                                                                .addGap(18, 18, 18)
                                                                                                .addComponent(
                                                                                                        cbxarea_asiste,
                                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                        118,
                                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                .addGap(65, 65, 65)
                                                                                                .addComponent(
                                                                                                        jLabel20)))))
                                                                .addGap(18, 18, 18)
                                                                .addComponent(jdFechaRegistro,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 155,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addGap(0, 0, Short.MAX_VALUE))))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                jInternalFrame1Layout.createSequentialGroup()
                                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(todosasisten)
                                        .addGap(101, 101, 101)));
        jInternalFrame1Layout.setVerticalGroup(
                jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jInternalFrame1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 28,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(jInternalFrame1Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jInternalFrame1Layout.createSequentialGroup()
                                                .addGap(33, 33, 33)
                                                .addGroup(jInternalFrame1Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(jdFechaRegistro,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jLabel20)))
                                        .addGroup(jInternalFrame1Layout.createSequentialGroup()
                                                .addGap(9, 9, 9)
                                                .addComponent(nombre_obra2, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addGroup(jInternalFrame1Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(cbxarea_asiste,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jLabel19))))
                                .addGroup(jInternalFrame1Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jInternalFrame1Layout.createSequentialGroup()
                                                .addGap(9, 9, 9)
                                                .addComponent(todosasisten))
                                        .addGroup(jInternalFrame1Layout.createSequentialGroup()
                                                .addGap(18, 18, 18)
                                                .addGroup(jInternalFrame1Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(excel_asistencia)
                                                        .addComponent(jButton1))
                                                .addGap(33, 33, 33)
                                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 279,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap(26, Short.MAX_VALUE)));

        jTabbedPane1.addTab("Asistencias", jInternalFrame1);

        jInternalFrame2.setVisible(true);

        nombre_obra1.setEditable(false);
        nombre_obra1.setText("Nomobre Obra");
        nombre_obra1.addActionListener(this::nombre_obra1ActionPerformed);

        Calcular.setBackground(new java.awt.Color(41, 128, 185));
        Calcular.setText("Calcular");
        Calcular.addActionListener(this::CalcularActionPerformed);

        tabla_anticipo.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {
                        { null, null, null, null, null, null, null, null },
                        { null, null, null, null, null, null, null, null },
                        { null, null, null, null, null, null, null, null },
                        { null, null, null, null, null, null, null, null }
                },
                new String[] {
                        "Nombre", "Cargo", "Sueldo", "Días", "H.Extra", "Total Ganado", "Anticipos", "Neto a Pagar"
                }));
        jScrollPane2.setViewportView(tabla_anticipo);

        jLabel4.setText("Reporte de nómina");

        excel_pagos.setBackground(new java.awt.Color(33, 115, 70));
        excel_pagos.setForeground(new java.awt.Color(220, 221, 225));
        excel_pagos.setText("Excel");
        excel_pagos.addActionListener(this::excel_pagosActionPerformed);

        cbxarea_nomina.setModel(
                new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbxarea_nomina.addActionListener(this::cbxarea_nominaActionPerformed);

        jLabel15.setText("Desde");

        jLabel16.setText("Hasta");

        jLabel17.setText("Área");

        jLabel18.setText("Generara reporte:");

        javax.swing.GroupLayout jInternalFrame2Layout = new javax.swing.GroupLayout(jInternalFrame2.getContentPane());
        jInternalFrame2.getContentPane().setLayout(jInternalFrame2Layout);
        jInternalFrame2Layout.setHorizontalGroup(
                jInternalFrame2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jInternalFrame2Layout
                                .createSequentialGroup()
                                .addGap(34, 34, 34)
                                .addGroup(jInternalFrame2Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jInternalFrame2Layout.createSequentialGroup()
                                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 27,
                                                        Short.MAX_VALUE))
                                        .addGroup(jInternalFrame2Layout.createSequentialGroup()
                                                .addComponent(nombre_obra1, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jLabel4)
                                                .addGap(200, 200, 200)))
                                .addGroup(jInternalFrame2Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jInternalFrame2Layout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(jLabel17)
                                                .addComponent(jLabel15)
                                                .addComponent(jLabel16)
                                                .addComponent(dcFin, javax.swing.GroupLayout.DEFAULT_SIZE, 145,
                                                        Short.MAX_VALUE)
                                                .addComponent(dcInicio, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(cbxarea_nomina, 0, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        Short.MAX_VALUE)
                                                .addComponent(Calcular, javax.swing.GroupLayout.Alignment.TRAILING,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addComponent(jLabel18)
                                        .addComponent(excel_pagos, javax.swing.GroupLayout.PREFERRED_SIZE, 145,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(33, 33, 33)));
        jInternalFrame2Layout.setVerticalGroup(
                jInternalFrame2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jInternalFrame2Layout.createSequentialGroup()
                                .addGroup(jInternalFrame2Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jInternalFrame2Layout.createSequentialGroup()
                                                .addGap(7, 7, 7)
                                                .addGroup(jInternalFrame2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(jLabel4)
                                                        .addComponent(nombre_obra1,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                                jInternalFrame2Layout.createSequentialGroup()
                                                        .addContainerGap()
                                                        .addComponent(jLabel18)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jInternalFrame2Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jInternalFrame2Layout.createSequentialGroup()
                                                .addGap(12, 12, 12)
                                                .addComponent(jLabel15)
                                                .addGap(1, 1, 1)
                                                .addComponent(dcInicio, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(10, 10, 10)
                                                .addComponent(jLabel16)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(dcFin, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 25,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cbxarea_nomina, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(24, 24, 24)
                                                .addComponent(Calcular)
                                                .addGap(18, 18, 18)
                                                .addComponent(excel_pagos))
                                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(15, Short.MAX_VALUE)));

        jTabbedPane1.addTab("Reporte de nómina", jInternalFrame2);

        jInternalFrame3.setMaximizable(true);
        jInternalFrame3.setVisible(true);

        chkAsistencias.setText("Asistencia");
        chkAsistencias.addItemListener(this::chkAsistenciasItemStateChanged);
        chkAsistencias.addActionListener(this::chkAsistenciasActionPerformed);

        chkHoras.setText("Horas extra");
        chkHoras.addItemListener(this::chkHorasItemStateChanged);

        grafica_asistencia.setVisible(true);

        javax.swing.GroupLayout grafica_asistenciaLayout = new javax.swing.GroupLayout(
                grafica_asistencia.getContentPane());
        grafica_asistencia.getContentPane().setLayout(grafica_asistenciaLayout);
        grafica_asistenciaLayout.setHorizontalGroup(
                grafica_asistenciaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 444, Short.MAX_VALUE));
        grafica_asistenciaLayout.setVerticalGroup(
                grafica_asistenciaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 412, Short.MAX_VALUE));

        jLabel6.setText("Visor de asistencias y de horas extra.");

        javax.swing.GroupLayout jInternalFrame3Layout = new javax.swing.GroupLayout(jInternalFrame3.getContentPane());
        jInternalFrame3.getContentPane().setLayout(jInternalFrame3Layout);
        jInternalFrame3Layout.setHorizontalGroup(
                jInternalFrame3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jInternalFrame3Layout.createSequentialGroup()
                                .addContainerGap(12, Short.MAX_VALUE)
                                .addGroup(jInternalFrame3Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(chkAsistencias)
                                        .addComponent(chkHoras))
                                .addGap(70, 70, 70)
                                .addComponent(grafica_asistencia, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(49, 49, 49))
                        .addGroup(jInternalFrame3Layout.createSequentialGroup()
                                .addGap(212, 212, 212)
                                .addComponent(jLabel6)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        jInternalFrame3Layout.setVerticalGroup(
                jInternalFrame3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jInternalFrame3Layout.createSequentialGroup()
                                .addGap(71, 71, 71)
                                .addComponent(chkAsistencias)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(chkHoras)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jInternalFrame3Layout
                                .createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel6)
                                .addGap(18, 18, 18)
                                .addComponent(grafica_asistencia, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap()));

        jTabbedPane1.addTab("Gráficas", jInternalFrame3);

        jInternalFrame5.setVisible(true);

        cbx_area_emp.setModel(
                new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbx_area_emp.addItemListener(this::cbx_area_empItemStateChanged);
        cbx_area_emp.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cbx_area_empMouseClicked(evt);
            }
        });
        cbx_area_emp.addActionListener(this::cbx_area_empActionPerformed);

        cbx_cargo_emp.setModel(
                new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        tabla_gestion_personal.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {
                        { null, null, null, null, null, null },
                        { null, null, null, null, null, null },
                        { null, null, null, null, null, null },
                        { null, null, null, null, null, null },
                        { null, null, null, null, null, null },
                        { null, null, null, null, null, null },
                        { null, null, null, null, null, null },
                        { null, null, null, null, null, null },
                        { null, null, null, null, null, null },
                        { null, null, null, null, null, null },
                        { null, null, null, null, null, null },
                        { null, null, null, null, null, null },
                        { null, null, null, null, null, null },
                        { null, null, null, null, null, null },
                        { null, null, null, null, null, null },
                        { null, null, null, null, null, null },
                        { null, null, null, null, null, null },
                        { null, null, null, null, null, null },
                        { null, null, null, null, null, null },
                        { null, null, null, null, null, null },
                        { null, null, null, null, null, null }
                },
                new String[] {
                        "ID", "Nombres", "Cédula", "Cargo", "Salario Diario", "ACTIVO"
                }));
        tabla_gestion_personal.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabla_gestion_personalMouseClicked(evt);
            }
        });
        jScrollPane5.setViewportView(tabla_gestion_personal);

        txt_nombre_emp.setText("Nombre");

        txt_cedula_emp.setText("Cédula");

        chk_activo_emp.setText("Activo?");
        chk_activo_emp.setToolTipText("");
        chk_activo_emp.addActionListener(this::chk_activo_empActionPerformed);

        jButton2.setBackground(new java.awt.Color(46, 204, 113));
        jButton2.setForeground(new java.awt.Color(0, 0, 0));
        jButton2.setText("Guardar");
        jButton2.addActionListener(this::jButton2ActionPerformed);

        cancelar_emp.setBackground(new java.awt.Color(231, 76, 60));
        cancelar_emp.setText("Cancelar");
        cancelar_emp.addActionListener(this::cancelar_empActionPerformed);

        jLabel3.setText("Editor de empleados");

        excel_trabajadores.setBackground(new java.awt.Color(33, 115, 70));
        excel_trabajadores.setText("Excel");
        excel_trabajadores.addActionListener(this::excel_trabajadoresActionPerformed);

        jLabel10.setText("Cargo");

        jLabel11.setText("Área");

        jLabel12.setText("Nombres");

        jLabel13.setText("Cédula");

        javax.swing.GroupLayout jInternalFrame5Layout = new javax.swing.GroupLayout(jInternalFrame5.getContentPane());
        jInternalFrame5.getContentPane().setLayout(jInternalFrame5Layout);
        jInternalFrame5Layout.setHorizontalGroup(
                jInternalFrame5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jInternalFrame5Layout.createSequentialGroup()
                                .addGap(29, 29, 29)
                                .addGroup(jInternalFrame5Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel13)
                                        .addGroup(jInternalFrame5Layout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                .addComponent(txt_nombre_emp, javax.swing.GroupLayout.DEFAULT_SIZE, 148,
                                                        Short.MAX_VALUE)
                                                .addComponent(chk_activo_emp, javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(cbx_cargo_emp, javax.swing.GroupLayout.Alignment.LEADING,
                                                        0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(cbx_area_emp, javax.swing.GroupLayout.Alignment.LEADING,
                                                        0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addComponent(txt_cedula_emp, javax.swing.GroupLayout.DEFAULT_SIZE, 148,
                                                Short.MAX_VALUE)
                                        .addComponent(excel_trabajadores))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jInternalFrame5Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jInternalFrame5Layout.createSequentialGroup()
                                                .addComponent(jButton2)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(cancelar_emp)
                                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(jInternalFrame5Layout.createSequentialGroup()
                                                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 497,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jLabel14)
                                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        Short.MAX_VALUE))))
                        .addGroup(jInternalFrame5Layout.createSequentialGroup()
                                .addGap(272, 272, 272)
                                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE)
                                .addGap(284, 284, 284)));
        jInternalFrame5Layout.setVerticalGroup(
                jInternalFrame5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jInternalFrame5Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel3)
                                .addGap(1, 1, 1)
                                .addGroup(jInternalFrame5Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jInternalFrame5Layout.createSequentialGroup()
                                                .addGroup(jInternalFrame5Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING,
                                                                jInternalFrame5Layout.createSequentialGroup()
                                                                        .addComponent(jLabel11)
                                                                        .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                        .addComponent(cbx_area_emp,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                        .addComponent(jLabel10)
                                                                        .addGap(2, 2, 2)
                                                                        .addComponent(cbx_cargo_emp,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addGap(9, 9, 9)
                                                                        .addComponent(jLabel12)
                                                                        .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                        .addComponent(txt_nombre_emp,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                61,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                        .addComponent(jLabel13)
                                                                        .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                        .addComponent(txt_cedula_emp,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addGap(15, 15, 15)
                                                                        .addComponent(chk_activo_emp))
                                                        .addComponent(jScrollPane5,
                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 320,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(18, 18, 18)
                                                .addGroup(jInternalFrame5Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(jButton2)
                                                        .addComponent(cancelar_emp)
                                                        .addComponent(excel_trabajadores))
                                                .addContainerGap(97, Short.MAX_VALUE))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                                jInternalFrame5Layout.createSequentialGroup()
                                                        .addGap(0, 0, Short.MAX_VALUE)
                                                        .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                41, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGap(85, 85, 85)))));

        jTabbedPane1.addTab("Empleados", jInternalFrame5);

        jInternalFrame4.setVisible(true);

        guardar_area.setBackground(new java.awt.Color(46, 204, 113));
        guardar_area.setForeground(new java.awt.Color(0, 0, 0));
        guardar_area.setText("Guardar");
        guardar_area.addActionListener(this::guardar_areaActionPerformed);

        nom_area.setText("jTextField1");

        tabla_area.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {
                        { null, null, null, null },
                        { null, null, null, null },
                        { null, null, null, null },
                        { null, null, null, null }
                },
                new String[] {
                        "Title 1", "Title 2", "Title 3", "Title 4"
                }));
        tabla_area.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabla_areaMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(tabla_area);

        cbxarea_cargo.setModel(
                new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbxarea_cargo.addItemListener(this::cbxarea_cargoItemStateChanged);
        cbxarea_cargo.addActionListener(this::cbxarea_cargoActionPerformed);

        nom_cargo.setText("jTextField2");

        guardar_cargo.setBackground(new java.awt.Color(46, 204, 113));
        guardar_cargo.setForeground(new java.awt.Color(0, 0, 0));
        guardar_cargo.setText("Guardar");
        guardar_cargo.addActionListener(this::guardar_cargoActionPerformed);

        lista_cargos.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {
                        { null, null, null, null },
                        { null, null, null, null },
                        { null, null, null, null },
                        { null, null, null, null }
                },
                new String[] {
                        "Title 1", "Title 2", "Title 3", "Title 4"
                }));
        lista_cargos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lista_cargosMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(lista_cargos);

        jLabel1.setText("Tabla de cargos");

        jLabel2.setText("Tabla de áreas");

        cancelar_area.setBackground(new java.awt.Color(231, 76, 60));
        cancelar_area.setText("Cancelar");
        cancelar_area.addActionListener(this::cancelar_areaActionPerformed);

        excel_cargos.setBackground(new java.awt.Color(33, 115, 70));
        excel_cargos.setText("Excel");
        excel_cargos.addActionListener(this::excel_cargosActionPerformed);

        cancelar_cargo.setBackground(new java.awt.Color(231, 76, 60));
        cancelar_cargo.setText("Cancelar");
        cancelar_cargo.addActionListener(this::cancelar_cargoActionPerformed);

        jLabel7.setText("Nombre de área:");

        jLabel8.setText("Nombre cargo");

        jLabel9.setText("Área");

        javax.swing.GroupLayout jInternalFrame4Layout = new javax.swing.GroupLayout(jInternalFrame4.getContentPane());
        jInternalFrame4.getContentPane().setLayout(jInternalFrame4Layout);
        jInternalFrame4Layout.setHorizontalGroup(
                jInternalFrame4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jInternalFrame4Layout.createSequentialGroup()
                                .addGap(318, 318, 318)
                                .addComponent(jLabel2)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(jInternalFrame4Layout.createSequentialGroup()
                                .addGap(11, 11, 11)
                                .addComponent(guardar_area, javax.swing.GroupLayout.PREFERRED_SIZE, 87,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(cancelar_area)
                                .addGap(0, 0, Short.MAX_VALUE))
                        .addGroup(jInternalFrame4Layout.createSequentialGroup()
                                .addGroup(jInternalFrame4Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jInternalFrame4Layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addGroup(jInternalFrame4Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(jInternalFrame4Layout.createSequentialGroup()
                                                                .addComponent(nom_area)
                                                                .addGap(12, 12, 12))
                                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                                                jInternalFrame4Layout.createSequentialGroup()
                                                                        .addGroup(jInternalFrame4Layout
                                                                                .createParallelGroup(
                                                                                        javax.swing.GroupLayout.Alignment.LEADING)
                                                                                .addGroup(jInternalFrame4Layout
                                                                                        .createSequentialGroup()
                                                                                        .addComponent(guardar_cargo)
                                                                                        .addPreferredGap(
                                                                                                javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                        .addComponent(cancelar_cargo))
                                                                                .addComponent(nom_cargo,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                        178,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                        .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.UNRELATED,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                Short.MAX_VALUE))))
                                        .addGroup(jInternalFrame4Layout.createSequentialGroup()
                                                .addGroup(jInternalFrame4Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(jInternalFrame4Layout.createSequentialGroup()
                                                                .addGap(19, 19, 19)
                                                                .addComponent(jLabel7))
                                                        .addGroup(jInternalFrame4Layout.createSequentialGroup()
                                                                .addContainerGap()
                                                                .addComponent(cbxarea_cargo,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 178,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(jInternalFrame4Layout.createSequentialGroup()
                                                                .addContainerGap()
                                                                .addComponent(jLabel9))
                                                        .addGroup(jInternalFrame4Layout.createSequentialGroup()
                                                                .addContainerGap()
                                                                .addComponent(jLabel8))
                                                        .addGroup(jInternalFrame4Layout.createSequentialGroup()
                                                                .addContainerGap()
                                                                .addComponent(excel_cargos)))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .addGroup(jInternalFrame4Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jInternalFrame4Layout
                                                .createSequentialGroup()
                                                .addGroup(jInternalFrame4Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jScrollPane3,
                                                                javax.swing.GroupLayout.Alignment.TRAILING,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jScrollPane4,
                                                                javax.swing.GroupLayout.Alignment.TRAILING,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(35, 35, 35))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                                jInternalFrame4Layout.createSequentialGroup()
                                                        .addComponent(jLabel1)
                                                        .addGap(323, 323, 323)))));
        jInternalFrame4Layout.setVerticalGroup(
                jInternalFrame4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jInternalFrame4Layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jInternalFrame4Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jInternalFrame4Layout.createSequentialGroup()
                                                .addGap(10, 10, 10)
                                                .addComponent(jLabel7)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(nom_area, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addGroup(jInternalFrame4Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(guardar_area)
                                                        .addComponent(cancelar_area)))
                                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 142,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jInternalFrame4Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jInternalFrame4Layout.createSequentialGroup()
                                                .addGap(39, 39, 39)
                                                .addComponent(jLabel1)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 224,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jInternalFrame4Layout.createSequentialGroup()
                                                .addGap(54, 54, 54)
                                                .addComponent(jLabel9)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cbxarea_cargo, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(14, 14, 14)
                                                .addComponent(jLabel8)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(nom_cargo, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addGroup(jInternalFrame4Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(guardar_cargo)
                                                        .addComponent(cancelar_cargo))
                                                .addGap(18, 18, 18)
                                                .addComponent(excel_cargos)))
                                .addContainerGap(7, Short.MAX_VALUE)));

        jTabbedPane1.addTab("Áreas y cargos", jInternalFrame4);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 703, Short.MAX_VALUE)
                                .addGap(19, 19, 19)));
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 557,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 6, Short.MAX_VALUE)));

        jTabbedPane1.getAccessibleContext().setAccessibleName("Area_cargo");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void nombre_obra2ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_nombre_obra2ActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_nombre_obra2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton1ActionPerformed
        if (jdFechaRegistro.getDate() == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Selecciona una fecha válida.");
            return;
        }
        java.sql.Date fechaSQL = new java.sql.Date(jdFechaRegistro.getDate().getTime());
        asistenciaDAO asisDAO = new asistenciaDAO();
        anticipoDAO antDAO = new anticipoDAO();

        if (asisDAO.existeAsistenciaDeFecha(fechaSQL)) {
            int respuesta = javax.swing.JOptionPane.showConfirmDialog(
                    this,
                    "Ya existen datos para el día " + fechaSQL + ". ¿Deseas sobreescribirlos?",
                    "Confirmar cambio",
                    javax.swing.JOptionPane.YES_NO_OPTION,
                    javax.swing.JOptionPane.WARNING_MESSAGE);

            if (respuesta != javax.swing.JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            for (int i = 0; i < tblAsistencia.getRowCount(); i++) {
                int idTrabajador = idsTrabajadoresEnTabla.get(i);

                boolean asistio = (boolean) tblAsistencia.getValueAt(i, 3);
                int horasE = Integer.parseInt(tblAsistencia.getValueAt(i, 4).toString());

                String obs = (tblAsistencia.getValueAt(i, 6) != null) ? tblAsistencia.getValueAt(i, 6).toString() : "";

                double montoAnticipo = 0;
                try {
                    Object valorAnt = tblAsistencia.getValueAt(i, 5);
                    if (valorAnt != null && !valorAnt.toString().isEmpty()) {
                        montoAnticipo = Double.parseDouble(valorAnt.toString());
                    }
                } catch (Exception e) {
                    montoAnticipo = 0;
                }

                if (montoAnticipo > 0) {
                    gestormodelo.anticipo ant = new gestormodelo.anticipo();
                    ant.setIdTrabajador(idTrabajador);
                    ant.setFecha(fechaSQL);
                    ant.setMonto(montoAnticipo);
                    ant.setDetalle(obs);

                    antDAO.guardarOActualizar(ant);
                } else {
                    antDAO.eliminarSiEsCero(idTrabajador, fechaSQL);
                }

                gestormodelo.asistencia asis = new gestormodelo.asistencia();
                asis.setIdTrabajador(idTrabajador);
                asis.setFecha(fechaSQL);
                asis.setEstado(asistio ? "ASISTIO" : "FALTA");
                asis.setHorasExtras(horasE);
                asis.setObservacion(obs);

                asisDAO.guardarOActualizar(asis);
            }

            javax.swing.JOptionPane.showMessageDialog(this, "¡Registros actualizados correctamente!");

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            e.printStackTrace();
        }
    }// GEN-LAST:event_jButton1ActionPerformed

    private void todosasistenActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_todosasistenActionPerformed
        int filas = tblAsistencia.getRowCount();
        for (int i = 0; i < filas; i++) {
            tblAsistencia.setValueAt(true, i, 3);
        }
    }// GEN-LAST:event_todosasistenActionPerformed

    private void cbxarea_asisteItemStateChanged(java.awt.event.ItemEvent evt) {// GEN-FIRST:event_cbxarea_asisteItemStateChanged
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            String seleccion = cbxarea_asiste.getSelectedItem().toString();
            cargarTablaAsistencia(seleccion);
        }
    }// GEN-LAST:event_cbxarea_asisteItemStateChanged

    private void cbxarea_asisteActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cbxarea_asisteActionPerformed

    }// GEN-LAST:event_cbxarea_asisteActionPerformed

    private void CalcularActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_CalcularActionPerformed
        actualizarGraficaNomina();

        if (dcInicio.getDate() == null || dcFin.getDate() == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Por favor, selecciona el rango de fechas (Inicio y Fin)");
            return;
        }

        java.sql.Date fechaInicio = new java.sql.Date(dcInicio.getDate().getTime());
        java.sql.Date fechaFin = new java.sql.Date(dcFin.getDate().getTime());

        gestordatos.trabajadorDAO dao = new gestordatos.trabajadorDAO();
        List<Object[]> reporte = dao.obtenerReporteNomina(fechaInicio, fechaFin,
                cbxarea_nomina.getSelectedItem().toString());

        DefaultTableModel modelo = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        modelo.addColumn("Nombre");
        modelo.addColumn("Cargo");
        modelo.addColumn("Sueldo D.");
        modelo.addColumn("Días");
        modelo.addColumn("H. Extra");
        modelo.addColumn("Total Ganado");
        modelo.addColumn("Anticipos");
        modelo.addColumn("Neto a Pagar");

        if (reporte.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "No hay datos de asistencia en este rango.");
        } else {
            for (Object[] fila : reporte) {
                modelo.addRow(fila);
            }
        }

        tabla_anticipo.setModel(modelo);

        configurarAnchoColumnasNomina();
    }// GEN-LAST:event_CalcularActionPerformed

    private void nombre_obra1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_nombre_obra1ActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_nombre_obra1ActionPerformed

    private void jdFechaRegistroPropertyChange(java.beans.PropertyChangeEvent evt) {// GEN-FIRST:event_jdFechaRegistroPropertyChange
        if ("date".equals(evt.getPropertyName())) {
            if (jdFechaRegistro.getDate() != null) {
                String areaActual = cbxarea_asiste.getSelectedItem().toString();

                cargarTablaAsistencia(areaActual);

                System.out.println("Tabla refrescada para la fecha: " + jdFechaRegistro.getDate());
            }
        }

    }// GEN-LAST:event_jdFechaRegistroPropertyChange

    private void tabla_areaMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_tabla_areaMouseClicked
        int fila = tabla_area.getSelectedRow();
        if (fila != -1) {
            nombreAreaParaEditar = tabla_area.getValueAt(fila, 1).toString();
            nom_area.setText(nombreAreaParaEditar);
            guardar_area.setText("Actualizar");
        }
    }// GEN-LAST:event_tabla_areaMouseClicked

    private void guardar_areaActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_guardar_areaActionPerformed
        String nuevoNombre = nom_area.getText().trim();
        cargoDAO dao = new cargoDAO();

        if (nombreAreaParaEditar.equals("")) {
            if (dao.insertar(nuevoNombre)) {
                JOptionPane.showMessageDialog(this, "Área guardada");
            }
        } else {
            if (dao.editar(nombreAreaParaEditar, nuevoNombre)) {
                JOptionPane.showMessageDialog(this, "Área actualizada");
                nombreAreaParaEditar = "";
                guardar_area.setText("Guardar");
            }
        }
        nom_area.setText("");
        listarAreas();
        cargarComboAreasConfig();
        cargarComboAsistencia();
        cancelar_areaActionPerformed(null);
    }// GEN-LAST:event_guardar_areaActionPerformed

    private void cancelar_areaActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cancelar_areaActionPerformed
        nom_area.setText("");
        nombreAreaParaEditar = "";
        guardar_area.setText("Guardar");
        tabla_area.clearSelection();
    }// GEN-LAST:event_cancelar_areaActionPerformed

    private void cbxarea_cargoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cbxarea_cargoActionPerformed
        listarCargos();
    }// GEN-LAST:event_cbxarea_cargoActionPerformed

    private void guardar_cargoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_guardar_cargoActionPerformed

        try {
            if (cbxarea_cargo.getSelectedItem() == null || cbxarea_cargo.getSelectedItem().toString().equals("TODOS")) {
                JOptionPane.showMessageDialog(this, "Seleccione un área válida para el cargo.");
                return;
            }
            if (nom_cargo.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre del cargo no puede estar vacío.");
                return;
            }

            gestormodelo.cargo cargoObj = new gestormodelo.cargo();
            cargoObj.setNombre(nom_cargo.getText().trim());
            cargoObj.setArea(cbxarea_cargo.getSelectedItem().toString());

            String s = JOptionPane.showInputDialog(this, "Sueldo diario para " + cargoObj.getNombre() + ":",
                    guardar_cargo.getText().equals("Actualizar") ? "Editando" : "Nuevo", JOptionPane.QUESTION_MESSAGE);

            if (s == null || s.isEmpty()) {
                return;
            }
            cargoObj.setSueldo(Double.parseDouble(s));
            cargoObj.setTopeAnticipo(cargoObj.getSueldo() * 0.5);

            cargoDAO dao = new cargoDAO();

            if (guardar_cargo.getText().equals("Actualizar")) {
                cargoObj.setId(idCargoSeleccionado);

                if (dao.actualizar(cargoObj)) {
                    JOptionPane.showMessageDialog(this, "Cargo actualizado con éxito.");
                }
            } else {
                if (dao.registrar(cargoObj)) {
                    JOptionPane.showMessageDialog(this, "Nuevo cargo registrado.");
                }
            }

            limpiarFormularioCargo();
            listarCargos();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error: El sueldo debe ser un número válido.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }// GEN-LAST:event_guardar_cargoActionPerformed

    private void cbxarea_cargoItemStateChanged(java.awt.event.ItemEvent evt) {// GEN-FIRST:event_cbxarea_cargoItemStateChanged
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            listarCargos();
        }
    }// GEN-LAST:event_cbxarea_cargoItemStateChanged

    private void tabla_gestion_personalMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_tabla_gestion_personalMouseClicked
        int fila = tabla_gestion_personal.getSelectedRow();
        if (fila != -1) {
            idTrabajadorSeleccionado = Integer.parseInt(tabla_gestion_personal.getValueAt(fila, 0).toString());

            trabajadorDAO dao = new trabajadorDAO();
            List<gestormodelo.trabajador> listaTotal = dao.listarTodos();

            gestormodelo.trabajador seleccionado = listaTotal.stream()
                    .filter(t -> t.getId() == idTrabajadorSeleccionado)
                    .findFirst()
                    .orElse(null);

            if (seleccionado != null) {
                cbx_area_emp.setSelectedItem(seleccionado.getArea());

                txt_nombre_emp.setText(seleccionado.getNombre());
                txt_cedula_emp.setText(seleccionado.getCedula());
                chk_activo_emp.setSelected(seleccionado.isActivo());

                cbx_cargo_emp.setSelectedItem(seleccionado.getCargoNombre());
            }

            jButton2.setText("Actualizar Datos");
        }
    }// GEN-LAST:event_tabla_gestion_personalMouseClicked

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton2ActionPerformed
        if (txt_nombre_emp.getText().isEmpty() || cbx_cargo_emp.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Nombre y Cargo son obligatorios");
            return;
        }

        String area = cbx_area_emp.getSelectedItem().toString();
        String cargoNom = cbx_cargo_emp.getSelectedItem().toString();

        cargoDAO cDAO = new cargoDAO();
        int idCargo = cDAO.obtenerIdPorNombreYArea(cargoNom, area);

        gestormodelo.trabajador t = new gestormodelo.trabajador();
        t.setNombre(txt_nombre_emp.getText().trim());
        t.setCedula(txt_cedula_emp.getText().trim());
        t.setIdCargo(idCargo);
        t.setActivo(chk_activo_emp.isSelected());

        trabajadorDAO tDAO = new trabajadorDAO();

        if (idTrabajadorSeleccionado == -1) {
            if (tDAO.registrar(t)) {
                JOptionPane.showMessageDialog(this, "Trabajador registrado");
                limpiarFormularioTrabajador();
            }
        } else {
            t.setId(idTrabajadorSeleccionado);
            if (tDAO.actualizar(t)) {
                JOptionPane.showMessageDialog(this, "Datos actualizados correctamente");
                limpiarFormularioTrabajador();
            }
        }

        filtrarTablaPersonalPorArea(area);
    }// GEN-LAST:event_jButton2ActionPerformed

    private void cbx_area_empItemStateChanged(java.awt.event.ItemEvent evt) {// GEN-FIRST:event_cbx_area_empItemStateChanged
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            String area = cbx_area_emp.getSelectedItem().toString();

            cargoDAO dao = new cargoDAO();
            List<gestormodelo.cargo> listaCargos = dao.listarCargosPorArea(area);

            cbx_cargo_emp.removeAllItems();
            for (gestormodelo.cargo c : listaCargos) {
                cbx_cargo_emp.addItem(c.getNombre());
            }

            filtrarTablaPersonalPorArea(area);
        }

    }// GEN-LAST:event_cbx_area_empItemStateChanged

    private void cbx_area_empActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cbx_area_empActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_cbx_area_empActionPerformed

    private void cancelar_empActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cancelar_empActionPerformed
        limpiarFormularioTrabajador();

    }// GEN-LAST:event_cancelar_empActionPerformed

    private void excel_pagosActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_excel_pagosActionPerformed
        if (tabla_anticipo.getRowCount() > 0) {
            exportarExcel(tabla_anticipo, nombreObra);
        } else {
            JOptionPane.showMessageDialog(this, "No hay datos en la tabla para exportar. Presione 'Calcular' primero.");
        }
    }// GEN-LAST:event_excel_pagosActionPerformed

    private void excel_asistenciaActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_excel_asistenciaActionPerformed
        if (tblAsistencia.getRowCount() > 0) {
            exportarExcel(tblAsistencia, nombreObra);
        } else {
            JOptionPane.showMessageDialog(this, "No hay datos en la tabla para exportar. Presione 'Calcular' primero.");
        }
    }// GEN-LAST:event_excel_asistenciaActionPerformed

    private void excel_trabajadoresActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_excel_trabajadoresActionPerformed
        if (tabla_gestion_personal.getRowCount() > 0) {
            exportarExcel(tabla_gestion_personal, nombreObra);
        } else {
            JOptionPane.showMessageDialog(this, "No hay datos en la tabla para exportar. Presione 'Calcular' primero.");
        }
    }// GEN-LAST:event_excel_trabajadoresActionPerformed

    private void excel_cargosActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_excel_cargosActionPerformed
        if (lista_cargos.getRowCount() > 0) {
            exportarExcel(lista_cargos, nombreObra);
        } else {
            JOptionPane.showMessageDialog(this, "No hay datos en la tabla para exportar. Presione 'Calcular' primero.");
        }
    }// GEN-LAST:event_excel_cargosActionPerformed

    private void chkAsistenciasActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_chkAsistenciasActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_chkAsistenciasActionPerformed

    private void chkAsistenciasItemStateChanged(java.awt.event.ItemEvent evt) {// GEN-FIRST:event_chkAsistenciasItemStateChanged
        actualizarGraficaNomina();
    }// GEN-LAST:event_chkAsistenciasItemStateChanged

    private void chkHorasItemStateChanged(java.awt.event.ItemEvent evt) {// GEN-FIRST:event_chkHorasItemStateChanged
        actualizarGraficaNomina();
    }// GEN-LAST:event_chkHorasItemStateChanged

    private void cbxarea_nominaActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cbxarea_nominaActionPerformed
    }// GEN-LAST:event_cbxarea_nominaActionPerformed

    private void lista_cargosMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_lista_cargosMouseClicked
        int fila = lista_cargos.getSelectedRow();
        if (fila != -1) {
            idCargoSeleccionado = Integer.parseInt(lista_cargos.getValueAt(fila, 0).toString());

            nom_cargo.setText(lista_cargos.getValueAt(fila, 1).toString());
            cbxarea_cargo.setSelectedItem(lista_cargos.getValueAt(fila, 2).toString());

            guardar_cargo.setText("Actualizar");
        }
    }// GEN-LAST:event_lista_cargosMouseClicked

    private void cancelar_cargoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cancelar_cargoActionPerformed
        limpiarFormularioCargo();
    }// GEN-LAST:event_cancelar_cargoActionPerformed

    private void chk_activo_empActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_chk_activo_empActionPerformed
    }// GEN-LAST:event_chk_activo_empActionPerformed

    private void cbx_area_empMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_cbx_area_empMouseClicked

    }// GEN-LAST:event_cbx_area_empMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        // <editor-fold defaultstate="collapsed" desc=" Look and feel setting code
        // (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the default
         * look and feel.
         * For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        // </editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new Interface1().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Calcular;
    private javax.swing.JButton cancelar_area;
    private javax.swing.JButton cancelar_cargo;
    private javax.swing.JButton cancelar_emp;
    private javax.swing.JComboBox<String> cbx_area_emp;
    private javax.swing.JComboBox<String> cbx_cargo_emp;
    private javax.swing.JComboBox<String> cbxarea_asiste;
    private javax.swing.JComboBox<String> cbxarea_cargo;
    private javax.swing.JComboBox<String> cbxarea_nomina;
    private javax.swing.JCheckBox chkAsistencias;
    private javax.swing.JCheckBox chkHoras;
    private javax.swing.JCheckBox chk_activo_emp;
    private com.toedter.calendar.JDateChooser dcFin;
    private com.toedter.calendar.JDateChooser dcInicio;
    private javax.swing.JButton excel_asistencia;
    private javax.swing.JButton excel_cargos;
    private javax.swing.JButton excel_pagos;
    private javax.swing.JButton excel_trabajadores;
    private javax.swing.JInternalFrame grafica_asistencia;
    private javax.swing.JButton guardar_area;
    private javax.swing.JButton guardar_cargo;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JInternalFrame jInternalFrame1;
    private javax.swing.JInternalFrame jInternalFrame2;
    private javax.swing.JInternalFrame jInternalFrame3;
    private javax.swing.JInternalFrame jInternalFrame4;
    private javax.swing.JInternalFrame jInternalFrame5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JTabbedPane jTabbedPane1;
    private com.toedter.calendar.JDateChooser jdFechaRegistro;
    private javax.swing.JTable lista_cargos;
    private javax.swing.JTextField nom_area;
    private javax.swing.JTextField nom_cargo;
    private javax.swing.JTextField nombre_obra1;
    private javax.swing.JTextField nombre_obra2;
    private javax.swing.JTable tabla_anticipo;
    private javax.swing.JTable tabla_area;
    private javax.swing.JTable tabla_gestion_personal;
    private javax.swing.JTable tblAsistencia;
    private javax.swing.JButton todosasisten;
    private javax.swing.JTextField txt_cedula_emp;
    private javax.swing.JTextField txt_nombre_emp;
    // End of variables declaration//GEN-END:variables
}
