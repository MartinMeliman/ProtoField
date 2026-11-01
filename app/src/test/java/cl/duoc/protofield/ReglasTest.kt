package cl.duoc.protofield

import cl.duoc.protofield.model.PuntoMedicion
import cl.duoc.protofield.model.Reglas
import cl.duoc.protofield.model.Tendencia
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
/**
 * PRUEBAS UNITARIAS de las reglas de negocio (no necesitan emulador).
 * Se ejecutan con clic derecho → Run 'ReglasTest'.
 */
class ReglasTest {

    @Test
    fun rutConDigitoCorrectoEsValido() {
        assertTrue(Reglas.rutValido("12.345.678-5"))
        assertTrue(Reglas.rutValido("15432876-9"))
    }

    @Test
    fun rutConDigitoIncorrectoNoEsValido() {
        assertFalse(Reglas.rutValido("12.345.678-4"))
        assertFalse(Reglas.rutValido("123"))
    }

    @Test
    fun rutSeFormateaMientrasSeEscribe() {
        assertEquals("12.345.678-5", Reglas.formatearRut("123456785"))
        assertEquals("12345678-5", Reglas.rutParaGuardar("12.345.678-5"))
    }

    @Test
    fun criterioMinimoYMaximo() {
        assertTrue(Reglas.cumpleCriterio(1500.0, minimo = 1000.0, maximo = null))
        assertFalse(Reglas.cumpleCriterio(800.0, minimo = 1000.0, maximo = null))
        assertTrue(Reglas.cumpleCriterio(80.0, minimo = null, maximo = 100.0))
        assertFalse(Reglas.cumpleCriterio(70.0, minimo = 20.0, maximo = 60.0))
    }

    @Test
    fun numeroConComaOPunto() {
        assertEquals(12.5, Reglas.aNumero("12,5")!!, 0.0001)
        assertEquals(12.5, Reglas.aNumero("12.5")!!, 0.0001)
        assertNull(Reglas.aNumero("abc"))
    }

    @Test
    fun alertaCuandoCambia25PorCientoOMas() {
        val variacion = Reglas.variacionPorcentual(1380.0, 2150.0)
        assertTrue(Reglas.esAlerta(variacion))       // −35,8 %
        assertFalse(Reglas.esAlerta(Reglas.variacionPorcentual(2150.0, 2400.0))) // −10,4 %
    }

    @Test
    fun tendenciaQueBajaProyectaFechaDeCruce() {
        val dia = 24L * 60 * 60 * 1000
        val puntos = listOf(PuntoMedicion(0, 2400.0), PuntoMedicion(185 * dia, 2150.0), PuntoMedicion(365 * dia, 1380.0))
        val tendencia = Tendencia.calcular("Resistencia de aislamiento", "MΩ", puntos, minimo = 1000.0, maximo = null)
        assertNotNull(tendencia)
        assertNotNull(tendencia!!.fechaCruce)
        assertTrue(tendencia.fechaCruce!! > 365 * dia)
    }
}