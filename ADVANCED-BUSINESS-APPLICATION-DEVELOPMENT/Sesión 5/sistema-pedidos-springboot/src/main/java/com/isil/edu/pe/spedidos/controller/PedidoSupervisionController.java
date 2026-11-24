package com.isil.edu.pe.spedidos.controller;

import com.isil.edu.pe.spedidos.client.reportes.PedidoReporteClient;
import com.isil.edu.pe.spedidos.client.reportes.PedidoReporteNoDisponibleException;
import com.isil.edu.pe.spedidos.client.reportes.dto.PedidoRevisionReporteResponse.EstadoRevision;
import com.isil.edu.pe.spedidos.service.PedidoRevisionService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/supervision/pedidos")
public class PedidoSupervisionController {

    private final PedidoReporteClient reportes;
    private final PedidoRevisionService revisiones;

    public PedidoSupervisionController(
            PedidoReporteClient reportes, PedidoRevisionService revisiones) {
        this.reportes = reportes;
        this.revisiones = revisiones;
    }

    @GetMapping
    public String mostrarSupervision(
            @RequestParam(name = "estado", defaultValue = "PENDIENTE") EstadoRevision estado,
            Model model,
            HttpServletResponse response) {
        model.addAttribute("estado", estado);

        try {
            var pedidos = reportes.listarRevisiones(estado);
            var estadisticas = reportes.obtenerEstadisticasRevisiones();
            model.addAttribute("pedidos", pedidos);
            model.addAttribute("estadisticas", estadisticas);
        } catch (PedidoReporteNoDisponibleException e) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            model.addAttribute("errorReportes",
                    "Reportes no está disponible por el momento. Intenta nuevamente.");
        }

        return "supervision-pedidos";
    }

    @PostMapping("/{id}/revisar")
    public String revisarPedido(
            @PathVariable("id") long pedidoId,
            @RequestParam(name = "observacion", required = false) String observacion,
            RedirectAttributes redirect) {
        try {
            switch (revisiones.revisar(pedidoId, observacion)) {
                case REVISADO -> {
                    redirect.addFlashAttribute("mensaje", "El pedido se revisó correctamente.");
                    return "redirect:/supervision/pedidos?estado=REVISADO";
                }
                case PEDIDO_INEXISTENTE ->
                        redirect.addFlashAttribute("errorRevision", "El pedido no existe.");
                case YA_REVISADO ->
                        redirect.addFlashAttribute("errorRevision", "El pedido ya fue revisado.");
            }
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("errorRevision", e.getMessage());
        } catch (DataAccessException e) {
            redirect.addFlashAttribute("errorRevision",
                    "No se pudo guardar la revisión. Intenta nuevamente.");
        }

        return "redirect:/supervision/pedidos";
    }
}
