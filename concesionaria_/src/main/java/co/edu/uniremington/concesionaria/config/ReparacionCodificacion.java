package co.edu.uniremington.concesionaria.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class ReparacionCodificacion {

    private final JdbcTemplate jdbcTemplate;

    public ReparacionCodificacion(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void repararDatosSemilla() {
        String[] tablas = {
                "concesionarias:horario_atencion",
                "vehiculos:descripcion",
                "vehiculos:rendimiento",
                "vehiculos:seguridad",
                "vehiculos:tipo",
                "vehiculos:transmision",
                "financiaciones:nombre",
                "financiaciones:descripcion",
                "financiaciones:condiciones"
        };

        for (String tablaColumna : tablas) {
            String[] partes = tablaColumna.split(":", 2);
            String tabla = partes[0];
            String columna = partes[1];
            String expresion = columna;
            expresion = reemplazar(expresion, "ÃÂ¡", "á");
            expresion = reemplazar(expresion, "ÃÂ©", "é");
            expresion = reemplazar(expresion, "ÃÂ­", "í");
            expresion = reemplazar(expresion, "ÃÂ³", "ó");
            expresion = reemplazar(expresion, "ÃÂº", "ú");
            expresion = reemplazar(expresion, "ÃÂ±", "ñ");
            expresion = reemplazar(expresion, "Ã¡", "á");
            expresion = reemplazar(expresion, "Ã©", "é");
            expresion = reemplazar(expresion, "Ã­", "í");
            expresion = reemplazar(expresion, "Ã³", "ó");
            expresion = reemplazar(expresion, "Ãº", "ú");
            expresion = reemplazar(expresion, "Ã±", "ñ");
            if (!expresion.equals(columna)) {
                jdbcTemplate.update("UPDATE " + tabla + " SET " + columna + " = " + expresion
                        + " WHERE " + columna + " IS NOT NULL");
            }
        }
    }

    private String reemplazar(String expresion, String incorrecto, String correcto) {
        return "replace(" + expresion + ", '" + incorrecto + "', '" + correcto + "')";
    }
}