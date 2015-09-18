package io.vertx.blog;

import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.RamlLoaders;
import guru.nidi.ramltester.jaxrs.CheckingWebTarget;
import guru.nidi.ramltester.junit.RamlMatchers;
import io.vertx.util.Runner;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class APITest {

  private static final RamlDefinition api = RamlLoaders.fromClasspath()
      .load("/webroot/api/hello.raml")
      .assumingBaseUri("http://localhost:8080/");

  private ResteasyClient client = new ResteasyClientBuilder().build();
  private CheckingWebTarget checking;

  @BeforeClass
  public static void bootApp() {
    Runner.run(App.class);
  }

  @Before
  public void createTarget() {
    checking = api.createWebTarget(client.target("http://localhost:8080"));
  }

  @Test
  public void testHelloEndpoint() {
    checking.path("/hello").request().get();
    Assert.assertThat(checking.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
