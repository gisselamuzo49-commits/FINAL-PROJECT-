package com.uce.report_service.ws;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlRootElement(name = "GetReporteEstudianteRequest", namespace = "http://uce.com/report_service/ws")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "estudianteId"
})
public class GetReporteEstudianteRequest {

    @XmlElement(name = "estudianteId", namespace = "http://uce.com/report_service/ws", required = true)
    private String estudianteId;

    public String getEstudianteId() {
        return estudianteId;
    }

    public void setEstudianteId(String estudianteId) {
        this.estudianteId = estudianteId;
    }
}
