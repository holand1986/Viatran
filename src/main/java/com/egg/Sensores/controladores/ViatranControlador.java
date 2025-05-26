package com.egg.Sensores.controladores;

import com.egg.Sensores.entidades.Instrumentos;
import com.egg.Sensores.entidades.Viatran;
import com.egg.Sensores.excepciones.MiException;
import com.egg.Sensores.repositorios.InstrumentosRepositorio;
import com.egg.Sensores.repositorios.ViatranRepositorio;
import com.egg.Sensores.servicios.InstrumentosServicio;
import com.egg.Sensores.servicios.ViatranServicio;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/viatran")
public class ViatranControlador {

    @Autowired
    private ViatranServicio viatranServicio;
    @Autowired
    private InstrumentosServicio instrumentosservicio;
    @Autowired
    private ViatranRepositorio viatranrepositorio; //instancio para utilizar los metodos
    @Autowired
    private InstrumentosRepositorio instrumentorepositorio;

    //registro de un nuevo sensor. Contempla la lista de modelos de sensores
    @GetMapping("/registrar") //localhost:8080/viatran/registrar
    public String registrar(ModelMap modelo) {   //model map permite listar 

        List<Instrumentos> instrumentos = instrumentosservicio.listarInstrumentos();  //instrumetos tiene toda la lista, ahora lo debo enviar al html
        modelo.addAttribute("instrumentos", instrumentos);//con este atibuto puedo ver la lista en el html

        return "viatran_form.html"; //levanto el formulario html

    }

    @PostMapping("/registro")
    public String registro(@RequestParam Integer Serial,
            @RequestParam String idSensor,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaalta,
            @RequestParam(required = false) Integer certificado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechacal,
            @RequestParam(required = false) String servicio,
            @RequestParam String estado,
            @RequestParam Integer periodo,
            //  @RequestParam(required = false) String rutaCertificado,
            @RequestParam(required = false) MultipartFile certificadoPdf,
            ModelMap modelo) throws IOException {

        try {
            byte[] pdfBytes = null;
            if (certificadoPdf != null && !certificadoPdf.isEmpty()) {
                long maxSizeBytes = 10 * 1024 * 1024; // 10 MB

                if (certificadoPdf.getSize() > maxSizeBytes) {
                    throw new MiException("El archivo excede el tamaño máximo permitido de 10 MB.");
                }

                pdfBytes = certificadoPdf.getBytes();
            }

            viatranServicio.crearPresion(Serial, idSensor, fechaalta, certificado, fechacal, servicio, estado, periodo, pdfBytes);
            modelo.put("exito", "El sensor fue cargado correctamente!");
            // redirectAttributes.addFlashAttribute("exito", "El sensor fue cargado correctamente!");
            return "viatran_form.html";

        } catch (MiException e) {
            // Reenviás la vista con el mensaje de error
            modelo.put("error", e.getMessage());

            // ⚠️ Volvés a cargar los datos necesarios para que el formulario funcione bien
            List<Instrumentos> instrumentos = instrumentosservicio.listarInstrumentos();  //instrumetos tiene toda la lista, ahora lo debo enviar al html
            modelo.addAttribute("instrumentos", instrumentos);

            return "viatran_form.html"; //vuelvo a cargar el formulario
        }

    }

    //listado de todos los sensores
    // @GetMapping("/lista")
    // public String listar(ModelMap modelo) {
    //     List<Viatran> viatrans = viatranServicio.listarViatran();
    //     modelo.addAttribute("viatrans",viatrans); //paso la lista viatrans al html
    //     return "viatran_list.html";
    //  }
    // @GetMapping("/lista")
    //    public String listar(ModelMap modelo) {
    //       List<Viatran> viatrans = viatranServicio.listarActivos();
    //       modelo.put("viatrans", viatrans);
    //      return "viatran_list.html";
    //     }
    @GetMapping("/lista")
    public String listarViatrans(
            @RequestParam(required = false) Integer serial,
            @RequestParam(required = false) String modelo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechacal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechavenc,
            @RequestParam(required = false) String servicio,
            @RequestParam(required = false) Integer certificado,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Boolean inactivos, // <-- nuevo parámetro
            ModelMap modeloMap) {

        // Limpiar strings vacíos
        if (modelo != null && modelo.trim().isEmpty()) {
            modelo = null;
        }
        if (servicio != null && servicio.trim().isEmpty()) {
            servicio = null;
        }
        if (estado != null && estado.trim().isEmpty()) {
            estado = null;
        }

        List<Viatran> viatrans;

        if (Boolean.TRUE.equals(inactivos)) {
            viatrans = viatranServicio.listarInactivos();

        } else if (serial != null || modelo != null || fechavenc != null || fechacal != null || servicio != null || certificado != null || estado != null) {
            viatrans = viatranServicio.filtrar(serial, modelo, fechavenc, fechacal, servicio, certificado, estado);
            System.out.println("la lista es:" + viatrans);
        } else {
            viatrans = viatranServicio.listarActivos(); // o listarTodos() si lo prefieres

        }

        modeloMap.put("activosPorServicio", viatranServicio.contarActivosPorServicio());
        modeloMap.put("totalInactivos", viatranServicio.contarInactivos());

        // Para que se mantengan los valores en el navbar luego del submit
        modeloMap.put("viatrans", viatrans);
        modeloMap.put("serial", serial);
        modeloMap.put("modelo", modelo);
        modeloMap.put("fechavenc", fechavenc);
        modeloMap.put("fechacal", fechacal);
        modeloMap.put("servicio", servicio);
        modeloMap.put("certificado", certificado);
        modeloMap.put("estado", estado);

        return "viatran_list";
    }

