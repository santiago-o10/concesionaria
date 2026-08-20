package co.edu.uniremington.concesionaria.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="seguimientos")
public class Seguimiento {
 @Id @SequenceGenerator(name="seguimiento_seq",sequenceName="seguimiento_seq",allocationSize=1)
 @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="seguimiento_seq")
 private Long idSeguimiento;
 @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="oportunidad_id",nullable=false) private Oportunidad oportunidad;
 @Column(name="fecha_programada",nullable=false) private LocalDate fechaProgramada;
 @Column(nullable=false,length=30) private String medio;
 @Column(nullable=false,length=30) private String estado="PENDIENTE";
 @Column(length=40) private String resultado;
 @Column(length=1000) private String observaciones;
 @Column(name="fecha_realizado") private LocalDateTime fechaRealizado;
 public Long getIdSeguimiento(){return idSeguimiento;} public void setIdSeguimiento(Long v){idSeguimiento=v;}
 public Oportunidad getOportunidad(){return oportunidad;} public void setOportunidad(Oportunidad v){oportunidad=v;}
 public LocalDate getFechaProgramada(){return fechaProgramada;} public void setFechaProgramada(LocalDate v){fechaProgramada=v;}
 public String getMedio(){return medio;} public void setMedio(String v){medio=v;}
 public String getEstado(){return estado;} public void setEstado(String v){estado=v;}
 public String getResultado(){return resultado;} public void setResultado(String v){resultado=v;}
 public String getObservaciones(){return observaciones;} public void setObservaciones(String v){observaciones=v;}
 public LocalDateTime getFechaRealizado(){return fechaRealizado;} public void setFechaRealizado(LocalDateTime v){fechaRealizado=v;}
}