package codesmell.camel.errorhandler;

import codesmell.camel.CamelConstants;
import codesmell.service.Grokker;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * a sub route in a separate RouteBuilder
 * that has no ErrorHandler
 * will cause errors to be handled
 * by the main route
 */
@Component
public class GrokSubRouteRemoveErrorHandlerRouteBuilder extends RouteBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrokSubRouteRemoveErrorHandlerRouteBuilder.class);

    @Autowired
    private Grokker grokService;

    @Override
    public void configure() {
        
        LOGGER.info("building camel sub route to test exceptions...");

        from(CamelConstants.GROK_ENDPOINT_URI)
            .routeId(CamelConstants.GROK_ROUTE_ID)
            // this will cause 
            // errors to be handled by 
            // the main route
            .errorHandler(noErrorHandler())
            .log("grokking along")
            .process(exchange -> {
                // playing on a subroute
                String in = exchange.getMessage().getBody(String.class);
                String out = grokService.process(in);
                exchange.getIn().setBody(out);
            })
            .process(exchange -> {
                LOGGER.info("after grokking message is now > {}", exchange.getMessage().getBody(String.class));
            })
            .log("end grok");
    }

}