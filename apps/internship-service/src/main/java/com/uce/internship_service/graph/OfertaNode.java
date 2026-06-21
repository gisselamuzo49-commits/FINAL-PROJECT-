package com.uce.internship_service.graph;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("Oferta")
public class OfertaNode {
    @Id
    private String internshipId;
    private String title;
    private String company;

    public OfertaNode() {}

    public OfertaNode(String internshipId, String title, String company) {
        this.internshipId = internshipId;
        this.title = title;
        this.company = company;
    }

    public String getInternshipId() { return internshipId; }
    public void setInternshipId(String internshipId) { this.internshipId = internshipId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
}
