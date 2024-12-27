package codesmell.camel.errorhandler;

import codesmell.camel.CamelConstants;
import codesmell.exception.GrokException;
import codesmell.service.FooBar;
import codesmell.service.Grokker;
import codesmell.service.HeisenBugger;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * a sub route in a separate RouteBuilder
 * that is still using the DefaultErrorHandler
 * will cause errors to be handled here
 * instead of by the main route
 */
@Component
public class HeisenbugSubRouteRouteBuilder extends RouteBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(HeisenbugSubRouteRouteBuilder.class);

    @Autowired
    private HeisenBugger heisenBuggerService;
    
    @Override
    public void configure() {
        
        LOGGER.info("building camel sub route to test exceptions...");

        from(CamelConstants.HEISENBUG_ENDPOINT_URI)
            .routeId(CamelConstants.HEISENBUG_ROUTE_ID)
            // using the default error handler
            // will cause errors to be handled here
            // instead of by the main route
            .log("uncertain where we are going")
            .process(exchange -> {
                // playing on a subroute
                String in = exchange.getMessage().getBody(String.class);
                String out = heisenBuggerService.process(in);
                exchange.getIn().setBody(out);
            })
            .process(exchange -> {
                LOGGER.info("after some uncertainty the message is now > {}", exchange.getMessage().getBody(String.class));
            })
            .log("end hesienbug");
    }

}