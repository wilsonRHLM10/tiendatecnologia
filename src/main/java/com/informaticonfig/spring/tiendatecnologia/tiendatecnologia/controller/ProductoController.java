package com.informaticonfig.spring.tiendatecnologia.tiendatecnologia.controller;

import com.informaticonfig.spring.tiendatecnologia.tiendatecnologia.model.Producto;
import com.informaticonfig.spring.tiendatecnologia.tiendatecnologia.repository.ProductoRepository;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class ProductoController {

    private static final long MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp");

    private final ProductoRepository productoRepository;

    public ProductoController(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("productos", productoRepository.findAll());
        return "index";
    }

    @GetMapping("/productos")
    public String listarProductos(Model model) {
        model.addAttribute("productos", productoRepository.findAll());
        return "productos";
    }

    @GetMapping("/productos/{id}")
    public String verProducto(@PathVariable Long id, Model model) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        model.addAttribute("producto", producto);
        return "detalle";
    }

    @GetMapping("/productos/{id}/imagen")
    public ResponseEntity<byte[]> verImagen(@PathVariable Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        if (producto.getImagen() == null || producto.getTipoImagen() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El producto no tiene imagen");
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(producto.getTipoImagen()))
                .body(producto.getImagen());
    }

    @GetMapping("/productos/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("titulo", "Registrar producto");
        return "formulario";
    }

    @PostMapping("/productos/guardar")
    public String guardarProducto(@Valid @ModelAttribute("producto") Producto producto,
                                 BindingResult bindingResult,
                                 Model model,
                                 @RequestParam(value = "archivoImagen", required = false) MultipartFile imagen) throws IOException {
        if (bindingResult.hasErrors()) {
            model.addAttribute("titulo", "Registrar producto");
            return "formulario";
        }

        String errorImagen = validarImagen(imagen);
        if (errorImagen != null) {
            model.addAttribute("titulo", "Registrar producto");
            model.addAttribute("errorImagen", errorImagen);
            return "formulario";
        }

        asignarImagen(producto, imagen);

        productoRepository.save(producto);
        return "redirect:/productos";
    }

    @GetMapping("/productos/editar/{id}")
    public String editarProducto(@PathVariable Long id, Model model) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        model.addAttribute("producto", producto);
        model.addAttribute("titulo", "Editar producto");
        return "formulario";
    }

    @PostMapping("/productos/actualizar/{id}")
    public String actualizarProducto(@PathVariable Long id,
                                    @Valid @ModelAttribute("producto") Producto producto,
                                    BindingResult bindingResult,
                                    Model model,
                                    @RequestParam(value = "archivoImagen", required = false) MultipartFile imagen) throws IOException {
        Producto productoExistente = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        if (bindingResult.hasErrors()) {
            producto.setImagen(productoExistente.getImagen());
            producto.setTipoImagen(productoExistente.getTipoImagen());
            model.addAttribute("titulo", "Editar producto");
            return "formulario";
        }

        String errorImagen = validarImagen(imagen);
        if (errorImagen != null) {
            producto.setImagen(productoExistente.getImagen());
            producto.setTipoImagen(productoExistente.getTipoImagen());
            model.addAttribute("titulo", "Editar producto");
            model.addAttribute("errorImagen", errorImagen);
            return "formulario";
        }

        productoExistente.setNombre(producto.getNombre());
        productoExistente.setCategoria(producto.getCategoria());
        productoExistente.setPrecio(producto.getPrecio());
        productoExistente.setCantidad(producto.getCantidad());
        productoExistente.setDescripcion(producto.getDescripcion());
        asignarImagen(productoExistente, imagen);

        productoRepository.save(productoExistente);
        return "redirect:/productos";
    }

    private String validarImagen(MultipartFile imagen) {
        if (imagen == null || imagen.isEmpty()) {
            return null;
        }
        if (imagen.getSize() > MAX_IMAGE_SIZE_BYTES) {
            return "La imagen no puede superar los 5 MB.";
        }

        String tipo = imagen.getContentType();
        if (tipo == null || !ALLOWED_IMAGE_TYPES.contains(tipo.toLowerCase(Locale.ROOT))) {
            return "Usa una imagen JPG, PNG, GIF o WEBP.";
        }
        return null;
    }

    private void asignarImagen(Producto producto, MultipartFile imagen) throws IOException {
        if (imagen != null && !imagen.isEmpty()) {
            producto.setImagen(imagen.getBytes());
            producto.setTipoImagen(imagen.getContentType().toLowerCase(Locale.ROOT));
        }
    }

    @GetMapping("/productos/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id) {
        productoRepository.deleteById(id);
        return "redirect:/productos";
    }
}
