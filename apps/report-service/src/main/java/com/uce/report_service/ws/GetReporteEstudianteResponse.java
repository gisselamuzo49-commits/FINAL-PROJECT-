package com.uce.report_service.ws;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.math.BigDecimal;

@XmlRootElement(name = "GetReporteEstudianteResponse", namespace = "http://uce.com/report_service/ws")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "estudianteId",
    "totalHorasValidadas",
    "totalHorasPendientes",
    "totalDocumentos",
    "ultimaActualizacion"
})
public class GetReporteEstudianteResponse {

    @XmlElement(name = "estudianteId", namespace = "http://uce.com/report_service/ws")
    private String estudianteId;

    @XmlElement(name = "totalHorasValidadas", namespace = "http://uce.com/report_service/ws")
    private BigDecimal totalHorasValidadas;

    @XmlElement(name = "totalHorasPendientes", namespace = "http://uce.com/report_service/ws")
    private BigDecimal totalHorasPendientes;

    @XmlElement(name = "totalDocumentos", namespace = "http://uce.com/report_service/ws")
    private Integer totalDocumentos;

    @XmlElement(name = "ultimaActualizacion", namespace = "http://uce.com/report_service/ws")
    private String ultimaActualizacion;

    public String getEstudianteId() {
        return estudianteId;
    }

    public void setEstudianteId(String estudianteId) {
        this.estudianteId = estudianteId;
    }

    public BigDecimal getTotalHorasValidadas() {
        return totalHorasValidadas;
    }

    public void setTotalHorasValidadas(BigDecimal totalHorasValidadas) {
        this.totalHorasValidadas = totalHorasValidadas;
    }

    public BigDecimal getTotalHorasPendientes() {
        return totalHorasPendientes;
    }

    public void setTotalHorasPendientes(BigDecimal totalHorasPendientes) {
        this.totalHorasPendientes = totalHorasPendientes;
    }

    public Integer getTotalDocumentos() {
        return totalDocumentos;
    }

    public void setTotalDocumentos(Integer totalDocumentos) {
        this.totalDocumentos = totalDocumentos;
    }

    public String getUltimaActualizacion() {
        return ultimaActualizacion;
    }

    public void setUltimaActualizacion(String ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
    }
}
