package com.notify.gateway.filter;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApiKeyFilter implements GlobalFilter {
    private final MeterRegistry registry;
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-Key");
        if(apiKey == null || apiKey.isBlank()){
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        log.debug("API ключ не пустой");
        long start = System.nanoTime();
        return chain.filter(exchange).doFinally(signalType -> {
            long duration = System.nanoTime() - start;
            registry.timer("gateway.requests",
                    "path", exchange.getRequest().getPath().value(),
                    "status", exchange.getResponse().getStatusCode().toString())
                    .record(duration, TimeUnit.NANOSECONDS);
        });
    }
}
