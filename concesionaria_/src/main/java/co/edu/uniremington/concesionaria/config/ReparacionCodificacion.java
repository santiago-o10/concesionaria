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
            expresion = reemplazar(expresion, "\u00C3\u0083\u00C2\u00A1", "á");
            expresion = reemplazar(expresion, "\u00C3\u0083\u00C2\u00A9", "é");
            expresion = reemplazar(expresion, "\u00C3\u0083\u00C2\u00AD", "í");
            expresion = reemplazar(expresion, "\u00C3\u0083\u00C2\u00B3", "ó");
            expresion = reemplazar(expresion, "\u00C3\u0083\u00C2\u00BA", "ú");
            expresion = reemplazar(expresion, "\u00C3\u0083\u00C2\u00B1", "ñ");
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