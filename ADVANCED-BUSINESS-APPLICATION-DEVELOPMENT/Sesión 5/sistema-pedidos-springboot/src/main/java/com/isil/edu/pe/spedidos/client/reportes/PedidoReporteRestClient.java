package com.isil.edu.pe.spedidos.client.reportes;

import com.isil.edu.pe.spedidos.client.reportes.dto.PedidoReporteResponse;
import com.isil.edu.pe.spedidos.client.reportes.dto.ProductoVendidoResponse;
import com.isil.edu.pe.spedidos.client.reportes.dto.ResumenPedidosResponse;
import java.util.List;
import com.isil.edu.pe.spedidos.client.reportes.dto.EstadisticasRevisionResponse;
import com.isil.edu.pe.spedidos.client.reportes.dto.PedidoRevisionReporteResponse;
import com.isil.edu.pe.spedidos.client.reportes.dto.PedidoRevisionReporteResponse.EstadoRevision;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClientException;

@Component
public class PedidoReporteRestClient implements PedidoReporteClient {

  private final RestClient restClient;
  private final String baseUrl;

  public PedidoReporteRestClient(
      @Qualifier("pedidoReporteHttpClient") RestClient restClient,
      @Value("${pedido-reportes.api.base-url}") String baseUrl) {
    this.restClient = restClient;
    this.baseUrl = baseUrl;
  }

  @Override
  public ResumenPedidosResponse obtenerResumen() {
    try {
      ResumenPedidosResponse response = restClient.get()
          .uri(baseUrl + "/estadisticas/resumen")
          .accept(MediaType.APPLICATION_JSON)
          .retrieve()
          .body(ResumenPedidosResponse.class);
      if (response == null) {
        throw new PedidoReporteNoDisponibleException("El servicio de reportes devolvió una respuesta vacía", null);
      }
      return response;
    } catch (RestClientResponseException | ResourceAccessException e) {
      throw noDisponible(e);
    }
  }

  @Override
  public List<PedidoReporteResponse> buscarPorCliente(String cliente) {
    if (cliente == null || cliente.isBlank()) {
      return List.of();
    }
    try {
      List<PedidoReporteResponse> response = restClient.get()
          .uri(baseUrl + "?cliente={cliente}", cliente.trim())
          .accept(MediaType.APPLICATION_JSON)
          .retrieve()
          .body(new ParameterizedTypeReference<List<PedidoReporteResponse>>() { });
      return response == null ? List.of() : response;
    } catch (RestClientResponseException | ResourceAccessException e) {
      throw noDisponible(e);
    }
  }

  @Override
  public List<ProductoVendidoResponse> listarProductosMasVendidos() {
    try {
      List<ProductoVendidoResponse> response = restClient.get()
          .uri(baseUrl + "/estadisticas/productos")
          .accept(MediaType.APPLICATION_JSON)
          .retrieve()
          .body(new ParameterizedTypeReference<List<ProductoVendidoResponse>>() { });
      return response == null ? List.of() : response;
    } catch (RestClientResponseException | ResourceAccessException e) {
      throw noDisponible(e);
    }
  }
    @Override
    public List<PedidoRevisionReporteResponse> listarRevisiones(EstadoRevision estado) {
        try {
            var respuesta = restClient.get()
                    .uri(baseUrl + "/revisiones?estado={estado}", estado.name())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<PedidoRevisionReporteResponse>>() {});

            if (respuesta == null) {
                throw new PedidoReporteNoDisponibleException(
                        "El servicio de reportes devolvió una respuesta vacía.", null);
            }
            return respuesta;
        } catch (RestClientException e) {
            throw noDisponible(e);
        }
    }

    @Override
    public EstadisticasRevisionResponse obtenerEstadisticasRevisiones() {
        try {
            var respuesta = restClient.get()
                    .uri(baseUrl + "/estadisticas/revisiones")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(EstadisticasRevisionResponse.class);

            if (respuesta == null) {
                throw new PedidoReporteNoDisponibleException(
                        "El servicio de reportes devolvió una respuesta vacía.", null);
            }
            return respuesta;
        } catch (RestClientException e) {
            throw noDisponible(e);
        }
    }
  private PedidoReporteNoDisponibleException noDisponible(Exception e) {
    return new PedidoReporteNoDisponibleException(
        "pedidos-reportes-service no está disponible. Verifique que se ejecute en el puerto 8083.", e);
  }
}
