package pe.utp.peluqueria.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import pe.utp.peluqueria.model.EstadoCita;

/**
 * Genera el reporte de gestion en formato Excel (.xlsx) con Apache POI.
 * Seguridad: todos los textos se escriben con {@code setCellValue(String)}, es decir como TEXTO y nunca
 * como formula, por lo que un nombre de servicio malicioso no puede inyectar formulas (CSV/Excel injection).
 */
@Service
public class ExcelExportService {

    /**
     * @param desde             inicio del periodo
     * @param hasta             fin del periodo
     * @param porEstado         citas por estado (R1)
     * @param ranking           filas {nombre servicio, cantidad} de servicios mas solicitados (R2)
     * @param minutosEstilista  minutos atendidos por estilista (R3)
     * @return bytes del archivo .xlsx
     */
    public byte[] reporteGestion(LocalDate desde, LocalDate hasta, Map<EstadoCita, Long> porEstado,
                                 List<Object[]> ranking, Map<String, Long> minutosEstilista) {
        try (Workbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            CellStyle cabecera = estiloCabecera(libro);

            Sheet citas = libro.createSheet("Citas por estado");
            fila(citas, 0, cabecera, "Periodo", desde + " a " + hasta);
            fila(citas, 2, cabecera, "Estado", "Cantidad");
            int r = 3;
            for (Map.Entry<EstadoCita, Long> e : porEstado.entrySet()) fila(citas, r++, null, e.getKey().name(), e.getValue());
            ajustar(citas, 2);

            Sheet servicios = libro.createSheet("Servicios solicitados");
            fila(servicios, 0, cabecera, "Servicio", "Veces solicitado");
            r = 1;
            for (Object[] f : ranking) fila(servicios, r++, null, String.valueOf(f[0]), f[1]);
            ajustar(servicios, 2);

            Sheet minutos = libro.createSheet("Minutos por estilista");
            fila(minutos, 0, cabecera, "Estilista", "Minutos atendidos");
            r = 1;
            for (Map.Entry<String, Long> e : minutosEstilista.entrySet()) fila(minutos, r++, null, e.getKey(), e.getValue());
            ajustar(minutos, 2);

            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo generar el reporte Excel", ex);
        }
    }

    private static CellStyle estiloCabecera(Workbook libro) {
        Font negrita = libro.createFont();
        negrita.setBold(true);
        CellStyle estilo = libro.createCellStyle();
        estilo.setFont(negrita);
        estilo.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return estilo;
    }

    private static void fila(Sheet hoja, int n, CellStyle estilo, String a, Object b) {
        Row fila = hoja.createRow(n);
        Cell c0 = fila.createCell(0);
        c0.setCellValue(a);
        Cell c1 = fila.createCell(1);
        if (b instanceof Number num) c1.setCellValue(num.doubleValue()); else c1.setCellValue(String.valueOf(b));
        if (estilo != null) { c0.setCellStyle(estilo); c1.setCellStyle(estilo); }
    }

    private static void ajustar(Sheet hoja, int columnas) {
        for (int i = 0; i < columnas; i++) hoja.autoSizeColumn(i);
    }
}
