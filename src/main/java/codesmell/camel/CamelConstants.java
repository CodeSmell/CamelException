package codesmell.camel;

public final class CamelConstants {

    public static final String SHARED_FOO_ERROR_CONFIG = "fooErrorConfig";
    public static final String SHARED_SPIKE_ERROR_CONFIG = "spikeErrorConfig";
    public static final String SHARED_GROK_ERROR_CONFIG = "grokErrorConfig";
    
    public static final String MAIN_ENDPOINT_URI = "direct:foo";
    public static final String MAIN_ROUTE_ID = "fooRoute";
    public static final String MAIN_OUTGOING_ENDPOINT_URI = "mock:end";
    
    public static final String GROK_ENDPOINT_URI = "direct:grok";
    public static final String GROK_ROUTE_ID = "grokRoute";
    
    public static final String HEISENBUG_ENDPOINT_URI = "direct:hesienbug";
    public static final String HEISENBUG_ROUTE_ID = "hesienbugRoute";
    


    public static final String MAIN_CONFIGURATOR_ENDPOINT_URI = "direct:fooConfig";
    public static final String MAIN_CONFIGURATOR_ROUTE_ID = "fooConfigRoute";
    public static final String MAIN_CONFIGURATOR_OUTGOING_ENDPOINT_URI = "mock:endConfig";
    
    public static final String GROK_CONFIGURATOR_ENDPOINT_URI = "direct:grokConfig";
    public static final String GROK_CONFIGURATOR_ROUTE_ID = "grokRouteConfig";
    
    
    private CamelConstants() {
        // you can't make me
    }
}
