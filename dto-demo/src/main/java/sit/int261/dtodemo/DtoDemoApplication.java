package sit.int261.dtodemo;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import sit.int261.dtodemo.properties.AppProperties;

@EnableConfigurationProperties({
        AppProperties.class
})
@SpringBootApplication
public class DtoDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DtoDemoApplication.class, args);
    }
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

}
