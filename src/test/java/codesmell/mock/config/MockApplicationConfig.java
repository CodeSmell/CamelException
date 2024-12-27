package codesmell.mock.config;

import codesmell.service.FooBar;
import codesmell.service.Grokker;
import codesmell.service.HeisenBugger;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class MockApplicationConfig {

    @MockBean
    FooBar mockFooBar;
    
    @MockBean
    Grokker mockGrokker;
    
    @MockBean
    HeisenBugger mockHeisenBugger;
}
