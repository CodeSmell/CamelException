package camel.exception.mock.config;

import camel.exception.service.FooBar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import static org.mockito.Mockito.mock;

@Configuration
@PropertySource(value = "classpath:application.test.properties")
public class MockApplicationConfig {

    @Bean
    public FooBar buildFooBar() {
        return mock(FooBar.class);
    }

}
