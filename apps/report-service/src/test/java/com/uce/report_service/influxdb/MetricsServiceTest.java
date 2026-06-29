package com.uce.report_service.influxdb;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.write.Point;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MetricsServiceTest {

    private InfluxDBClient influxDBClient;
    private WriteApiBlocking writeApiBlocking;
    private MetricsService metricsService;
    private final String org = "pasantias-uce";
    private final String bucket = "horas-metricas";

    @BeforeEach
    void setUp() {
        influxDBClient = mock(InfluxDBClient.class);
        writeApiBlocking = mock(WriteApiBlocking.class);
        when(influxDBClient.getWriteApiBlocking()).thenReturn(writeApiBlocking);
        
        metricsService = new MetricsService(influxDBClient, org, bucket);
    }

    @Test
    void testRecordHorasMetricCallsWritePoint() {
        String estudianteId = "EST123";
        double horas = 4.5;

        metricsService.recordHorasMetric(estudianteId, horas);

        verify(influxDBClient, times(1)).getWriteApiBlocking();
        verify(writeApiBlocking, times(1)).writePoint(any(Point.class));
    }

    @Test
    void testGetDatabaseInfoReturnsCorrectJson() {
        String info = metricsService.getDatabaseInfo();

        assertNotNull(info);
        assertTrue(info.contains("\"database\": \"InfluxDB\""));
        assertTrue(info.contains("\"bucket\": \"" + bucket + "\""));
        assertTrue(info.contains("\"org\": \"" + org + "\""));
        assertTrue(info.contains("\"status\": \"connected\""));
    }
}
