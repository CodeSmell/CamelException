package codesmell.config;

import codesmell.service.DefaultFooBar;
import codesmell.service.FooBar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@ComponentScan(basePackages = "camel.kafka.config")
@PropertySource(value = "classpath:application.properties")
public class ApplicationConfig {

    @Bean
    public FooBar buildFooBar() {
        return new DefaultFooBar();
    }

//    @Bean
//    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
//        return new PropertySourcesPlaceholderConfigurer();
//    }

}