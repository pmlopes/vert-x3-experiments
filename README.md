# Vert.x3 API Documentation Testing

This experiment shows how one can easily test API contracts defined in [RAML](http://raml.org/) and implemented in
 [Vert.x](http://vertx.io).

 The API RAML document lives at `src/main/resources/webroot/api/hello.raml` the location is not required to be that one,
 it just a location where it can be shared both by the web console and the unit tests. If only unit tests were to
 access the RAML document then the file could just be located at `src/test/resources`.

 In order to test this, first build the project and run:

 ```
mvn clean install
java -jar target/vertx-raml-1.0.0-SNAPSHOT-fat.jar
 ```

After this open a browser to: `http://localhost:8080?raml=/api/hello.raml`.