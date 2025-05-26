
package com.egg.Sensores.servicios;

import com.egg.Sensores.entidades.Instrumentos;
import com.egg.Sensores.excepciones.MiException;
import com.egg.Sensores.repositorios.InstrumentosRepositorio;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InstrumentosServicio {
    
    @Autowired
    InstrumentosRepositorio instrumentosrepositorio;
    
    @Transactional
    public void crearInstrumentos(String modelo) throws MiException{
        Instrumentos instrumento = new Instrumentos();
        instrumento.setModelo(modelo);
        
        instrumentosrepositorio.save(instrumento);
    }
    
    //metodo para crear una lista de instrumentos    
 public List<Instrumentos> listarInstrumentos(){
     
     List<Instrumentos> instrumentos = new ArrayList();
     instrumentos = instrumentosrepositorio.findAll();
     return instrumentos;
    }
     
   //metodo modificar instrumentos
 public void modificarInstrumentos(String idSensor, String modelo) throws MiException{
     
     validar(modelo);
     
     Optional<Instrumentos> respuesta = instrumentosrepositorio.findById(idSensor);
     System.out.println("id sensor es: " + respuesta);
     if (respuesta.isPresent()){
         Instrumentos instrumentos = respuesta.get();
         
         instrumentos.setModelo(modelo);
         instrumentosrepositorio.save(instrumentos);
         
     }
    }
 
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Instrumentos getOne(String idSensor){
        return instrumentosrepositorio.getOne(idSensor);
    }
    
   
    @org.springframework.transaction.annotation.Transactional
    public void eliminar(String idSensor) throws MiException{
        
        Instrumentos instrumentos = instrumentosrepositorio.getById(idSensor);
        
        instrumentosrepositorio.delete(instrumentos);

    }
    
     private void validar(String modelo) throws MiException {
        
        if (modelo.isEmpty() || modelo == null) {
            throw new MiException("el modelo no puede ser nulo o estar vacio");
        }
    }
 
 
}
