package codesmell.camel.routeconfig;

import codesmell.camel.CamelConstants;
import codesmell.exception.FooBarException;
import codesmell.exception.GrokException;
import codesmell.exception.SpikeException;
import org.apache.camel.builder.RouteConfigurationBuilder;
import org.springframework.stereotype.Component;

@Component
public class CamelExceptionRouteConfigurationBuilder extends RouteConfigurationBuilder {

    @Override
    public void configuration() throws Exception {

        routeConfiguration(CamelConstants.SHARED_SPIKE_ERROR_CONFIG)
            .onException(SpikeException.class)
                .handled(false)
                .log("things are not going well...")
                .process(exchange -> {
                    String in = exchange.getMessage().getBody(String.class);
                    exchange.getMessage().setBody("unhandled_spike_exception_" + in);
                });

        routeConfiguration(CamelConstants.SHARED_FOO_ERROR_CONFIG)
            .onException(FooBarException.class)
                .handled(true)
                .log("houston we have a problem...")
                .process(exchange -> {
                    String in = exchange.getMessage().getBody(String.class);
    
                    if (in.startsWith("error")) {
                        exchange.getMessage().setBody("another_exception_" + in);
                        throw new SpikeException("still can't handle the message");
                    } else {
                        exchange.getMessage().setBody("handled_foo_exception_" + in);
                    }
                });
        
        routeConfiguration(CamelConstants.SHARED_GROK_ERROR_CONFIG)
            .onException(GrokException.class)
                .handled(false)
                .log("things are not grokking very well...")
                .process(exchange -> {
                    String in = exchange.getMessage().getBody(String.class);
                    exchange.getMessage().setBody("unhandled_grok_exception_" + in);
                });
    }

}
