package co.edu.uniremington.concesionaria.services.impl;

import co.edu.uniremington.concesionaria.models.*;
import co.edu.uniremington.concesionaria.repositorys.*;
import co.edu.uniremington.concesionaria.services.SolicitudFinanciacionService;
import co.edu.uniremington.concesionaria.exceptions.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;

@Service
public class SolicitudFinanciacionServiceImpl implements SolicitudFinanciacionService {
    private final SolicitudFinanciacionRepository repo;
    private final OportunidadRepository oportunidades;
    private final FinanciacionRepository financiaciones;

    public SolicitudFinanciacionServiceImpl(SolicitudFinanciacionRepository repo, OportunidadRepository oportunidades,
                                            FinanciacionRepository financiaciones) {
        this.repo=repo; this.oportunidades=oportunidades; this.financiaciones=financiaciones;
    }

    @Override public List<SolicitudFinanciacion> listarTodos(){return repo.findAll();}
    @Override public List<SolicitudFinanciacion> listarPorAsesor(Long id){return repo.findByOportunidadAsesorIdAsesor(id);}
    @Override public List<SolicitudFinanciacion> listarPorCliente(Long id){return repo.findByOportunidadClienteIdCliente(id);}

    @Override @Transactional
    public SolicitudFinanciacion crear(SolicitudFinanciacion s){
        if(s.getOportunidad()==null || s.getOportunidad().getIdOportunidad()==null)
            throw new ReglaNegocioException("Debe indicar una oportunidad.");
        Oportunidad o=oportunidades.findById(s.getOportunidad().getIdOportunidad())
                .orElseThrow(()->new RecursoNoEncontradoException("No existe la oportunidad."));
        if(List.of("VENDIDA","PERDIDA").contains(String.valueOf(o.getEstado()).toUpperCase()))
            throw new ReglaNegocioException("La oportunidad ya está cerrada.");
        if(repo.findByOportunidadIdOportunidad(o.getIdOportunidad()).isPresent())
            throw new RecursoDuplicadoException("La oportunidad ya tiene una solicitud de financiación.");
        if(s.getFinanciacion()==null || s.getFinanciacion().getIdFinanciacion()==null)
            throw new ReglaNegocioException("Debe seleccionar un plan de financiación.");
        Financiacion plan=financiaciones.findById(s.getFinanciacion().getIdFinanciacion())
                .orElseThrow(()->new RecursoNoEncontradoException("No existe el plan de financiación."));
        BigDecimal valorVehiculo=o.getVehiculo().getPrecio();
        if(valorVehiculo==null || valorVehiculo.signum()<=0)
            throw new ReglaNegocioException("El vehículo no tiene un precio válido para financiar.");
        s.setMontoVehiculo(valorVehiculo);
        if(s.getCuotaInicial()==null || s.getCuotaInicial().signum()<0 || s.getCuotaInicial().compareTo(valorVehiculo)>=0)
            throw new ReglaNegocioException("La cuota inicial debe ser menor que el valor del vehículo.");
        if(plan.getPorcentajeInicial()!=null){
            BigDecimal minimo=valorVehiculo.multiply(plan.getPorcentajeInicial()).divide(BigDecimal.valueOf(100));
            if(s.getCuotaInicial().compareTo(minimo)<0)
                throw new ReglaNegocioException("La cuota inicial no cumple el porcentaje mínimo del plan seleccionado.");
        }
        BigDecimal solicitado=valorVehiculo.subtract(s.getCuotaInicial());
        if(s.getMontoSolicitado()!=null && s.getMontoSolicitado().compareTo(solicitado)!=0)
            throw new ReglaNegocioException("El monto a financiar debe ser valor del vehículo menos cuota inicial.");
        if(s.getPlazoMeses()==null || s.getPlazoMeses()<=0)
            throw new ReglaNegocioException("El plazo debe ser mayor que cero.");
        if(plan.getPlazo()!=null && s.getPlazoMeses()>plan.getPlazo())
            throw new ReglaNegocioException("El plazo supera el máximo del plan seleccionado.");
        s.setOportunidad(o); s.setFinanciacion(plan); o.setFinanciacion(plan); oportunidades.save(o); s.setMontoSolicitado(solicitado); s.setEstado("PENDIENTE");
        return repo.save(s);
    }

    @Override @Transactional
    public SolicitudFinanciacion actualizarEstado(Long id,String estado,String observaciones){
        SolicitudFinanciacion s=repo.findById(id).orElseThrow(()->new RecursoNoEncontradoException("No existe la solicitud de financiación."));
        String e=estado==null?"":estado.toUpperCase();
        if(!List.of("PENDIENTE","EN_ESTUDIO","APROBADA","RECHAZADA").contains(e))
            throw new ReglaNegocioException("Estado de financiación no válido.");
        s.setEstado(e); s.setObservaciones(observaciones);
        return repo.save(s);
    }
}
