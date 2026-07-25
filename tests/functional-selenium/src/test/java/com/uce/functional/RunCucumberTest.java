package com.uce.functional;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = "cucumber.plugin", value = "json:target/cucumber.json,pretty")
@ConfigurationParameter(key = "cucumber.publish.quiet", value = "true")
public class RunCucumberTest {
}
