package com.example.demo.controller;

import com.example.demo.dtos.OrderRequest;
import com.example.demo.dtos.OrderResponse;
import com.example.demo.services.OrderService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private MeterRegistry meterRegistry;

    private final Counter orderCounter;
    private final Timer orderTimer;

    public OrderController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.orderCounter = Counter.builder("orders.created")
            .description("Total orders created")
            .register(meterRegistry);
        this.orderTimer = Timer.builder("orders.processing.time")
            .description("Order processing time")
            .register(meterRegistry);
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        log.info("📥 Received order request | User: {}, Product: {}", 
                request.getUserId(), request.getProductId());

        OrderResponse response = orderTimer.record(() -> 
            orderService.createOrder(request)
        );

        orderCounter.increment();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalOrders", orderCounter.count());
        metrics.put("averageProcessingTime", orderTimer.mean());
        metrics.put("maxProcessingTime", orderTimer.max());
        metrics.put("platformThreads", threadBean.getThreadCount());
        metrics.put("peakThreads", threadBean.getPeakThreadCount());
        metrics.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        
        // Compter Virtual Threads si Java 21+
        long virtualThreadCount = Thread.getAllStackTraces().keySet().stream()
            .filter(Thread::isVirtual)
            .count();
        metrics.put("virtualThreads", virtualThreadCount);

        return ResponseEntity.ok(metrics);
    }
}