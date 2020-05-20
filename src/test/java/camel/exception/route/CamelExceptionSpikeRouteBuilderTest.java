package camel.exception.route;

import camel.exception.config.CamelRoutesConfig;
import camel.exception.mock.config.MockApplicationConfig;
import camel.exception.service.FooBar;
import camel.exception.service.FooBarException;
import camel.exception.service.SpikeException;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MockApplicationConfig.class, CamelRoutesConfig.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class CamelExceptionSpikeRouteBuilderTest extends CamelTestSupport {

    private String kafkaEndpoint;
    
    private MockEndpoint mockOutgoingEndpoint;
    
    @Autowired
    private FooBar mockFooBar;
    
    @Autowired
    private RouteBuilder exceptionSpikeRoute;
    
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        
        mockOutgoingEndpoint = getMockEndpoint(CamelExceptionSpikeRouteBuilder.OUTGOING_ENDPOINT);

        context.addRoutes(exceptionSpikeRoute);
        
        Mockito.reset(mockFooBar);
    }
    
    @Test
    public void test_successful_message_flow() throws Exception {
        
        // foo bar processing
        Mockito.when(mockFooBar.process(Mockito.anyString()))
            .thenReturn("barfoo");
        
        // setup the test data
        Exchange exchangeIn = new DefaultExchange(context);
        exchangeIn.getMessage().setBody("foobar");
        
        // put test data on the route
        ProducerTemplate producerTemplate = this.template();
        Exchange exchangeOut = producerTemplate.send(CamelExceptionSpikeRouteBuilder.FROM_ENDPOINT, exchangeIn);
        
        // check that all routes processed as expected
        mockOutgoingEndpoint.expectedMessageCount(1);
        assertMockEndpointsSatisfied();
        
        // checking the exceptions
        assertFalse(exchangeOut.isFailed());
        
        Exception exception = exchangeOut.getException();
        assertNull(exception);
        
        exception = exchangeOut.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        assertNull(exception);
        
        // verify the body
        String outBody = exchangeOut.getMessage().getBody(String.class);
        assertNotNull(outBody);
        assertEquals("barfoo", outBody);
    }
    
    @Test
    public void test_error_message_flow_handled() throws Exception {

        // simulate exception during route processing
        Mockito.when(mockFooBar.process(Mockito.anyString()))
            .thenThrow(new FooBarException("boom!"));
        
        // setup the test data
        Exchange exchangeIn = new DefaultExchange(context);
        exchangeIn.getMessage().setBody("foobar");
        
        // put test data on the route
        ProducerTemplate producerTemplate = this.template();
        Exchange exchangeOut = producerTemplate.send(CamelExceptionSpikeRouteBuilder.FROM_ENDPOINT, exchangeIn);

        // check that all routes processed as expected
        mockOutgoingEndpoint.expectedMessageCount(0);
        assertMockEndpointsSatisfied();
        
        // checking the exceptions
        assertFalse(exchangeOut.isFailed());
        
        Exception exception = exchangeOut.getException();
        assertNull(exception);
        
        exception = exchangeOut.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        assertNotNull(exception);
        assertTrue(exception instanceof FooBarException);
        assertEquals("boom!", exception.getMessage());
        
        // verify the body
        String outBody = exchangeOut.getMessage().getBody(String.class);
        assertNotNull(outBody);
        assertEquals("handled_exception_foobar", outBody);
    }
    
    @Test
    public void test_error_message_flow_unhandled() throws Exception {

        // simulate exception during route processing
        Mockito.when(mockFooBar.process(Mockito.anyString()))
            .thenThrow(new SpikeException("boom!"));
        
        // setup the test data
        Exchange exchangeIn = new DefaultExchange(context);
        exchangeIn.getMessage().setBody("foobar");
        
        // put test data on the route
        ProducerTemplate producerTemplate = this.template();
        Exchange exchangeOut = producerTemplate.send(CamelExceptionSpikeRouteBuilder.FROM_ENDPOINT, exchangeIn);

        // check that all routes processed as expected
        mockOutgoingEndpoint.expectedMessageCount(0);
        assertMockEndpointsSatisfied();
        
        // checking the exceptions
        assertTrue(exchangeOut.isFailed());
        
        Exception exception = exchangeOut.getException();
        assertNotNull(exception);
        assertTrue(exception instanceof SpikeException);
        assertEquals("boom!", exception.getMessage());
        
        exception = exchangeOut.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        assertNotNull(exception);
        assertTrue(exception instanceof SpikeException);
        assertEquals("boom!", exception.getMessage());
        
        // verify the body
        String outBody = exchangeOut.getMessage().getBody(String.class);
        assertNotNull(outBody);
        assertEquals("unhandled_exception_foobar", outBody);
    }

    @Test
    public void test_error_message_flow_exception_in_handled_onException() throws Exception {

        // simulate exception during route processing
        Mockito.when(mockFooBar.process(Mockito.anyString()))
            .thenReturn("error");
        
        // setup the test data
        Exchange exchangeIn = new DefaultExchange(context);
        exchangeIn.getMessage().setBody("foobar");
        
        // put test data on the route
        ProducerTemplate producerTemplate = this.template();
        Exchange exchangeOut = producerTemplate.send(CamelExceptionSpikeRouteBuilder.FROM_ENDPOINT, exchangeIn);

        // check that all routes processed as expected
        mockOutgoingEndpoint.expectedMessageCount(0);
        assertMockEndpointsSatisfied();
        
        // checking the exceptions
        assertTrue(exchangeOut.isFailed());
        
        Exception exception = exchangeOut.getException();
        assertNotNull(exception);
        assertTrue(exception instanceof SpikeException);
        assertEquals("still can't handle the message", exception.getMessage());
        
        exception = exchangeOut.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        assertNotNull(exception);
        assertTrue(exception instanceof SpikeException);
        assertEquals("still can't handle the message", exception.getMessage());
        
        // verify the body
        String outBody = exchangeOut.getMessage().getBody(String.class);
        assertNotNull(outBody);
        assertEquals("another_exception_error", outBody);
    }
}