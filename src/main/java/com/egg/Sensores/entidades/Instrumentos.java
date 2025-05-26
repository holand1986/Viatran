
package com.egg.Sensores.entidades;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import org.hibernate.annotations.GenericGenerator;

@Entity
public class Instrumentos {
    
    @Id
    @GeneratedValue(generator="uuid")                     //genera el id automaticamente
    @GenericGenerator(name = "uuid", strategy = "uuid2")  //segunda estrategia para que no se repitan 
    private String idSensor;
    private String modelo;

    public Instrumentos() {
    }

    public String getIdSensor() {
        return idSensor;
    }

    public void setIdSensor(String idSensor) {
        this.idSensor = idSensor;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

   
    
  
    
}
