package com.pventabase.ventas.integration.repository;

import com.pventabase.clientes.entity.Cliente;
import com.pventabase.inventario.entity.Producto;
import com.pventabase.ventas.domain.Venta;
import com.pventabase.ventas.domain.DetalleVenta;
import com.pventabase.clientes.repository.ClienteRepository;
import com.pventabase.inventario.repository.ProductoRepository;
import com.pventabase.ventas.repository.VentaRepository;
import com.pventabase.ventas.repository.DetalleVentaRepository;
import com.pventabase.ventas.config.TestJpaConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = TestJpaConfig.class)
class RepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    @Test
    void shouldSaveAndFindCliente() {
        Cliente cliente = new Cliente();
        cliente.setNombre("Juan Pérez");
        cliente.setApellido("García");
        cliente.setDocumento("12345678");
        cliente.setEmail("juan@test.com");
        cliente.setTelefono("555-1234");
        cliente.setDireccion("Calle 123");

        Cliente saved = clienteRepository.save(cliente);
        entityManager.flush();

        Optional<Cliente> found = clienteRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getNombre()).isEqualTo("Juan Pérez");
        assertThat(found.get().getEmail()).isEqualTo("juan@test.com");
    }

    @Test
    void shouldSaveAndFindProducto() {
        Producto producto = new Producto();
        producto.setCodigo("PROD-001");
        producto.setNombre("Producto Test");
        producto.setPrecioCompra(new BigDecimal("50.00"));
        producto.setPrecioVenta(new BigDecimal("100.00"));
        producto.setExistencia(10);

        Producto saved = productoRepository.save(producto);
        entityManager.flush();

        Optional<Producto> found = productoRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCodigo()).isEqualTo("PROD-001");
        assertThat(found.get().getExistencia()).isEqualTo(10);
    }

    @Test
    void shouldSaveAndFindVentaWithDetalles() {
        Cliente cliente = new Cliente();
        cliente.setNombre("Cliente Test");
        cliente.setApellido("Apellido");
        cliente.setDocumento("12345678");
        cliente.setEmail("cliente@test.com");
        cliente.setTelefono("555-1234");
        cliente.setDireccion("Calle 123");
        cliente = clienteRepository.save(cliente);

        Producto producto = new Producto();
        producto.setCodigo("PROD-001");
        producto.setNombre("Producto Test");
        producto.setPrecioCompra(new BigDecimal("50.00"));
        producto.setPrecioVenta(new BigDecimal("100.00"));
        producto.setExistencia(10);
        producto = productoRepository.save(producto);

        Venta venta = new Venta();
        venta.setFecha(LocalDateTime.now());
        venta.setEstado(Venta.EstadoVenta.COMPLETADA);
        venta.setCliente(cliente);

        DetalleVenta detalle = new DetalleVenta();
        detalle.setVenta(venta);
        detalle.setProducto(producto);
        detalle.setCantidad(new BigDecimal("2"));
        detalle.setPrecioUnitario(new BigDecimal("100.00"));
        venta.agregarDetalle(detalle);

        venta.setTotal(new BigDecimal("200.00"));

        Venta saved = ventaRepository.save(venta);
        entityManager.flush();

        Optional<Venta> found = ventaRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTotal()).isEqualByComparingTo("200.00");
        assertThat(found.get().getEstado()).isEqualTo(Venta.EstadoVenta.COMPLETADA);
        assertThat(found.get().getCliente().getId()).isEqualTo(cliente.getId());
        assertThat(found.get().getDetalles()).hasSize(1);
        assertThat(found.get().getDetalles().get(0).getCantidad()).isEqualTo(new BigDecimal("2"));
        assertThat(found.get().getDetalles().get(0).getSubtotal()).isEqualByComparingTo("200.00");
    }

    @Test
    void shouldFindVentaByEstado() {
        Cliente cliente = new Cliente();
        cliente.setNombre("Cliente 1");
        cliente.setApellido("Apellido");
        cliente.setDocumento("11111111");
        cliente.setEmail("cliente1@test.com");
        cliente.setTelefono("555-1111");
        cliente.setDireccion("Calle 123");
        cliente = clienteRepository.save(cliente);

        Venta venta1 = new Venta();
        venta1.setFecha(LocalDateTime.now());
        venta1.setEstado(Venta.EstadoVenta.COMPLETADA);
        venta1.setCliente(cliente);
        venta1.setTotal(new BigDecimal("100.00"));
        ventaRepository.save(venta1);

        Venta venta2 = new Venta();
        venta2.setFecha(LocalDateTime.now());
        venta2.setEstado(Venta.EstadoVenta.PENDIENTE);
        venta2.setCliente(cliente);
        venta2.setTotal(new BigDecimal("200.00"));
        ventaRepository.save(venta2);

        entityManager.flush();

        var completadas = ventaRepository.findByEstado(Venta.EstadoVenta.COMPLETADA, PageRequest.of(0, 10));
        assertThat(completadas.getContent()).hasSize(1);
        assertThat(completadas.getContent().get(0).getEstado()).isEqualTo(Venta.EstadoVenta.COMPLETADA);

        var pendientes = ventaRepository.findByEstado(Venta.EstadoVenta.PENDIENTE, PageRequest.of(0, 10));
        assertThat(pendientes.getContent()).hasSize(1);
        assertThat(pendientes.getContent().get(0).getEstado()).isEqualTo(Venta.EstadoVenta.PENDIENTE);
    }

    @Test
    void shouldFindClienteByEmail() {
        Cliente cliente = new Cliente();
        cliente.setNombre("Juan Pérez");
        cliente.setApellido("García");
        cliente.setDocumento("12345678");
        cliente.setEmail("juan@test.com");
        cliente.setTelefono("555-1234");
        cliente.setDireccion("Calle 123");
        clienteRepository.save(cliente);
        entityManager.flush();

        Optional<Cliente> found = clienteRepository.findByEmail("juan@test.com");
        assertThat(found).isPresent();
        assertThat(found.get().getNombre()).isEqualTo("Juan Pérez");

        Optional<Cliente> notFound = clienteRepository.findByEmail("noexiste@test.com");
        assertThat(notFound).isEmpty();
    }

    @Test
    void shouldFindProductoByCodigo() {
        Producto producto = new Producto();
        producto.setCodigo("PROD-001");
        producto.setNombre("Producto Test");
        producto.setPrecioCompra(new BigDecimal("50.00"));
        producto.setPrecioVenta(new BigDecimal("100.00"));
        producto.setExistencia(10);
        productoRepository.save(producto);
        entityManager.flush();

        Optional<Producto> found = productoRepository.findByCodigo("PROD-001");
        assertThat(found).isPresent();
        assertThat(found.get().getNombre()).isEqualTo("Producto Test");

        Optional<Producto> notFound = productoRepository.findByCodigo("NOEXISTE");
        assertThat(notFound).isEmpty();
    }
}