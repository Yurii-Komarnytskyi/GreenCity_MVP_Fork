package greencity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.core.env.ConfigurableEnvironment;

@SpringBootApplication
@EnableCaching
public class GreenCityApplication {
    /**
     * Main method of SpringBoot app.
     */
    public static void main(String[] args) {
        SpringApplication.run(GreenCityApplication.class, args);
    	
//    	try {
//            System.out.println("Raw DATASOURCE_URL: " + System.getenv("DATASOURCE_URL"));
//            System.out.println("Raw DATASOURCE_USER: " + System.getenv("DATASOURCE_USER"));
//            System.out.println("Raw DATASOURCE_PASSWORD: " + System.getenv("DATASOURCE_PASSWORD"));
//
//            SpringApplication app = new SpringApplication(GreenCityApplication.class);
//            app.setAdditionalProfiles("dev");
//            app.run(args);
////            ConfigurableEnvironment env = app.run(args).getEnvironment();
////            System.out.println("Pre-run Spring URL: " + env.getProperty("spring.datasource.url"));
////            System.out.println("Pre-run Spring User: " + env.getProperty("spring.datasource.username"));
////            System.out.println("Pre-run Spring Password: " + env.getProperty("spring.datasource.password"));
//
////            app.run(args);
//        } catch (Exception e) {
//            System.err.println("Startup failed:");
//            e.printStackTrace();
//        }
    }
    
 
}
