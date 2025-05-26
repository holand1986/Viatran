
package com.egg.Sensores.repositorios;

import com.egg.Sensores.entidades.Viatran;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ViatranRepositorio extends JpaRepository<Viatran, Integer> {
   
    @Query("SELECT l FROM Viatran l WHERE l.serial =:serial ")
    public Viatran buscarPorSerial (@Param("serial")Integer serie);
    
   // @Query("SELECT v FROM Viatran v WHERE v.activo = true")
   // List<Viatran> listarActivos();
    
    // @Query("SELECT v FROM Viatran v WHERE v.activo = false")
  //  List<Viatran> listarInactivos();
    
    List<Viatran> findByActivoTrue();  // Este método filtra por registros activos
    
    List<Viatran> findByActivoFalse();  // Este método filtra por registros inactivos
    
   @Query("SELECT v FROM Viatran v WHERE " +
       "(:serial IS NULL OR v.serial = :serial) AND " +
       "(:modelo IS NULL OR LOWER(v.modelo.modelo) LIKE LOWER(CONCAT('%', :modelo, '%'))) AND " +
       "(:fechavenc IS NULL OR v.fechavenc = :fechavenc) AND " +
       "(:fechacal IS NULL OR v.fechacal = :fechacal) AND " +
       "(:servicio IS NULL OR v.servicio = :servicio) AND " +
       "(:certificado IS NULL OR v.certificado = :certificado) AND " +
       "(:estado IS NULL OR v.estado = :estado) AND " +
       "v.activo = true")
        List<Viatran> filtrarViatrans(@Param("serial") Integer serial,
                              @Param("modelo") String modelo,
                              @Param("fechavenc") LocalDate fechavenc,
                              @Param("fechacal") LocalDate fechacal,
                              @Param("servicio") String servicio,
                              @Param("certificado") Integer certificado,
                              @Param("estado") String estado);

                                    




}
