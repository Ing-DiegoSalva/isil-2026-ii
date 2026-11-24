<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Supervisión de pedidos</title>
    <c:url var="cssUrl" value="/assets/css/app.css"/>
    <link rel="stylesheet" href="${cssUrl}">
</head>
<body>
<main class="contenedor">
    <c:url var="pedidosUrl" value="/pedidos"/>
    <c:url var="pendientesUrl" value="/supervision/pedidos">
        <c:param name="estado" value="PENDIENTE"/>
    </c:url>
    <c:url var="revisadosUrl" value="/supervision/pedidos">
        <c:param name="estado" value="REVISADO"/>
    </c:url>
    <c:set var="esOperador" value="${pageContext.request.isUserInRole('OPERADOR')}"/>

    <header class="cabecera">
        <h1>Supervisión de pedidos</h1>
        <a href="${pedidosUrl}" class="boton-link boton-link-secundario">Volver a Pedidos</a>
    </header>

    <section class="tarjeta" aria-label="Estado de revisión">
        <nav aria-label="Filtrar pedidos">
            <a href="${pendientesUrl}" class="boton-link boton-link-primario">Pendientes</a>
            <a href="${revisadosUrl}" class="boton-link boton-link-secundario">Revisados</a>
        </nav>

        <c:if test="${not empty mensaje}">
            <p class="alerta exito" role="status"><c:out value="${mensaje}"/></p>
        </c:if>
        <c:if test="${not empty errorRevision}">
            <p class="alerta error" role="alert"><c:out value="${errorRevision}"/></p>
        </c:if>

        <c:choose>
            <c:when test="${not empty errorReportes}">
                <p class="alerta error" role="alert"><c:out value="${errorReportes}"/></p>
            </c:when>
            <c:otherwise>
                <dl>
                    <dt>Total de pedidos</dt>
                    <dd><c:out value="${estadisticas.totalPedidos()}"/></dd>
                    <dt>Revisados</dt>
                    <dd><c:out value="${estadisticas.totalRevisados()}"/></dd>
                    <dt>Pendientes</dt>
                    <dd><c:out value="${estadisticas.totalPendientes()}"/></dd>
                    <dt>Porcentaje revisado</dt>
                    <dd><c:out value="${estadisticas.porcentajeRevisado()}"/> %</dd>
                </dl>

                <h2>Pedidos: <c:out value="${estado}"/></h2>
                <div class="tabla-scroll">
                    <table>
                        <thead>
                        <tr>
                            <th scope="col">ID</th>
                            <th scope="col">Cliente</th>
                            <th scope="col">Producto</th>
                            <th scope="col">Total</th>
                            <th scope="col">Estado</th>
                            <th scope="col">Observación</th>
                            <th scope="col">Fecha de revisión</th>
                            <th scope="col">Acción</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="pedido" items="${pedidos}">
                            <tr>
                                <td><c:out value="${pedido.id()}"/></td>
                                <td><c:out value="${pedido.cliente()}"/></td>
                                <td><c:out value="${pedido.productoNombre()}"/></td>
                                <td>S/ <c:out value="${pedido.total()}"/></td>
                                <td><c:out value="${pedido.estado()}"/></td>
                                <td><c:out value="${pedido.observacion()}" default="—"/></td>
                                <td><c:out value="${pedido.fechaRevision()}" default="—"/></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${esOperador and pedido.estado().name() eq 'PENDIENTE'}">
                                            <c:url var="revisarUrl" value="/supervision/pedidos/${pedido.id()}/revisar"/>
                                            <form method="post" action="${revisarUrl}">
                                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                                <label for="observacion-${pedido.id()}">Observación</label>
                                                <textarea id="observacion-${pedido.id()}" name="observacion"
                                                          rows="2" required></textarea>
                                                <button type="submit">Revisar pedido</button>
                                            </form>
                                        </c:when>
                                        <c:otherwise>—</c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty pedidos}">
                            <tr><td colspan="8" class="sin-datos">No hay pedidos en este estado.</td></tr>
                        </c:if>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
</main>
</body>
</html>
