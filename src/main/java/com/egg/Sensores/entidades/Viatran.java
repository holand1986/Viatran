
package com.egg.Sensores.entidades;

import java.time.LocalDate;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class Viatran {
    
    @Id
    private Integer serial;
    
    //private String marca;
    @ManyToOne
    private Instrumentos modelo;
    
   // @Temporal(TemporalType.DATE)
    private LocalDate fechaalta;
    
    private Integer certificado;
    
  // @Temporal(TemporalType.DATE)
    private LocalDate fechacal;
    
  // @Temporal(TemporalType.DATE)
    private LocalDate fechavenc;
    
    private String servicio;
    private String estado;
    
  
    
   // private String link; este atributo es para usar en un server donde pueda tener los cert en C:/mis-archivos
    
    @Lob
    @Column(name = "certificado_pdf")
    private byte[] certificadoPdf;

    private Boolean activo = true; // o "eliminado = false"
    
    private Integer periodo=12; //periodo establecido en 12 meses

    public Integer getSerial() {
        return serial;
    }

   

    public void setSerial(Integer serial) {
        this.serial = serial;
    }

    public Instrumentos getModelo() {
        return modelo;
    }

    public void setModelo(Instrumentos modelo) {
        this.modelo = modelo;
    }

    public LocalDate getFechaalta() {
        return fechaalta;
    }

    public void setFechaalta(LocalDate fechaalta) {
        this.fechaalta = fechaalta;
    }

    


    public Integer getCertificado() {
        return certificado;
    }

    public void setCertificado(Integer certificado) {
        this.certificado = certificado;
    }

    public LocalDate getFechacal() {
        return fechacal;
    }

    public void setFechacal(LocalDate fechacal) {
        this.fechacal = fechacal;
    }

    public LocalDate getFechavenc() {
        return fechavenc;
    }

    public void setFechavenc(LocalDate fechavenc) {
        this.fechavenc = fechavenc;
    }

    public String getServicio() {
        return servicio;
    }

    public void setServicio(String servicio) {
        this.servicio = servicio;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

   // public String getLink() {
    //    return link;
   // }

   // public void setLink(String link) {
   //     this.link = link;
   // }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Integer getPeriodo() {
        return periodo;
    }

    public void setPeriodo(Integer periodo) {
        this.periodo = periodo;
    }

    public byte[] getCertificadoPdf() {
        return certificadoPdf;
    }

    public void setCertificadoPdf(byte[] certificadoPdf) {
        this.certificadoPdf = certificadoPdf;
    }

    
    
}
