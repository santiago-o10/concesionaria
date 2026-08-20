package co.edu.uniremington.concesionaria.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
public class HomeController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> inicio() {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("api", "Concesionaria API");
        respuesta.put("version", "1.0.0");
        respuesta.put("curso", "Lenguaje de Programacion 3 - IF0122");
        respuesta.put("estado", "en linea");
        respuesta.put("documentacion", "/api");
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("api")
    public ResponseEntity<Map<String, Object>> indice() {
        Map<String, String> recursos = new LinkedHashMap<>();
        recursos.put("marcas", "/api/marcas");
        recursos.put("modelos", "/api/modelos");
        recursos.put("vehiculos", "/api/vehiculos");
        recursos.put("concesionarias", "/api/concesionarias");
        recursos.put("asesores", "/api/asesores");
        recursos.put("disponibilidades", "/api/disponibilidades");
        recursos.put("clientes", "/api/clientes");
        recursos.put("solicitudes", "/api/solicitudes");
        recursos.put("financiaciones", "/api/financiaciones");
        recursos.put("administradores", "/api/administradores");

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("api", "Concesionaria API");
        respuesta.put("recursos", recursos);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("api/salud")
    public ResponseEntity<Map<String, Object>> salud() {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("estado", "UP");
        respuesta.put("marcaTiempo", LocalDateTime.now());
        return ResponseEntity.ok(respuesta);
    }
}
