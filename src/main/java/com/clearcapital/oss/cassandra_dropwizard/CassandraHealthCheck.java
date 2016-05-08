package com.clearcapital.oss.cassandra_dropwizard;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.clearcapital.oss.cassandra.RingClient;
import com.clearcapital.oss.cassandra.multiring.MultiRingClientManager;
import com.clearcapital.oss.json.JsonSerializer;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.health.HealthCheck;
import com.google.common.collect.ImmutableMap;

public class CassandraHealthCheck extends HealthCheck {

    private MultiRingClientManager manager;

    public CassandraHealthCheck(MultiRingClientManager manager) {
        this.manager = manager;
    }
    
    public static class HealthcheckResult {
        public Map<String, RingDetails> ringDetails;
        
        HealthcheckResult(Map<String,RingDetails> ringDetails) {
            this.ringDetails = ringDetails;
        }
    }
    
    public static class RingDetails {
        int openConnections;
        
        public RingDetails(int openConnections) {
            this.openConnections = openConnections;
        }
    }

    @Override
    protected Result check() throws Exception {
        long startTime = new Date().getTime();
        ImmutableMap<String, RingClient> clients = manager.getRingClients();

        Map<String,RingDetails> ringDetails = new TreeMap<>();
        for (Entry<String, RingClient> e : clients.entrySet()) {
            Gauge<Integer> count = e.getValue().getCluster().getMetrics().getConnectedToHosts();
            ringDetails.put(e.getKey(),new RingDetails(count.getValue()));
        }
        HealthcheckResult result = new HealthcheckResult(ringDetails); 
        String status = JsonSerializer.getInstance().getStringRepresentation(result);
        return Result.healthy(status + " [Request Duration: " + (new Date().getTime() - startTime) + "ms]");
    }

}
