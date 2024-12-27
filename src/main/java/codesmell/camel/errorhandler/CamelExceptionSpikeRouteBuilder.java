package codesmell.camel.errorhandler;

import codesmell.camel.CamelConstants;
import codesmell.exception.FooBarException;
import codesmell.exception.SpikeException;
import codesmell.service.FooBar;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * the main route
 * it will flow some processing to sub routes
 * and will both handle and not handle some exceptions
 */
@Component
public class CamelExceptionSpikeRouteBuilder extends RouteBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(CamelExceptionSpikeRouteBuilder.class);

    @Autowired
    private FooBar fooBarService;

    @Override
    public void configure() {
        
        LOGGER.info("building camel route to test exceptions...");

        onException(SpikeException.class)
            .handled(false)
            .log("things are not going well...")
            .process(exchange -> {
                String in = exchange.getMessage().getBody(String.class);
                exchange.getMessage().setBody("unhandled_exception_" + in);
            });
        
        onException(FooBarException.class)
            .handled(true)
            .log("houston we have a problem...")
            .process(exchange -> {
                String in = exchange.getMessage().getBody(String.class);
                
                if (in.startsWith("error")) {
                    exchange.getMessage().setBody("another_exception_" + in);
                    throw new SpikeException("still can't handle the message");
                } else {
                    exchange.getMessage().setBody("handled_exception_" + in);
                }
            });

        from(CamelConstants.MAIN_ENDPOINT_URI)
            .routeId(CamelConstants.MAIN_ROUTE_ID)
            .log("chugging along")
            .choice()
            .when(this::isGrokking)
                // end point w/ no error handler
                .to(CamelConstants.GROK_ENDPOINT_URI)
            .when(this::isHeisenBugging)
                // end point w/ default error handler
                .to(CamelConstants.HEISENBUG_ENDPOINT_URI)
            .otherwise()
                .log("foo fighting")
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
                LOGGER.info("message is now > {}", exchange.getMessage().getBody(String.class));
            })

            .to(CamelConstants.MAIN_OUTGOING_ENDPOINT_URI)
            .log("end");
    }
    
    private boolean isGrokking(Exchange exchange) {
        String body = exchange.getMessage().getBody(String.class);
        if (body.contains("grok")) {
            return true;
        } else {
            return false;
        }
    }
    
    private boolean isHeisenBugging(Exchange exchange) {
        String body = exchange.getMessage().getBody(String.class);
        if (body.contains("heisenbug")) {
            return true;
        } else {
            return false;
        }
    }

}