package xyz.jetdrone.detour.impl;

import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import org.junit.Test;

import static org.junit.Assert.*;

public class NodeTest {

  static class TestNeedle implements Context<String> {

    public final String route;

    public final MultiMap params = MultiMap.caseInsensitiveMultiMap();
    public final MultiMap verify = MultiMap.caseInsensitiveMultiMap();

    TestNeedle(String route) {
      this.route = route;
    }

    @Override
    public String getVerb() {
      return null;
    }

    @Override
    public String getPath() {
      return route;
    }

    public TestNeedle test(String name, String value) {
      this.verify.add(name, value);
      return this;
    }

    public TestNeedle addParam(String name, String value) {
      this.params.add(name, value);
      return this;
    }
  }


  final Handler noOp = (x) -> {
  };

  @Test
  public void testAddAndGet() {
    final Node tree = new Node();

    final String[] routes = {
      "/hi",
      "/contact",
      "/co",
      "/c",
      "/a",
      "/ab",
      "/doc/",
      "/doc/node_faq.html",
      "/doc/node1.html",
      "/α",
      "/β"
    };

    for (String route : routes) {
      tree.addRoute(route, noOp);
    }

//    tree.printTree();

    final String[] goodTestData = {
      "/a",
      "/hi",
      "/contact",
      "/co",
      "/ab",
      "/α",
      "/β"
    };

    final String[] badTestData = {
      "/",
      "/con",
      "/cona",
      "/no"
    };

    for (String route : goodTestData) {
      final Handler[] needle = tree.search(new TestNeedle(route));
      assertNotNull(needle);
    }

    for (String route : badTestData) {
      final Handler[] needle = tree.search(new TestNeedle(route));
      assertNull(needle);
    }
  }

  @Test
  public void testWildcard() {
    final Node tree = new Node();
    final String[] routes = {
      "/",
      "/cmd/:tool/:sub",
      "/cmd/:tool/",
      "/src/*filepath",
      "/search/",
      "/search/:query",
      "/user_:name",
      "/user_:name/about",
      "/files/:dir/*filepath",
      "/doc/",
      "/doc/node_faq.html",
      "/doc/node1.html",
      "/info/:user/public",
      "/info/:user/project/:project"
    };

    for (String route : routes) {
      tree.addRoute(route, noOp);
    }

    // tree.printTree();

    final TestNeedle[] foundData = {
      new TestNeedle("/"),
      new TestNeedle("/cmd/test/").test("tool", "test"),
      new TestNeedle("/cmd/test/3").test("tool", "test").test("sub", "3"),
      new TestNeedle("/src/").test("filepath", "/"),
      new TestNeedle("/src/some/file.png").test("filepath", "/some/file.png"),
      new TestNeedle("/search/"),
      new TestNeedle("/search/中文").test("query", "中文"),
      new TestNeedle("/user_noder").test("name", "noder"),
      new TestNeedle("/user_noder/about").test("name", "noder"),
      new TestNeedle("/files/js/inc/framework.js").test("dir", "js").test("filepath", "/inc/framework.js"),
      new TestNeedle("/info/gordon/public").test("user", "gordon"),
      new TestNeedle("/info/gordon/project/node").test("user", "gordon").test("project", "node")
    };

    for (TestNeedle testNeedle : foundData) {
      final Handler[] needle = tree.search(testNeedle);
      assertNotNull(needle);
      // TODO: properly compare the multimap
      assertEquals(testNeedle.params.toString(), testNeedle.verify.toString());
    }

    final TestNeedle[] noHandlerData = {
      new TestNeedle("/cmd/test").test("tool", "test"),
      new TestNeedle("/search/中文/").test("query", "中文")
    };

    for (TestNeedle testNeedle : noHandlerData) {
      final Handler[] needle = tree.search(testNeedle);
      assertNull(needle);
      // TODO: properly compare the multimap
      assertEquals(testNeedle.params.toString(), testNeedle.verify.toString());
    }
  }
}
