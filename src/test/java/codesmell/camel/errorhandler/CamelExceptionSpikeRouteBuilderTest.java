package codesmell.camel.errorhandler;

import codesmell.camel.CamelConstants;
import codesmell.exception.FooBarException;
import codesmell.exception.SpikeException;
import codesmell.mock.config.MockApplicationConfig;
import codesmell.service.FooBar;
import codesmell.service.Grokker;
import codesmell.service.HeisenBugger;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.support.DefaultExchange;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@CamelSpringBootTest
@SpringBootTest
@Import({
    MockApplicationConfig.class
})
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CamelExceptionSpikeRouteBuilderTest {
    
    @Autowired
    CamelContext context;
    
    @Produce(CamelConstants.MAIN_ENDPOINT_URI)
    private ProducerTemplate camelProducer;

    @EndpointInject(CamelConstants.MAIN_OUTGOING_ENDPOINT_URI)
    private MockEndpoint mockOutgoingEndpoint;

    @Autowired
    private FooBar mockFooBar;
    
    @Autowired
    private Grokker mockGrokker;
    
    @Autowired
    HeisenBugger mockHeisenBugger;

    @AfterEach
    public void cleanup() {
        Mockito.reset(mockFooBar, mockGrokker, mockHeisenBugger);
    }

    @Test
    public void test_successful_message_main_foo_flow() throws Exception {
        Mockito.when(mockFooBar.process(Mockito.anyString()))
            .thenReturn("barfoo");
        
        mockOutgoingEndpoint.expectedMessageCount(1);
        
        // setup the test data
        Exchange exchangeIn = new DefaultExchange(context);
        exchangeIn.getMessage().setBody("foobar");
        
        Exchange exchangeOut = camelProducer.send(exchangeIn);
        
        // check that all routes processed as expected
        mockOutgoingEndpoint.assertIsSatisfied();
        
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
    public void test_error_message_main_foo_flow_handled() throws Exception {

        // simulate exception during route processing
        Mockito.when(mockFooBar.process(Mockito.anyString()))
            .thenThrow(new FooBarException("boom!"));
        
        mockOutgoingEndpoint.expectedMessageCount(0);
        
        // setup the test data
        Exchange exchangeIn = new DefaultExchange(context);
        exchangeIn.getMessage().setBody("foobar");
        
        // put test data on the route
        Exchange exchangeOut = camelProducer.send(exchangeIn);

        // check that all routes processed as expected
        mockOutgoingEndpoint.assertIsSatisfied();
        
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
    public void test_error_message_main_foo_flow_unhandled() throws Exception {

        // simulate exception during route processing
        Mockito.when(mockFooBar.process(Mockito.anyString()))
            .thenThrow(new SpikeException("boom!"));
        
        mockOutgoingEndpoint.expectedMessageCount(0);
        
        // setup the test data
        Exchange exchangeIn = new DefaultExchange(context);
        exchangeIn.getMessage().setBody("foobar");
        
        // put test data on the route
        Exchange exchangeOut = camelProducer.send(exchangeIn);

        // check that all routes processed as expected
        mockOutgoingEndpoint.assertIsSatisfied();
        
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
    public void test_error_message_main_foo_flow_exception_in_handled_onException() throws Exception {

        // simulate exception during route processing
        Mockito.when(mockFooBar.process(Mockito.anyString()))
            .thenReturn("error");
        
        mockOutgoingEndpoint.expectedMessageCount(0);
        
        // setup the test data
        Exchange exchangeIn = new DefaultExchange(context);
        exchangeIn.getMessage().setBody("foobar");
        
        // put test data on the route
        Exchange exchangeOut = camelProducer.send(exchangeIn);
        
        // check that all routes processed as expected
        mockOutgoingEndpoint.assertIsSatisfied();
        
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
    
    /**
     * should use main route error handling
     */
    @Test
    public void test_successful_message_grok_flow() throws Exception {
        Mockito.when(mockGrokker.process(Mockito.anyString()))
            .thenReturn("you grokked me");
        
        mockOutgoingEndpoint.expectedMessageCount(1);
        
        // setup the test data
        Exchange exchangeIn = new DefaultExchange(context);
        exchangeIn.getMessage().setBody("grok");
        
        Exchange exchangeOut = camelProducer.send(exchangeIn);
        
        // check that all routes processed as expected
        mockOutgoingEndpoint.assertIsSatisfied();
        
        // checking the exceptions
        assertFalse(exchangeOut.isFailed());
        
        Exception exception = exchangeOut.getException();
        assertNull(exception);
        
        exception = exchangeOut.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        assertNull(exception);
        
        // verify the body
        String outBody = exchangeOut.getMessage().getBody(String.class);
        assertNotNull(outBody);
        assertEquals("you grokked me", outBody);
    }
    
    /**
     * should use main route error handling
     */
    @Test
    public void test_error_message_grok_flow_handled() throws Exception {

        // simulate exception during route processing
        Mockito.when(mockGrokker.process(Mockito.anyString()))
            .thenThrow(new FooBarException("grok went boom!"));
        
        mockOutgoingEndpoint.expectedMessageCount(0);
        
        // setup the test data
        Exchange exchangeIn = new DefaultExchange(context);
        exchangeIn.getMessage().setBody("grok");
        
        // put test data on the route
        Exchange exchangeOut = camelProducer.send(exchangeIn);

        // check that all routes processed as expected
        mockOutgoingEndpoint.assertIsSatisfied();
        
        // checking the exceptions
        assertFalse(exchangeOut.isFailed());
        
        
        Exception exception = exchangeOut.getException();
        assertNull(exception);
        
        exception = exchangeOut.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        assertNotNull(exception);
        assertTrue(exception instanceof FooBarException);
        assertEquals("grok went boom!", exception.getMessage());
        
        // verify the body
        String outBody = exchangeOut.getMessage().getBody(String.class);
        assertNotNull(outBody);
        assertEquals("handled_exception_grok", outBody);
    }
    
    
    /**
     * should use main route error handling
     */
    @Test
    public void test_error_message_grok_flow_unhandled() throws Exception {

        // simulate exception during route processing
        Mockito.when(mockGrokker.process(Mockito.anyString()))
            .thenThrow(new SpikeException("grok went boom w/ spike exception!"));
        
        mockOutgoingEndpoint.expectedMessageCount(0);
        
        // setup the test data
        Exchange exchangeIn = new DefaultExchange(context);
        exchangeIn.getMessage().setBody("grok");
        
        // put test data on the route
        Exchange exchangeOut = camelProducer.send(exchangeIn);

        // check that all routes processed as expected
        mockOutgoingEndpoint.assertIsSatisfied();
        
        // checking the exceptions
        assertTrue(exchangeOut.isFailed());
        
        Exception exception = exchangeOut.getException();
        assertNotNull(exception);
        assertTrue(exception instanceof SpikeException);
        assertEquals("grok went boom w/ spike exception!", exception.getMessage());
        
        exception = exchangeOut.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        assertNotNull(exception);
        assertTrue(exception instanceof SpikeException);
        assertEquals("grok went boom w/ spike exception!", exception.getMessage());
        
        // verify the body
        String outBody = exchangeOut.getMessage().getBody(String.class);
        assertNotNull(outBody);
        assertEquals("unhandled_exception_grok", outBody);
    }

    /**
     * will NOT use main route error handling
     */    
    @Test
    public void test_successful_message_heisenbug_flow() throws Exception {
        Mockito.when(mockHeisenBugger.process(Mockito.anyString()))
            .thenReturn("you certainly made it this far");
        
        mockOutgoingEndpoint.expectedMessageCount(1);
        
        // setup the test data
        Exchange exchangeIn = new DefaultExchange(context);
        exchangeIn.getMessage().setBody("heisenbug");
        
        Exchange exchangeOut = camelProducer.send(exchangeIn);
        
        // check that all routes processed as expected
        mockOutgoingEndpoint.assertIsSatisfied();
        
        // checking the exceptions
        assertFalse(exchangeOut.isFailed());
        
        Exception exception = exchangeOut.getException();
        assertNull(exception);
        
        exception = exchangeOut.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        assertNull(exception);
        
        // verify the body
        String outBody = exchangeOut.getMessage().getBody(String.class);
        assertNotNull(outBody);
        assertEquals("you certainly made it this far", outBody);
    }
    
    /**
     * will NOT use main route error handling
     */ 
    @Test
    public void test_error_message_heisenbug_flow_handled() throws Exception {
        // simulate exception during route processing
        Mockito.when(mockHeisenBugger.process(Mockito.anyString()))
            .thenThrow(new FooBarException("it certainly went boom!"));
        
        mockOutgoingEndpoint.expectedMessageCount(0);
        
        // setup the test data
        Exchange exchangeIn = new DefaultExchange(context);
        exchangeIn.getMessage().setBody("heisenbug");
        
        // put test data on the route
        Exchange exchangeOut = camelProducer.send(exchangeIn);

        // check that all routes processed as expected
        mockOutgoingEndpoint.assertIsSatisfied();
        
        // checking the exceptions
        assertTrue(exchangeOut.isFailed());
        
        Exception exception = exchangeOut.getException();
        assertNotNull(exception);
        assertTrue(exception instanceof FooBarException);
        assertEquals("it certainly went boom!", exception.getMessage());
        
        exception = exchangeOut.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        assertNotNull(exception);
        assertTrue(exception instanceof FooBarException);
        assertEquals("it certainly went boom!", exception.getMessage());
        
        // verify the body
        String outBody = exchangeOut.getMessage().getBody(String.class);
        assertNotNull(outBody);
        assertEquals("heisenbug", outBody);
        assertFalse(outBody.equals("handled_exception_heisenbug"));
    }
    
    /**
     * will NOT use main route error handling
     */ 
    @Test
    public void test_error_message_heisenbug_flow_unhandled() throws Exception {

        // simulate exception during route processing
        Mockito.when(mockHeisenBugger.process(Mockito.anyString()))
            .thenThrow(new SpikeException("heisenbug went boom w/ spike exception!"));
        
        mockOutgoingEndpoint.expectedMessageCount(0);
        
        // setup the test data
        Exchange exchangeIn = new DefaultExchange(context);
        exchangeIn.getMessage().setBody("heisenbug");
        
        // put test data on the route
        Exchange exchangeOut = camelProducer.send(exchangeIn);

        // check that all routes processed as expected
        mockOutgoingEndpoint.assertIsSatisfied();
        
        // checking the exceptions
        assertTrue(exchangeOut.isFailed());
        
        Exception exception = exchangeOut.getException();
        assertNotNull(exception);
        assertTrue(exception instanceof SpikeException);
        assertEquals("heisenbug went boom w/ spike exception!", exception.getMessage());
        
        exception = exchangeOut.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        assertNotNull(exception);
        assertTrue(exception instanceof SpikeException);
        assertEquals("heisenbug went boom w/ spike exception!", exception.getMessage());
        
        // verify the body
        String outBody = exchangeOut.getMessage().getBody(String.class);
        assertNotNull(outBody);
        assertEquals("heisenbug", outBody);
        assertFalse(outBody.equals("unhandled_exception_heisenbug"));
    }
    
}
