package ies.puesto.peliculas.repository.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

class PeliculaRowMapperTest {

    private final PeliculaRowMapper mapper = new PeliculaRowMapper();

    @Test
    void mapRowLeeLasCuatroColumnasPorNombre() throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("pelicula_codigo")).thenReturn("PEL001");
        when(rs.getString("titulo_texto")).thenReturn("El viaje de Chihiro");
        when(rs.getInt("estreno_anio")).thenReturn(2001);
        when(rs.getInt("duracion_minutos")).thenReturn(125);

        PeliculaEntity fila = mapper.mapRow(rs, 0);

        assertEquals("PEL001", fila.getCodigoFila());
        assertEquals("El viaje de Chihiro", fila.getTituloFila());
        assertEquals(2001, fila.getAnioFila());
        assertEquals(125, fila.getMinutosFila());
    }
}
