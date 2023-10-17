package it.pagopa.pn.external.registries;

import it.pagopa.pn.commons.configs.listeners.TaskIdApplicationListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@Slf4j
public class PnExternalRegistriesApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(PnExternalRegistriesApplication.class);
        app.addListeners(new TaskIdApplicationListener());
        app.run(args);
    }


    @RestController
    @RequestMapping("/")
    public static class RootController {

        @GetMapping("/")
        public String home() {
            return "";
        }
    }

}
