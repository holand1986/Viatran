
package com.egg.Sensores.controladores;

import com.egg.Sensores.entidades.Instrumentos;
import com.egg.Sensores.excepciones.MiException;
import com.egg.Sensores.servicios.InstrumentosServicio;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/instrumento")  //localhost:8080/instrumento
public class InstrumentosControlador {
    
    @Autowired
    private InstrumentosServicio instrumentosservicio;
    
    
    @GetMapping("/registrar")   //localhost:8080/instrumento/registrar
    public String registrar(){
        return "instrumentos_form.html";            
}
    @PostMapping("/registro")
    public String registro(@RequestParam String modelo) {   //indica q es un parametro requerido y que va a llegar cuando
        
        
        try {
       
        instrumentosservicio.crearInstrumentos(modelo);  //genera una excepcion porque en el metodo 
        } catch (MiException ex) {
            Logger.getLogger(InstrumentosControlador.class.getName()).log(Level.SEVERE, null, ex);
        return "instrumentos_form.html";
        }
        return "instrumentos_form.html";
    }
    
     @GetMapping("/lista")
    public String listar(ModelMap modelo){
        
        List <Instrumentos> instrumentos = instrumentosservicio.listarInstrumentos();
        
        modelo.addAttribute("instrumentos", instrumentos);  ////con este atibuto puedo ver la lista en el html
        
        return "instrumentos_list.html";
    }
    
    @GetMapping("/modificar/{idSensor}")
    public String modificar(@PathVariable String idSensor, ModelMap modelo){
        modelo.put("instrumentos", instrumentosservicio.getOne(idSensor));
        
        return "instrumentos_modificar.html";
    }
    
    @PostMapping("/modificar/{idSensor}")
    public String modificar(@PathVariable String idSensor, String nombre, ModelMap modelo){
        try {
            instrumentosservicio.modificarInstrumentos(idSensor, nombre);
            
            return "redirect:../lista";
            
        } catch (MiException ex) {
            modelo.put("error", ex.getMessage());
            return "instrumentos_modificar.html";
        }
        
    }
    
    @DeleteMapping("/modificar/{idSensor}")
    public String eliminar(@PathVariable String idSensor, ModelMap modelo) throws MiException{
        instrumentosservicio.eliminar(idSensor);
        
        return "instrumentos_modificar.html";
    }
    
    
    
    }
