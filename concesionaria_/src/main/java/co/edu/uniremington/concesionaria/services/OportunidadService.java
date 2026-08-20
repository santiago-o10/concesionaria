package co.edu.uniremington.concesionaria.services;
import co.edu.uniremington.concesionaria.dto.CierreOportunidadRequest;
import co.edu.uniremington.concesionaria.models.*;
import java.util.*;
public interface OportunidadService {
 Oportunidad crearResultado(Long solicitudId, String resultado, String observaciones, String motivoPerdida, java.math.BigDecimal presupuesto, String formaPago, Long financiacionId,
   Boolean crearSeguimiento, String fechaSeguimiento, String medioSeguimiento, java.math.BigDecimal cuotaInicial, Integer plazoMeses);
 List<Oportunidad> listarTodos(); List<Oportunidad> listarPorAsesor(Long id); List<Oportunidad> listarPorCliente(Long id);
 Oportunidad actualizar(Long id,Oportunidad data);
 Oportunidad cerrar(Long id, CierreOportunidadRequest request);
}
