package com.uce.report_service.influxdb;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class MetricsService {

    private final InfluxDBClient influxDBClient;
    private final String bucket;
    private final String org;

    @org.springframework.beans.factory.annotation.Autowired
    public MetricsService(
        @Value("${influxdb.url}") String url,
        @Value("${influxdb.token}") String token,
        @Value("${influxdb.org}") String org,
        @Value("${influxdb.bucket}") String bucket
    ) {
        this.org = org;
        this.bucket = bucket;
        this.influxDBClient = InfluxDBClientFactory.create(url, 
            token.toCharArray(), org, bucket);
    }

    // Constructor para pruebas unitarias
    MetricsService(InfluxDBClient influxDBClient, String org, String bucket) {
        this.org = org;
        this.bucket = bucket;
        this.influxDBClient = influxDBClient;
    }

    public void recordHorasMetric(String estudianteId, double horas) {
        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
        Point point = Point.measurement("horas_registradas")
            .addTag("estudiante_id", estudianteId)
            .addField("horas", horas)
            .time(Instant.now(), WritePrecision.MS);
        writeApi.writePoint(point);
    }

    public String getDatabaseInfo() {
        return "{\"database\": \"InfluxDB\", \"bucket\": \"" + bucket + 
               "\", \"org\": \"" + org + "\", \"status\": \"connected\"}";
    }
}
