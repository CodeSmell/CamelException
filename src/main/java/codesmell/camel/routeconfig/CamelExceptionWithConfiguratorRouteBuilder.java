package codesmell.camel.routeconfig;

import codesmell.camel.CamelConstants;
import codesmell.exception.FooBarException;
import codesmell.service.FooBar;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CamelExceptionWithConfiguratorRouteBuilder extends RouteBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(CamelExceptionWithConfiguratorRouteBuilder.class);

    @Autowired
    private FooBar fooBarService;

    @Override
    public void configure() {
        
        LOGGER.info("building camel route with configurator to test exceptions...");

        from(CamelConstants.MAIN_CONFIGURATOR_ENDPOINT_URI)
            .routeId(CamelConstants.MAIN_CONFIGURATOR_ROUTE_ID)
            .routeConfigurationId(
                CamelConstants.SHARED_FOO_ERROR_CONFIG 
                + "," 
                + CamelConstants.SHARED_SPIKE_ERROR_CONFIG)
            .log("chugging along with configurator")
            .choice()
            .when(this::isGrokking)
                // end point w/ route config for error handling
                .to(CamelConstants.GROK_CONFIGURATOR_ENDPOINT_URI)
            .otherwise()
                .log("foo fighting with configurator")
                .process(exchange -> {
                    String in = exchange.getMessage().getBody(String.class);
                    String out = fooBarService.process(in);
                    exchange.getIn().setBody(out);
                })
                .process(exchange -> {
                    String body = exchange.getMessage().getBody(String.class);
                    if (body.startsWith("error")) {
                        throw new FooBarException("can't handle the message");
                    }
                })            
            .end()
            .process(exchange -> {
                LOGGER.info("after route with configurator message is now > {}", exchange.getMessage().getBody(String.class));
            })
            .to(CamelConstants.MAIN_CONFIGURATOR_OUTGOING_ENDPOINT_URI)
            .log("end main with configurator");
    }
    
    private boolean isGrokking(Exchange exchange) {
        String body = exchange.getMessage().getBody(String.class);
        if (body.contains("grok")) {
            return true;
        } else {
            return false;
        }
    }
}