    /*   @GetMapping("/descargar/{serial}")
    public ResponseEntity<Resource> descargarCertificado(@PathVariable Integer serial) {
        System.out.println("🔍 Intentando descargar certificado para serial: " + serial);

        Viatran viatran = viatranrepositorio.findById(serial).orElse(null);
        if (viatran == null || viatran.getLink() == null) {
            System.out.println("⚠️ No se encontró el registro en la base de datos o el link es nulo.");
            return ResponseEntity.notFound().build();
        }

        Path path = Paths.get(viatran.getLink());
        System.out.println("📂 Ruta completa del archivo: " + path.toString());

        if (!Files.exists(path)) {
            System.out.println("⚠️ No se encontró el archivo en la ruta: " + path.toString());
            return ResponseEntity.notFound().build();
        }

        try {
            Resource recurso = new UrlResource(path.toUri());
            System.out.println("✅ Archivo encontrado y listo para descargar: " + recurso.getFilename());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + recurso.getFilename() + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(recurso);
        } catch (Exception e) {
            System.out.println("❌ Error al cargar el archivo: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/qr/{serial}")
    public void generarQR(@PathVariable Integer serial, HttpServletResponse response) {
        String linkDescarga = "http://localhost:8080/descargar/" + serial;  // URL dinámica
        int size = 250;  // Tamaño del código QR

        try {
            // Generar el código QR
            BitMatrix matrix = new QRCodeWriter().encode(linkDescarga, BarcodeFormat.QR_CODE, size, size);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);

            // Configurar la respuesta como imagen PNG
            response.setContentType("image/png");
            try (OutputStream os = response.getOutputStream()) {
                ImageIO.write(image, "png", os);  // Escribir la imagen en el output stream
                os.flush();
            }
        } catch (Exception e) {
            // Si ocurre algún error, se pueden manejar las excepciones aquí
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);  // Configurar código de error
        }
    } */
    @GetMapping("/descargar/{serial}")
    public ResponseEntity<byte[]> descargarCertificado(@PathVariable Integer serial) {
        System.out.println("🔍 Intentando descargar certificado desde base de datos para serial: " + serial);

        Viatran viatran = viatranrepositorio.findById(serial).orElse(null);
        if (viatran == null || viatran.getCertificadoPdf() == null) {
            System.out.println("⚠️ No se encontró el certificado en la base de datos.");
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"certificado_" + serial + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(viatran.getCertificadoPdf());
    }

    /*  @GetMapping("/qr/{serial}")
    public void generarQR(@PathVariable Integer serial, HttpServletResponse response) {
        String linkDescarga = "http://localhost:8080/descargar/" + serial;  // Asegúrate de usar el dominio correcto en producción
        int size = 250;

        try {
            BitMatrix matrix = new QRCodeWriter().encode(linkDescarga, BarcodeFormat.QR_CODE, size, size);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);

            response.setContentType("image/png");
            try (OutputStream os = response.getOutputStream()) {
                ImageIO.write(image, "png", os);
                os.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    } */
    @GetMapping("/qr/{serial}")
    public void generarQR(@PathVariable Integer serial, HttpServletRequest request, HttpServletResponse response) {
        int size = 250;

        try {
            // Construye la URL dinámica
            String baseUrl = request.getRequestURL().toString().replace(request.getRequestURI(), request.getContextPath());
            String linkDescarga = baseUrl + "/descargar/" + serial;

            BitMatrix matrix = new QRCodeWriter().encode(linkDescarga, BarcodeFormat.QR_CODE, size, size);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);

            response.setContentType("image/png");
            try (OutputStream os = response.getOutputStream()) {
                ImageIO.write(image, "png", os);
                os.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/modificar/{serial}")  //recibo el serial desde el html
    public String modificar(@PathVariable Integer serial, ModelMap modelo) {
        modelo.put("viatran", viatranServicio.getOne(serial));
        System.out.println("serial get " + viatranServicio.getOne(serial));

        List<Instrumentos> instrumentos = instrumentosservicio.listarInstrumentos();  //instrumetos tiene toda la lista, ahora lo debo enviar al html
        modelo.addAttribute("instrumentos", instrumentos);//con este atibuto puedo ver la lista en el html

        return "viatran_modificar.html";
    }

    @PostMapping("/modificar/{serial}")
    public String modificarViatran(@PathVariable Integer serial,
            @RequestParam String idSensor,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaalta,
            @RequestParam(required = false) Integer certificado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechacal,
            @RequestParam String servicio,
            @RequestParam String estado,
            @RequestParam Integer periodo,
            //@RequestParam(required = false) String rutaCertificado,
            @RequestParam(required = false) MultipartFile certificadoPdf, // ✅ archivo recibido
            ModelMap modelo) throws IOException {

        try {
            byte[] pdfBytes = null;
            if (certificadoPdf != null && !certificadoPdf.isEmpty()) {
                long maxSizeBytes = 2 * 1024 * 1024; // 2 MB

                if (certificadoPdf.getSize() > maxSizeBytes) {
                    throw new MiException("El archivo excede el tamaño máximo permitido de 2 MB");
                }

                pdfBytes = certificadoPdf.getBytes();
            }

            viatranServicio.modificarPresion(serial, idSensor, fechaalta, certificado, fechacal, servicio, estado, periodo, pdfBytes);
            return "redirect:../lista"; // o donde quieras redirigir al usuario

        } catch (MiException e) {
            // Reenviás la vista con el mensaje de error
            modelo.put("error", e.getMessage());

            // ⚠️ Volvés a cargar los datos necesarios para que el formulario funcione bien
            List<Instrumentos> instrumentos = instrumentosservicio.listarInstrumentos();  //instrumetos tiene toda la lista, ahora lo debo enviar al html
            modelo.addAttribute("instrumentos", instrumentos);
            modelo.put("viatran", viatranServicio.getOne(serial));

            return "viatran_modificar.html"; // mostrás la misma vista con el error
        }
    }

    @GetMapping("/eliminar/{serial}")
    public String eliminarSoft(@PathVariable Integer serial, ModelMap modelo) {
        try {
            viatranServicio.softDelete(serial);
            modelo.put("exito", "El sensor fue eliminado correctamente");
        } catch (MiException e) {
            modelo.put("error", e.getMessage());
        }

        return "redirect:/viatran/lista";
    }

    @GetMapping("/alta/{serial}")
    public String alta(@PathVariable Integer serial, ModelMap modelo) {
        try {
            viatranServicio.softAlta(serial);
            modelo.put("exito", "El sensor fue dado de alta correctamente");
        } catch (MiException e) {
            modelo.put("error", e.getMessage());
        }

        return "redirect:/viatran/lista";
    }

    @GetMapping("/eliminar-definitivo/{serial}")
    public String eliminarHard(@PathVariable Integer serial, ModelMap modelo) {
        try {
            viatranServicio.hardDelete(serial);
            modelo.put("exito", "El sensor fue eliminado correctamente");
        } catch (MiException e) {
            modelo.put("error", e.getMessage());
        }

        return "redirect:/viatran/lista";
    }

    //manejo de archivo excel
    @GetMapping("/exportar-excel")
    public void exportarExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=viatran_lista.xlsx";
        response.setHeader(headerKey, headerValue);

        List<Viatran> listaViatran = viatranServicio.listarViatran(); // asegurate de tener este método
        exportarAExcel(listaViatran, response);
    }

    private void exportarAExcel(List<Viatran> lista, HttpServletResponse response) throws IOException {

        // Cabecera
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Sensores");

        String[] headers = {"Serial", "Modelo", "Fecha Alta", "N°cert", "Fecha cal", "Fecha Venc", "Servicio", "Estado"};

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // Cuerpo
        int rowNum = 1;
        for (Viatran v : lista) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(v.getSerial());
            row.createCell(1).setCellValue(v.getModelo().getModelo());
            row.createCell(2).setCellValue(String.valueOf(v.getFechaalta()));
            row.createCell(3).setCellValue(String.valueOf(v.getCertificado()));
            row.createCell(4).setCellValue(String.valueOf(v.getFechacal()));
            row.createCell(5).setCellValue(String.valueOf(v.getFechavenc()));
            row.createCell(6).setCellValue(v.getServicio());
            row.createCell(7).setCellValue(v.getEstado());
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

}
