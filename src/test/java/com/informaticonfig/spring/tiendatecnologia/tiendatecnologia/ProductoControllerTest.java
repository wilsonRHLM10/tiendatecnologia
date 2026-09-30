package com.informaticonfig.spring.tiendatecnologia.tiendatecnologia;

import com.informaticonfig.spring.tiendatecnologia.tiendatecnologia.controller.ProductoController;
import com.informaticonfig.spring.tiendatecnologia.tiendatecnologia.model.Producto;
import com.informaticonfig.spring.tiendatecnologia.tiendatecnologia.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductoController.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoRepository productoRepository;

    @Test
    void debeMostrarLaPaginaPrincipal() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("TechStore")));
    }

    @Test
    void debeListarProductos() throws Exception {
        when(productoRepository.findAll()).thenReturn(List.of(
                new Producto(1L, "Portátil Lenovo", "Computadores", 2500000.0, 5, "Laptop 15\""))
        );

        mockMvc.perform(get("/productos"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Portátil Lenovo")));
    }

    @Test
    void debeGuardarLaImagenAlRegistrarUnProducto() throws Exception {
        byte[] contenidoImagen = {1, 2, 3};
        MockMultipartFile imagen = new MockMultipartFile("archivoImagen", "producto.png", "image/png", contenidoImagen);

        mockMvc.perform(multipart("/productos/guardar")
                        .file(imagen)
                        .param("nombre", "Teclado")
                        .param("categoria", "Accesorios")
                        .param("precio", "50000")
                        .param("cantidad", "4")
                        .param("descripcion", "Teclado mecánico"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/productos"));

        var captor = forClass(Producto.class);
        verify(productoRepository).save(captor.capture());
        assertArrayEquals(contenidoImagen, captor.getValue().getImagen());
        assertEquals("image/png", captor.getValue().getTipoImagen());
    }

    @Test
    void debeServirLaImagenGuardadaDelProducto() throws Exception {
        byte[] contenidoImagen = {4, 5, 6};
        Producto producto = new Producto(1L, "Teclado", "Accesorios", 50000.0, 4, "Teclado mecánico");
        producto.setImagen(contenidoImagen);
        producto.setTipoImagen("image/png");
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        mockMvc.perform(get("/productos/1/imagen"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/png"))
                .andExpect(content().bytes(contenidoImagen));
    }

    @Test
    void debeReemplazarLaImagenAlEditarUnProducto() throws Exception {
        byte[] imagenNueva = {7, 8, 9};
        Producto existente = new Producto(1L, "Teclado", "Accesorios", 50000.0, 4, "Teclado mecánico");
        existente.setImagen(new byte[]{1, 2, 3});
        existente.setTipoImagen("image/png");
        when(productoRepository.findById(1L)).thenReturn(Optional.of(existente));

        mockMvc.perform(multipart("/productos/actualizar/1")
                        .file(new MockMultipartFile("archivoImagen", "nuevo.jpg", "image/jpeg", imagenNueva))
                        .param("nombre", "Teclado actualizado")
                        .param("categoria", "Accesorios")
                        .param("precio", "55000")
                        .param("cantidad", "3")
                        .param("descripcion", "Teclado mecánico actualizado"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/productos"));

        var captor = forClass(Producto.class);
        verify(productoRepository).save(captor.capture());
        assertArrayEquals(imagenNueva, captor.getValue().getImagen());
        assertEquals("image/jpeg", captor.getValue().getTipoImagen());
    }

    @Test
    void debeConservarLaImagenAlEditarSinSeleccionarOtra() throws Exception {
        byte[] imagenActual = {1, 2, 3};
        Producto existente = new Producto(1L, "Teclado", "Accesorios", 50000.0, 4, "Teclado mecánico");
        existente.setImagen(imagenActual);
        existente.setTipoImagen("image/png");
        when(productoRepository.findById(1L)).thenReturn(Optional.of(existente));

        mockMvc.perform(multipart("/productos/actualizar/1")
                        .param("nombre", "Teclado actualizado")
                        .param("categoria", "Accesorios")
                        .param("precio", "55000")
                        .param("cantidad", "3")
                        .param("descripcion", "Teclado mecánico actualizado"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/productos"));

        var captor = forClass(Producto.class);
        verify(productoRepository).save(captor.capture());
        assertArrayEquals(imagenActual, captor.getValue().getImagen());
        assertEquals("image/png", captor.getValue().getTipoImagen());
    }
}
