package com.egg.Sensores.servicios;

import com.egg.Sensores.entidades.Viatran;
import com.egg.Sensores.entidades.Instrumentos;
import com.egg.Sensores.excepciones.MiException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.egg.Sensores.repositorios.ViatranRepositorio;
import com.egg.Sensores.repositorios.InstrumentosRepositorio;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ViatranServicio {

    @Autowired
    private ViatranRepositorio viatranrepositorio; //instancio para utilizar los metodos

    @Autowired
    private InstrumentosRepositorio instrumentosrepositorio; //instancio para utilizar los metodos de sensores

    @Transactional  //los cambios en la base de datos son transaccionales, si hay un error se debe volver para atras
    public void crearPresion(Integer serial, String idSensor, LocalDate fechaalta, Integer certificado,
            LocalDate fechacal, String servicio, String estado, Integer periodo, byte[] certificadoPdf) throws MiException, IOException {

        validar(serial, idSensor, fechaalta, certificado, fechacal, servicio, estado, periodo, true);
        // Calcular la fecha de vencimiento sumando 12 meses
        LocalDate fechavenc = null;  // Primero se declara fuera

        if (fechacal != null) {
            fechavenc = fechacal.plusMonths(periodo);
        }

        //busco el sensor
        Instrumentos instrumentos = instrumentosrepositorio.findById(idSensor).get();

        // Crear y configurar el objeto Viatran
        Viatran viatran = new Viatran();
        viatran.setSerial(serial);
        viatran.setModelo(instrumentos);
        viatran.setFechaalta(fechaalta); //usar fecha actual si alta es nula
        viatran.setCertificado(certificado);
        viatran.setFechacal(fechacal);
        viatran.setFechavenc(fechavenc); // Asignar fecha de vencimiento calculada
        viatran.setServicio(servicio);
        viatran.setEstado(estado);
        viatran.setPeriodo(periodo);
        viatran.setCertificadoPdf(certificadoPdf);  // Guarda la ruta en la base de datos

        // 📌 Crear la carpeta si no existe
        //  String carpetaBase = "C:/mis_certificados/";
        //  File carpeta = new File(carpetaBase);
        //  if (!carpeta.exists()) {
        //      carpeta.mkdirs(); // Crea la carpeta si no existe
        //      System.out.println("📂 Carpeta creada en: " + carpetaBase);
        //  }
        // 📌 Guardar la ruta en la base de datos
        //  if (rutaCertificado != null && !rutaCertificado.trim().isEmpty()) {
        //      String rutaCompleta = carpetaBase + rutaCertificado;
        //      viatran.setLink(rutaCompleta);
        //  } else {
        //      viatran.setLink(null); // o lo dejás como está si no querés modificarlo
        //  }
        viatranrepositorio.save(viatran);
    }
    //metodo para crear una lista de sensores de presion     

    public List<Viatran> listarViatran() {

        List<Viatran> presion = new ArrayList();
        presion = viatranrepositorio.findAll();
        return presion;
    }

    public List<Viatran> listarActivos() {
        return viatranrepositorio.findByActivoTrue();
    }

    public List<Viatran> listarInactivos() {
        return viatranrepositorio.findByActivoFalse();
    }

    public List<Viatran> filtrar(Integer serial, String modelo, LocalDate fechavenc, LocalDate fechacal, String servicio,
            Integer certificado, String estado) {
        System.out.println("el modelo es: " + modelo + " el serial es: " + serial + " la fechacal es: " + fechacal + " la fechavenc es: " + fechavenc + " el servicio es: " + servicio + " el certificado es: " + certificado + " el estado es: " + estado);
        return viatranrepositorio.filtrarViatrans(serial, modelo, fechavenc, fechacal, servicio, certificado, estado);

    }

    //metodo para modificar un sensor de presion
    public void modificarPresion(Integer serial, String idSensor, LocalDate fechaalta, Integer certificado, LocalDate fechacal,
            String servicio, String estado, Integer periodo, byte[] certificadoPdf) throws MiException {

        validar(serial, idSensor, fechaalta, certificado, fechacal, servicio, estado, periodo, false);

        Optional<Viatran> respuesta = viatranrepositorio.findById(serial);
        Optional<Instrumentos> respuestaInstrumento = instrumentosrepositorio.findById(idSensor);

        Instrumentos instrumentos = new Instrumentos();

        if (respuestaInstrumento.isPresent()) {
            instrumentos = respuestaInstrumento.get();
        }

        // Calcular la fecha de vencimiento sumando 6 meses     
        LocalDate fechavenc = fechacal.plusYears(1);

        if (respuesta.isPresent()) {

            Viatran viatran = respuesta.get();
            viatran.setModelo(instrumentos);
            viatran.setFechaalta(fechaalta);
            viatran.setCertificado(certificado);
            viatran.setFechacal(fechacal);
            viatran.setFechavenc(fechavenc); //nueva fecha calculada en funcion de fechacal
            viatran.setServicio(servicio);
            viatran.setEstado(estado);
            viatran.setPeriodo(periodo);
            // ✅ Solo actualizar si se sube un nuevo certificado
            if (certificadoPdf != null) {
                viatran.setCertificadoPdf(certificadoPdf);
            }

            // String carpetaBase = "C:/mis_certificados/";
            // if (rutaCertificado != null && !rutaCertificado.trim().isEmpty()) {
            //   String rutaCompleta = carpetaBase + rutaCertificado;
            // viatran.setLink(rutaCompleta);
            // } else {
            //   viatran.setLink(null); // o lo dejás como está si no querés modificarlo
            // }
            viatranrepositorio.save(viatran);
        }

    }

    @Transactional
    public Viatran getOne(Integer serial) {
        return viatranrepositorio.getOne(serial);
    }

    @Transactional
    public void softDelete(Integer serial) throws MiException {
        Optional<Viatran> respuesta = viatranrepositorio.findById(serial);

        if (respuesta.isPresent()) {
            Viatran viatran = respuesta.get();
            viatran.setActivo(false);
            viatranrepositorio.save(viatran);
        } else {
            throw new MiException("No se encontró el sensor con ese serial");
        }
    }

    @Transactional
    public void hardDelete(Integer serial) throws MiException {
        Optional<Viatran> respuesta = viatranrepositorio.findById(serial);

        if (respuesta.isPresent()) {
            viatranrepositorio.deleteById(serial);
        } else {
            throw new MiException("No se encontró el sensor con ese serial");
        }
    }
//doy de alta un sensor inactivo

    @Transactional
    public void softAlta(Integer serial) throws MiException {
        Optional<Viatran> respuesta = viatranrepositorio.findById(serial);

        if (respuesta.isPresent()) {
            Viatran viatran = respuesta.get();
            viatran.setActivo(true);
            viatranrepositorio.save(viatran);
        } else {
            throw new MiException("No se encontró el sensor con ese serial");
        }
    }

    //agrupo cantidades por servicio
    public Map<String, Long> contarActivosPorServicio() {
        return listarActivos().stream()
                .collect(Collectors.groupingBy(Viatran::getServicio, Collectors.counting()));
    }

    // public Map<String, Long> contarInactivosPorServicio() {
    //     return listarInactivos().stream()
    //             .collect(Collectors.groupingBy(Viatran::getServicio, Collectors.counting()));
    //  }
    public long contarInactivos() {
        return listarInactivos().size();
    }

    @Scheduled(cron = "0 0 0 * * ?") // Todos los días a medianoche
    //@Scheduled(fixedRate = 1000000) // Cada 10 segundos
    public void actualizarVencimientos() {
        LocalDate hoy = LocalDate.now();
        List<Viatran> viatrans = viatranrepositorio.findAll();

        for (Viatran v : viatrans) {
            if (v.getFechavenc() != null && v.getFechavenc().isBefore(hoy)) {
                v.setEstado("Vencido"); // O el estado que uses para vencido
                viatranrepositorio.save(v);
            }
        }
        System.out.println("Actualización de estados vencidos ejecutada.");
    }
    
    
    

    //creo metodo para validar datos mal ingresados
    private void validar(
            Integer serial,
            String idSensor,
            LocalDate fechaalta,
            Integer certificado,
            LocalDate fechacal,
            String servicio,
            String estado,
            Integer periodo,
            boolean esNuevo
    ) throws MiException {

        if (serial == null) {
            throw new MiException("El serial no puede ser nulo");
        }

        if (idSensor == null || idSensor.trim().isEmpty()) {
            throw new MiException("El modelo del sensor no puede ser nulo o vacío");
        }

        if (fechaalta == null) {
            throw new MiException("La fecha de alta no puede ser nula");
        }

        if (fechaalta.isAfter(LocalDate.now())) {
            throw new MiException("La fecha de alta no puede ser posterior a hoy");
        }

        // Solo validar estos si NO es nuevo
        if (!esNuevo) {
            if (certificado == null) {
                throw new MiException("El número de certificado no puede ser nulo");
            }

            if (fechacal == null) {
                throw new MiException("La fecha de calibración no puede ser nula");
            }

            if (fechacal != null && fechacal.isBefore(fechaalta)) {
                throw new MiException("La fecha de calibración no puede ser anterior a la fecha de alta");
            }
        }

        if (servicio == null || servicio.trim().isEmpty()) {
            throw new MiException("El servicio no puede ser nulo o vacío");
        }

        if (estado == null || estado.trim().isEmpty()) {
            throw new MiException("El estado no puede ser nulo o vacío");
        }
        if (periodo == null) {
            throw new MiException("El periodo no puede ser nulo");
        }
    }

}
