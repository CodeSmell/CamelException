package codesmell.camel.routeconfig;

import codesmell.camel.CamelConstants;
import codesmell.exception.FooBarException;
import codesmell.exception.SpikeException;
import codesmell.mock.config.MockApplicationConfig;
import codesmell.service.FooBar;
import codesmell.service.Grokker;
import codesmell.testingutil.CamelExceptionAssert;
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

import static org.junit.jupiter.api.Assertions.*;

@CamelSpringBootTest
@SpringBootTest
@Import({
    MockApplicationConfig.class
})
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CamelExceptionWithConfiguratorRouteBuilderTest {

    @Autowired
    CamelContext context;
    
    @Produce(CamelConstants.MAIN_CONFIGURATOR_ENDPOINT_URI)
    private ProducerTemplate camelProducer;

    @EndpointInject(CamelConstants.MAIN_CONFIGURATOR_OUTGOING_ENDPOINT_URI)
    private MockEndpoint mockOutgoingEndpoint;

    @Autowired
    private FooBar mockFooBar;
    
    @Autowired
    private Grokker mockGrokker;
    
    @AfterEach
    public void cleanup() {
        Mockito.reset(mockFooBar, mockGrokker);
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
        CamelExceptionAssert.assertHandledExceptionIsEmpty(exchangeOut);
        CamelExceptionAssert.assertUnhandledExceptionIsEmpty(exchangeOut);
        
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
        Exception ex = CamelExceptionAssert.assertHandledExceptionIsFound(exchangeOut);
        CamelExceptionAssert.assertUnhandledExceptionIsEmpty(exchangeOut);
        assertTrue(ex instanceof FooBarException);
        assertEquals("boom!", ex.getMessage());
        
        // verify the body
        String outBody = exchangeOut.getMessage().getBody(String.class);
        assertNotNull(outBody);
        assertEquals("handled_foo_exception_foobar", outBody);
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
        Exception ex = CamelExceptionAssert.assertHandledExceptionIsFound(exchangeOut);
        assertTrue(ex instanceof SpikeException);
        assertEquals("boom!", ex.getMessage());
        
        ex = CamelExceptionAssert.assertUnhandledExceptionIsFound(exchangeOut);
        assertTrue(ex instanceof SpikeException);
        assertEquals("boom!", ex.getMessage());
        
        // verify the body
        String outBody = exchangeOut.getMessage().getBody(String.class);
        assertNotNull(outBody);
        assertEquals("unhandled_spike_exception_foobar", outBody);
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
        Exception ex = CamelExceptionAssert.assertHandledExceptionIsFound(exchangeOut);
        assertTrue(ex instanceof SpikeException);
        assertEquals("still can't handle the message", ex.getMessage());
        
        ex = CamelExceptionAssert.assertUnhandledExceptionIsFound(exchangeOut);
        assertTrue(ex instanceof SpikeException);
        assertEquals("still can't handle the message", ex.getMessage());
        
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
            .thenReturn("you grokked me with a configurator");
        
        mockOutgoingEndpoint.expectedMessageCount(1);
        
        // setup the test data
        Exchange exchangeIn = new DefaultExchange(context);
        exchangeIn.getMessage().setBody("grok");
        
        Exchange exchangeOut = camelProducer.send(exchangeIn);
        
        // check that all routes processed as expected
        mockOutgoingEndpoint.assertIsSatisfied();
        
        // checking the exceptions
        CamelExceptionAssert.assertHandledExceptionIsEmpty(exchangeOut);
        CamelExceptionAssert.assertUnhandledExceptionIsEmpty(exchangeOut);
        
        // verify the body
        String outBody = exchangeOut.getMessage().getBody(String.class);
        assertNotNull(outBody);
        assertEquals("you grokked me with a configurator", outBody);
    }
    
    /**
     * should use main route error handling
     */
    @Test
    public void test_error_message_grok_flow_handled() throws Exception {

        // simulate exception during route processing
        Mockito.when(mockGrokker.process(Mockito.anyString()))
            .thenThrow(new FooBarException("grok with configurator went boom!"));
        
        mockOutgoingEndpoint.expectedMessageCount(0);
        
        // setup the test data
        Exchange exchangeIn = new DefaultExchange(context);
        exchangeIn.getMessage().setBody("grok");
        
        // put test data on the route
        Exchange exchangeOut = camelProducer.send(exchangeIn);

        // check that all routes processed as expected
        mockOutgoingEndpoint.assertIsSatisfied();
        
        // checking the exceptions
        Exception ex = CamelExceptionAssert.assertHandledExceptionIsFound(exchangeOut);
        CamelExceptionAssert.assertUnhandledExceptionIsEmpty(exchangeOut);
        assertTrue(ex instanceof FooBarException);
        assertEquals("grok with configurator went boom!", ex.getMessage());
        
        // verify the body
        String outBody = exchangeOut.getMessage().getBody(String.class);
        assertNotNull(outBody);
        assertEquals("handled_foo_exception_grok", outBody);
    }
    
    
    /**
     * should use main route error handling
     */
    @Test
    public void test_error_message_grok_flow_unhandled() throws Exception {

        // simulate exception during route processing
        Mockito.when(mockGrokker.process(Mockito.anyString()))
            .thenThrow(new SpikeException("grok with configurator went boom w/ spike exception!"));
        
        mockOutgoingEndpoint.expectedMessageCount(0);
        
        // setup the test data
        Exchange exchangeIn = new DefaultExchange(context);
        exchangeIn.getMessage().setBody("grok");
        
        // put test data on the route
        Exchange exchangeOut = camelProducer.send(exchangeIn);

        // check that all routes processed as expected
        mockOutgoingEndpoint.assertIsSatisfied();
        
        // checking the exceptions
        Exception ex = CamelExceptionAssert.assertHandledExceptionIsFound(exchangeOut);
        assertTrue(ex instanceof SpikeException);
        assertEquals("grok with configurator went boom w/ spike exception!", ex.getMessage());
        
        ex = CamelExceptionAssert.assertUnhandledExceptionIsFound(exchangeOut);
        assertTrue(ex instanceof SpikeException);
        assertEquals("grok with configurator went boom w/ spike exception!", ex.getMessage());
        
        // verify the body
        String outBody = exchangeOut.getMessage().getBody(String.class);
        assertNotNull(outBody);
        assertEquals("unhandled_spike_exception_grok", outBody);
    }

}
