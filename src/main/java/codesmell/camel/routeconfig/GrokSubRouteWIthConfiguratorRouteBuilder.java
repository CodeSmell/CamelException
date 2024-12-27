package codesmell.camel.routeconfig;

import codesmell.camel.CamelConstants;
import codesmell.service.Grokker;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * a sub route in a separate RouteBuilder
 * that has configured the shared error handling
 */
@Component
public class GrokSubRouteWIthConfiguratorRouteBuilder extends RouteBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrokSubRouteWIthConfiguratorRouteBuilder.class);

    @Autowired
    private Grokker grokService;

    @Override
    public void configure() {
        
        LOGGER.info("building camel sub route to test exceptions...");

        from(CamelConstants.GROK_CONFIGURATOR_ENDPOINT_URI)
            .routeId(CamelConstants.GROK_CONFIGURATOR_ROUTE_ID)
            // using the same shared error config
            .routeConfigurationId(
                CamelConstants.SHARED_FOO_ERROR_CONFIG 
                + "," 
                + CamelConstants.SHARED_SPIKE_ERROR_CONFIG)
            .log("grokking along with configurator")
            .process(exchange -> {
                // playing on a subroute
                String in = exchange.getMessage().getBody(String.class);
                String out = grokService.process(in);
                exchange.getIn().setBody(out);
            })
            .process(exchange -> {
                LOGGER.info("after grokking with a configurator message is now > {}", exchange.getMessage().getBody(String.class));
            })
            .log("end grok configurator");
    }

}