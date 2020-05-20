package camel.exception.config;

import camel.exception.route.CamelExceptionSpikeRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 
 * Configure the Routes for Camel
 *
 */
@Configuration
public class CamelRoutesConfig { 


    @Bean("camelExceptionSpikeRouteBuilder")
    public RouteBuilder camelExceptionSpikeRouteBuilder() {
        return new CamelExceptionSpikeRouteBuilder();
    }
    
}