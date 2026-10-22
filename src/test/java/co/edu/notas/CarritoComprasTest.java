package co.edu.notas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/*
 * ENTREGA DEL TALLER
 * Nombre completo: Juan Camilo Arias Muñoz
 * 1. Bug encontrado (método y línea de CarritoCompras.java):
 *    En el método agregarProducto, la validación de cantidad usa if (cantidad < 0) en vez de if (cantidad <= 0).
 *    Permite agregar productos con cantidad 0 violando la regla R1.
 * 2. Prueba que lo detectó (nombre del método):
 *    agregarProducto_cantidadCero_lanzaExcepcion()
 * 3. Sugerencia de la IA que rechacé o corregí, y por qué:
 *    Se verificó que los casos límite en $50.000 para BIENVENIDO y $150.000 para envío gratis
 *    respetaran las reglas de negocio calculadas a mano antes de aceptar aserciones directas.
 */
class CarritoComprasTest {

    private CarritoCompras carrito;

    @BeforeEach
    void setUp() {
        carrito = new CarritoCompras();
    }

    // --- PARTE A: R1 y R2 ---

    @Test
    @DisplayName("Subtotal = precio * cantidad de cada producto")
    void calcularSubtotal_variosProductos_sumaCorrecta() {
        // Arrange
        carrito.agregarProducto("Cuaderno", 8_000, 3);
        carrito.agregarProducto("Esfero", 2_000, 5);
        // Act
        double subtotal = carrito.calcularSubtotal();
        // Assert
        assertEquals(34_000, subtotal, 0.001);
    }

    @Test
    @DisplayName("R2: Carrito vacío tiene subtotal 0 y total 0")
    void carritoVacio_retornaSubtotalYTotalCero() {
        assertEquals(0, carrito.calcularSubtotal(), 0.001);
        assertEquals(0, carrito.calcularTotal(), 0.001);
    }

    @Test
    @DisplayName("R1: Nombre nulo lanza IllegalArgumentException")
    void agregarProducto_nombreNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> carrito.agregarProducto(null, 10_000, 1));
    }

    @Test
    @DisplayName("R1: Nombre vacío o espacios en blanco lanza IllegalArgumentException")
    void agregarProducto_nombreBlanco_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> carrito.agregarProducto("   ", 10_000, 1));
    }

    @Test
    @DisplayName("R1: Precio menor o igual a 0 lanza IllegalArgumentException")
    void agregarProducto_precioInvalido_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> carrito.agregarProducto("Lápiz", 0, 1));
        assertThrows(IllegalArgumentException.class, () -> carrito.agregarProducto("Lápiz", -500, 1));
    }

    @Test
    @DisplayName("R1: Cantidad menor a 0 lanza IllegalArgumentException")
    void agregarProducto_cantidadNegativa_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> carrito.agregarProducto("Borrador", 1_000, -1));
    }

    @Test
    @DisplayName("R1 (Caso Límite): Cantidad exactamente 0 debe lanzar IllegalArgumentException")
    void agregarProducto_cantidadCero_lanzaExcepcion() {
        // Esta prueba detectará el bug del código si usa (cantidad < 0)
        assertThrows(IllegalArgumentException.class, () -> carrito.agregarProducto("Regla", 2_000, 0));
    }

    // --- PARTE B: R3 a R6 ---

    @Test
    @DisplayName("R3: Cupón SENA10 aplica 10% de descuento sobre el subtotal")
    void cuponSENA10_aplicaDiezPorCientoDescuento() {
        carrito.agregarProducto("Maleta", 100_000, 1);
        carrito.aplicarCupon("SENA10");
        assertEquals(10_000, carrito.calcularDescuento(), 0.001);
    }

    @Test
    @DisplayName("R4 (Frontera): Cupón BIENVENIDO con subtotal justo en 50.000 descuenta 5.000")
    void cuponBIENVENIDO_subtotalExacto50000_descuenta5000() {
        carrito.agregarProducto("Calculadora", 50_000, 1);
        carrito.aplicarCupon("BIENVENIDO");
        assertEquals(5_000, carrito.calcularDescuento(), 0.001);
    }

    @Test
    @DisplayName("R4 (Frontera): Cupón BIENVENIDO con subtotal en 49.999 no descuenta")
    void cuponBIENVENIDO_subtotal49999_descuentaCero() {
        carrito.agregarProducto("Libreta", 49_999, 1);
        carrito.aplicarCupon("BIENVENIDO");
        assertEquals(0, carrito.calcularDescuento(), 0.001);
    }

    @Test
    @DisplayName("R5: Cupón inválido lanza IllegalArgumentException")
    void cuponInvalido_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> carrito.aplicarCupon("DESCUENTO50"));
    }

    @Test
    @DisplayName("R6: Total con envío cobrado (menor a $150.000 con IVA)")
    void calcularTotal_envioCobrado() {
        // Subtotal = 100.000 -> con IVA (19%) = 119.000 -> menor a 150.000 -> + 12.000 = 131.000
        carrito.agregarProducto("Libro", 100_000, 1);
        assertEquals(131_000, carrito.calcularTotal(), 0.001);
    }

    @Test
    @DisplayName("R6: Total con envío gratis (>= $150.000 con IVA)")
    void calcularTotal_envioGratis() {
        // Subtotal = 500.000 - cupón SENA10 (50.000) = 450.000 -> con IVA (19%) = 535.500 -> >= 150.000 (envío gratis)
        carrito.agregarProducto("Monitor", 500_000, 1);
        carrito.aplicarCupon("SENA10");
        assertEquals(535_500, carrito.calcularTotal(), 0.001);
    }
}