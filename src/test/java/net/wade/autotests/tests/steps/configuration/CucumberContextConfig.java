package net.wade.autotests.tests.steps.configuration;

import io.cucumber.spring.CucumberContextConfiguration;
import net.wade.autotests.configuration.TestContextConfiguration;
import org.springframework.test.context.ContextConfiguration;

@CucumberContextConfiguration
@ContextConfiguration(classes = TestContextConfiguration.class)
public class CucumberContextConfig {
}
