package com.uce.report_service.ws;

import com.uce.report_service.models.ReporteEstudiante;
import com.uce.report_service.repositories.ReporteEstudianteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.math.BigDecimal;
import java.util.Optional;

@Endpoint
public class ReporteEndpoint {

    private static final String NAMESPACE_URI = "http://uce.com/report_service/ws";

    @Autowired
    private ReporteEstudianteRepository repository;

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetReporteEstudianteRequest")
    @ResponsePayload
    public GetReporteEstudianteResponse getReporte(@RequestPayload GetReporteEstudianteRequest request) {
        GetReporteEstudianteResponse response = new GetReporteEstudianteResponse();
        response.setEstudianteId(request.getEstudianteId());

        Optional<ReporteEstudiante> reportOpt = repository.findByEstudianteId(request.getEstudianteId());
        if (reportOpt.isPresent()) {
            ReporteEstudiante report = reportOpt.get();
            response.setTotalHorasValidadas(report.getTotalHorasValidadas());
            response.setTotalHorasPendientes(report.getTotalHorasPendientes());
            response.setTotalDocumentos(report.getTotalDocumentos());
            response.setUltimaActualizacion(report.getUltimaActualizacion() != null ? report.getUltimaActualizacion().toString() : null);
        } else {
            response.setTotalHorasValidadas(BigDecimal.ZERO);
            response.setTotalHorasPendientes(BigDecimal.ZERO);
            response.setTotalDocumentos(0);
            response.setUltimaActualizacion(null);
        }

        return response;
    }
}
