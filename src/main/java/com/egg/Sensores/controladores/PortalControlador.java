
package com.egg.Sensores.controladores;

import com.egg.Sensores.entidades.Usuario;
import com.egg.Sensores.excepciones.MiException;
import com.egg.Sensores.servicios.UsuarioServicio;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller   //esta clase es tipo controlador
@RequestMapping("/")  //esto configura la URL que va a escuchar este controlador (va a escuchar a partir de la barra). Luego de localhost:8080/ se hace el llamado al controlador
public class PortalControlador {
    
    @Autowired
    private UsuarioServicio usuarioServicio;
    
    @GetMapping("/") // mapea en la url el metodo index
    public String index(){
      
        return "index.html"; // los .html se buscan en la carpeta template, other sources, templates
    }
    
    @GetMapping("/registrar")
    public String registrar(){
        return "registro.html";
    } 
      
    @PostMapping("/registro")
    public String registro(@RequestParam String nombre, @RequestParam String email, @RequestParam String password,
            String password2, ModelMap modelo, MultipartFile archivo) {

        try {
            usuarioServicio.registrar(nombre, email, password, password2);

            modelo.put("exito", "Usuario registrado correctamente!");

            return "registro.html";
        } catch (MiException ex) {

            modelo.put("error", ex.getMessage());
            modelo.put("nombre", nombre);
            modelo.put("email", email);

            return "registro.html";
        }

    }
   
   @GetMapping("/login")
    public String login(@RequestParam(required = false) String error, ModelMap modelo ) {

        if (error != null) {
            modelo.put("error", "Usuario o Contraseña invalidos!");
        }

        return "login.html";
    }
    
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/inicio")
    public String inicio(HttpSession session) {
        
        Usuario logueado = (Usuario) session.getAttribute("usuariosession");
        
        if (logueado.getRol().toString().equals("ADMIN")) {
            return "redirect:/viatran/lista";
        }
        
           return "inicio.html";
    }
    
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/perfil")
    public String perfil(ModelMap modelo,HttpSession session){
        Usuario usuario = (Usuario) session.getAttribute("usuariosession");
         modelo.put("usuario", usuario);
        return "usuario_modificar.html";
    }
    
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping("/perfil/{id}")
    public String actualizar(MultipartFile archivo,@PathVariable String id, @RequestParam String nombre,@RequestParam String email, 
            @RequestParam String password,@RequestParam String password2, ModelMap modelo) {

        try {
            usuarioServicio.actualizar( id, nombre, email, password, password2);

            modelo.put("exito", "Usuario actualizado correctamente!");

            return "login.html";
        } catch (MiException ex) {

            modelo.put("error", ex.getMessage());
            modelo.put("nombre", nombre);
            modelo.put("email", email);

            return "usuario_modificar.html";
        }

    }

}
