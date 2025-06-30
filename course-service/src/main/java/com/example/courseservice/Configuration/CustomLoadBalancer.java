package com.example.courseservice.Configuration;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private int lastInstance;
    private final ServiceInstanceListSupplier serviceInstanceListSupplier;

    public CustomLoadBalancer(ServiceInstanceListSupplier serviceInstanceListSupplier) {
        this.serviceInstanceListSupplier = serviceInstanceListSupplier;
        this.lastInstance = 0;
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        return serviceInstanceListSupplier.get().next().map(this::selectInstance);
    }

    private Response<ServiceInstance> selectInstance(List<ServiceInstance> instances) {
        if (instances.isEmpty()) return new EmptyResponse();

        List<ServiceInstance> weightedList = new ArrayList<>();
        for (ServiceInstance instance : instances) {
            int weight = parseWeight(instance);
            for (int i = 0; i < weight; i++) {
                weightedList.add(instance);
            }
        }

        lastInstance = (lastInstance + 1) % weightedList.size();
        System.out.println("Selected port: " + weightedList.get(lastInstance).getPort());

        return new DefaultResponse(weightedList.get(lastInstance));
    }

    private int parseWeight(ServiceInstance instance) {
        Map<String, String> metadata = instance.getMetadata();
        String weightStr = metadata.getOrDefault("weight", "1");
        try {
            return Math.max(1, Integer.parseInt(weightStr));
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
