package com.medapp.ui.config;

import com.medapp.ui.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;

@Configuration
@RequiredArgsConstructor
public class HttpClientConfig {

    private final SessionService sessionService;

    @Bean
    public RestClientHttpServiceGroupConfigurer groupConfigurer() {
        return groups -> groups.forEachClient((group, builder) -> {
            builder.requestInterceptor((request, body, execution) -> {
                if (sessionService.getAccessToken() != null) {
                    request.getHeaders().setBearerAuth(sessionService.getAccessToken());
                }
                return execution.execute(request, body);
            });
        });
    }
}