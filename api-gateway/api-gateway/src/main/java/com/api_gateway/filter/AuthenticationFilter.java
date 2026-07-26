package com.api_gateway.filter;

import com.api_gateway.util.JwtUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    // Endpoints that bypass JWT verification
    private final List<String> openApiEndpoints = List.of(
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/eureka"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Extract path BEFORE the lambda to avoid the "effectively final" error
        String path = request.getURI().getPath();

        // 1. Check if route is secured
        boolean isSecured = openApiEndpoints.stream()
                .noneMatch(uri -> path.contains(uri));

        if (isSecured) {
            // 2. Extract the header
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            // 3. Validate presence and format of the header
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = authHeader.substring(7);
            try {
                // 4. Validate Token signature
                DecodedJWT decodedJWT = jwtUtil.validateToken(token);

                // 5. Pass user context to downstream services via HTTP Headers
                String username = decodedJWT.getSubject();
                String role = decodedJWT.getClaim("role").asString();

                // Create a NEW mutated request instead of reassigning the old one
                ServerHttpRequest mutatedRequest = exchange.getRequest()
                        .mutate()
                        .header("X-Logged-In-User", username)
                        .header("X-User-Role", role)
                        .build();

                // Continue with the mutated request
                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (Exception e) {
                System.out.println("Invalid JWT token: " + e.getMessage());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        }

        // Continue request execution downstream for open endpoints
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1; // Highest priority execution in the Gateway filter chain
    }
}