
package com.egg.Sensores.repositorios;

import com.egg.Sensores.entidades.Instrumentos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstrumentosRepositorio extends JpaRepository <Instrumentos, String> {
    
}
