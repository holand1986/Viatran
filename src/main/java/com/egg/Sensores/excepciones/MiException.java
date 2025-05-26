
package com.egg.Sensores.excepciones;


public class MiException extends Exception {
    
    // creo constructor que reciba el mensaje
    public MiException (String msg){
        super (msg); //le paso el mensaje al super
    }
    
}
