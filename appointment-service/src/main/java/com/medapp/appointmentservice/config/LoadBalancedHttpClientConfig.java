package com.medapp.appointmentservice.config;

import com.medapp.appointmentservice.client.DoctorClient;
import com.medapp.appointmentservice.client.UserClient;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.restclient.autoconfigure.RestClientBuilderConfigurer;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@ImportHttpServices(group = "user-client", types = UserClient.class)
@ImportHttpServices(group = "doctor-client", types = DoctorClient.class)
public class LoadBalancedHttpClientConfig {

    @Autowired(required = false)
    private ObservationRegistry observationRegistry;

    @Autowired(required = false)
    private Tracer tracer;

    @Autowired(required = false)
    private Propagator propagator;

    @Bean
    @Primary
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public RestClient.Builder restClientBuilder(RestClientBuilderConfigurer configurer) {
        return configurer.configure(RestClient.builder());
    }

    // Load-balanced — gets the interceptor from the bean post processor.
    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder(RestClientBuilderConfigurer configurer) {

        RestClient.Builder builder = RestClient.builder();
        if (observationRegistry != null)
            builder.requestInterceptor(createTracingInterceptor());

        return configurer.configure(builder);
    }

    private ClientHttpRequestInterceptor createTracingInterceptor() {
        return ((request, body, execution) -> {
            if (tracer != null && propagator != null && tracer.currentSpan() != null)
                propagator.inject(tracer.currentTraceContext().context(),
                        request.getHeaders(),
                        (carrier, key, value) -> carrier.add(key, value));

            return execution.execute(request, body);
        });
    }

    @Bean
    public RestClientHttpServiceGroupConfigurer groupConfigurer() {
        return groups -> groups
                .forEachClient((group, clientBuilder) -> {
                    // اینجا می‌توانی برای هر گروه تنظیمات خاص اعمال کنی
                    // مثلاً:
                    // clientBuilder.defaultHeader("X-Custom-Header", "medapp");
                    // clientBuilder.requestFactory(new HttpComponentsClientHttpRequestFactory());
                });
    }

//    @Bean
//    public RestClientHttpServiceGroupConfigurer loadBalancedConfigurer(@LoadBalanced RestClient.Builder restClientBuilder) {
//        return RestClientHttpServiceGroupConfigurer.builder()
//                .forGroup("user-client", "doctor-client")
//                .apply(restClientBuilder -> restClientBuilder.build());
//    }
}