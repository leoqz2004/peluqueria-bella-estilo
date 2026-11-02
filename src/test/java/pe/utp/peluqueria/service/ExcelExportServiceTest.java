package pe.utp.peluqueria.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.*;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import pe.utp.peluqueria.model.EstadoCita;

class ExcelExportServiceTest {
    @Test
    void generaLibroConTresHojas() throws Exception {
        Map<EstadoCita, Long> estados = new EnumMap<>(EstadoCita.class);
        estados.put(EstadoCita.ATENDIDA, 7L); estados.put(EstadoCita.CANCELADA, 2L);
        List<Object[]> ranking = List.<Object[]>of(new Object[]{"=CMD|' /C calc'!A0", 5L});   // intento de inyeccion
        Map<String, Long> minutos = new TreeMap<>(Map.of("Milagros Ramos", 240L));

        byte[] bytes = new ExcelExportService().reporteGestion(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), estados, ranking, minutos);

        try (var libro = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertEquals(3, libro.getNumberOfSheets());
            Sheet citas = libro.getSheet("Citas por estado");
            assertEquals("ATENDIDA", citas.getRow(3).getCell(0).getStringCellValue());
            assertEquals(7.0, citas.getRow(3).getCell(1).getNumericCellValue());
            Sheet servicios = libro.getSheet("Servicios solicitados");
            assertEquals(CellType.STRING, servicios.getRow(1).getCell(0).getCellType(), "se guarda como texto, nunca como formula");
            assertEquals("Milagros Ramos", libro.getSheet("Minutos por estilista").getRow(1).getCell(0).getStringCellValue());
        }
    }
}
