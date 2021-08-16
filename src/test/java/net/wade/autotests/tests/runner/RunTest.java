package net.wade.autotests.tests.runner;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = {
                "pretty",
                "json:target/cucumber-reports/cucumber.json",
                "io.qameta.allure.cucumber5jvm.AllureCucumber5Jvm"
        },
        features = "src/test/features",
        glue = "net/wade/autotests/tests/steps",
        strict = true
)
public class RunTest {
}
