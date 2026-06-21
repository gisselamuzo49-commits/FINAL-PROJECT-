package com.uce.internship_service.repositories;

import com.uce.internship_service.graph.OfertaNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfertaNodeRepository extends Neo4jRepository<OfertaNode, String> {
}
