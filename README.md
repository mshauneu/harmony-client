# Epsilon Harmony client

An [Epsilon](https://us.epsilon.com/) **Harmony** client for [Java](https://www.java.com/en/) applications.

## Dependency

* [OkHttp](http://square.github.io/okhttp/) - Http client for [Epsilon](https://epsilon.com/) mail services
* [Jackson](https://github.com/FasterXML/jackson) - JSON library for Java
* [SLF4J](https://www.slf4j.org/) - Simple Logging Facade 

## Building

To build library use:

```sh
./mvnw package -DskipTests
```

## Testing

Tests could be run by following command:

```sh
./mvnw test
```

## Using

`HarmonyClient` should be *shared*. It performs best when you create only a *single* instance and reuse it for all of your calls. This is because each client holds its own connection pool and thread pools. Reusing connections and threads reduces latency and saves memory.

First you need to create a shared instance with default settings clientId, userName and passwords are mandatory to supply, others settings could be overridden, see `HarmonyClient.Builder` API.

```Java
public final HarmonyClient client = new HarmonyClient.Builder()
  .withClientId("<client_id>")
  .withClientPass("<client_pass>")
  .withUserName("<user_name>")
  .withUserPass("<user_pass>")
  .build();
```

Shutdown isn't necessary.

### Send using Java API 

Prepare a request with at least one recipient with at least one mail attribute (according to Epsilon) every email should have at least one Recipient with at least one attribute.

```java
SendMailRequest sendMailRequest = new SendMailRequest(
  "<message_id>",
  new SendMailRequest.Recipient("<email@test.com>",
    new SendMailRequest.Attribute("<attr_name>", "<attr_value>")
));
//send email synchronously
try (SendMailResponse sendMailResponse = harmonyClient.sendMail(aolOrgId, sendMailRequest).get()) {
  // process response
}

// or asynchronously
harmonyClient.sendMail(aolOrgId, sendMailRequest).handle((r, e) -> {
  if (e != null) {
    // deal with error
  } else {
    // process response
  }
});
```

### SpringBoot integration

1. Create configuration for HarmonyClient:

```java
@Configuration
@ConfigurationProperties(prefix="harmony")
public class HarmonyClientConfig {
  private String clientId;
  private String clientPassword;
  private String userName;
  private String userPassword;
  // getter and setters here

  @Bean
  public harmonyClient() {
    return new HarmonyClient.Builder()
      .withClientId(clientId)
      .withClientPass(clientPassword)
      .withUserName(userName)
      .withUserPass(userPassword)
      .build();
  }
}
```

2. Provide passwords in `application.properties`

```sh
harmony.client-id="<client-id>"
harmony.client-password="<client-password>"
harmony.user-name="<user-name>"
harmony.user-password="<user-password>"
```

3. Inject it into your service

```java
@Autowired
private final HarmonyClient harmonyClient;
```

## Contributing

To generate `eclipse` project files use:
```sh
./mvnw eclipse:eclipse
```
To generate `idea` project files use:
```sh
./mvnw idea:idea
```
Or you can import the `maven` project directly from your favorite `IDE`.
