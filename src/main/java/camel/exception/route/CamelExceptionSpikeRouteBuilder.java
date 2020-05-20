package camel.exception.route;

import camel.exception.service.FooBar;
import camel.exception.service.FooBarException;
import camel.exception.service.SpikeException;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class CamelExceptionSpikeRouteBuilder extends RouteBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(CamelExceptionSpikeRouteBuilder.class);

    public static final String FROM_ENDPOINT = "direct:foo";
    public static final String OUTGOING_ENDPOINT = "mock:end";
    public static final String ROUTE_ID = "exceptionSpike";
    
    @Autowired
    private FooBar fooBarService;

    @Override
    public void configure() throws Exception {
        
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

        from(FROM_ENDPOINT)
            .routeId(ROUTE_ID)
            .log("chugging along")
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
            .process(exchange -> {
                LOGGER.info("message is now > {}", exchange.getMessage().getBody(String.class));
            })
            .to(OUTGOING_ENDPOINT)
            .log("end");
    }

}