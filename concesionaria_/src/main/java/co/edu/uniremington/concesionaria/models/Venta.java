package co.edu.uniremington.concesionaria.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name="ventas")
public class Venta {
 @Id @SequenceGenerator(name="venta_seq",sequenceName="venta_seq",allocationSize=1)
 @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="venta_seq")
 private Long idVenta;
 @OneToOne(fetch=FetchType.EAGER) @JoinColumn(name="oportunidad_id",unique=true,nullable=false) private Oportunidad oportunidad;
 @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="cliente_id",nullable=false) private Cliente cliente;
 @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="asesor_id",nullable=false) private Asesor asesor;
 @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="vehiculo_id",nullable=false) private Vehiculo vehiculo;
 @NotNull @DecimalMin("0.01") @Column(nullable=false,precision=14,scale=2) private BigDecimal precioFinal;
 @Column(name="forma_pago",nullable=false,length=30) private String formaPago;
 @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="financiacion_id") private Financiacion financiacion;
 @Column(name="fecha_venta",nullable=false) private LocalDate fechaVenta;
 public Long getIdVenta(){return idVenta;} public void setIdVenta(Long v){idVenta=v;}
 public Oportunidad getOportunidad(){return oportunidad;} public void setOportunidad(Oportunidad v){oportunidad=v;}
 public Cliente getCliente(){return cliente;} public void setCliente(Cliente v){cliente=v;}
 public Asesor getAsesor(){return asesor;} public void setAsesor(Asesor v){asesor=v;}
 public Vehiculo getVehiculo(){return vehiculo;} public void setVehiculo(Vehiculo v){vehiculo=v;}
 public BigDecimal getPrecioFinal(){return precioFinal;} public void setPrecioFinal(BigDecimal v){precioFinal=v;}
 public String getFormaPago(){return formaPago;} public void setFormaPago(String v){formaPago=v;}
 public Financiacion getFinanciacion(){return financiacion;} public void setFinanciacion(Financiacion v){financiacion=v;}
 public LocalDate getFechaVenta(){return fechaVenta;} public void setFechaVenta(LocalDate v){fechaVenta=v;}
}