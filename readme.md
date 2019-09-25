# MCF Authentication with Singpass / Corppass

### Run Locally

```bash
./gradlew bootRun
# default port is set to 8000
```

### Run Test

```bash
./gradlew test
```

### Build Docker Image

```bash
./gradlew build docker
```

### Environment Variables
|                                                                |                                                        |
|----------------------------------------------------------------|--------------------------------------------------------|
| SPRING_PROFILES_ACTIVE                                         | specify which Environment are active.                  |
| **`Zipkin`**                                                   |                                                        |
| APP_INSTRUMENTATION_ZIPKIN_URL                                 | The hostname to the Zipkin server. For example, if you normally access the Zipkin server API at `https://zipkin.yourdomain.com/api/v1/spans`, specify the value as `https://zipkin.yourdomain.com`. |
| APP_INSTRUMENTATION_ZIPKIN_ENV                                 | Specifies the environment of the application. When this is specified ,it is appended to the service name for identification in the Zipkin UI. For example, if `"development"` is specified as the value, the resultant service name in Zipkin UI will be `"svc-auth-development"`. |
| **`Jwt Token`**                                                |                                                        |
| APP_TOKEN_PRIVATE_KEY                                          | Private key(pcks8 format) used to sign the Jwt         |
| APP_TOKEN_SIGNATURE_ALGORITHM                                  | Signature Algorithm used to sign Jwt. Only support RSA algo |
| APP_TOKEN_ENCRYPTION_JWK                                       | Json Web Key used to encrypt the content of JWT. Only support algo using AES-CBC and HMAC-SHA2 |
| APP_TOKEN_EXPIRATION_TIME                                      | How many milliseconds will the Jwt be valid for        |
| APP_TOKEN_PLUGIN_JAR_FILE_URL                                  | The URL of the plugin jar file                         |
| APP_TOKEN_PLUGIN_CLASS_PATH                                    | The class path of the plugin class to be instantiated  |
| **`Singpass`**                                                 |                                                        |
| APP_SINGPASS_HOMEPAGE_URL                                      | Homepage of the app using svc-auth for singpass        |
| APP_SINGPASS_SERVICE_PROVIDER_PRIVATE_KEY                      | Private key(pcks8 format) used by svc-auth to sign during artifact resolve and decrypt assertion |
| APP_SINGPASS_SERVICE_PROVIDER_METADATA_PATH                    | Service provider metadata file path                    |
| APP_SINGPASS_SERVICE_PROVIDER_METADATA_ID                      | ID to differiate between different service provider metadata |
| APP_SINGPASS_SERVICE_PROVIDER_LOGIN_URL                        | Login url of the app using svc-auth (url that the Jwt will be posted to) |
| APP_SINGPASS_IDENTITY_PROVIDER_HOST                            | Singpass url domain                                    |
| APP_SINGPASS_IDENTITY_PROVIDER_SERVICE_ID                      | Service ID registered with Singpass                    |
| APP_SINGPASS_IDENTITY_PROVIDER_METADATA_PATH                   | Identity provider metadata file path                   |
| APP_SINGPASS_IDENTITY_PROVIDER_METADATA_ID                     | ID to differiate between different service provider metadata |
| APP_SINGPASS_IDENTITY_PROVIDER_ARTIFACT_SERVICE_PROXY_HOST     | Whitelisted proxy host to resolve artifact with Singpass |
| APP_SINGPASS_IDENTITY_PROVIDER_ARTIFACT_SERVICE_PROXY_PORT     | Whitelisted proxy port to resolve artifact with Singpass |
| APP_SINGPASS_IDENTITY_PROVIDER_ARTIFACT_SERVICE_PROXY_USERNAME | Whitelisted proxy username to resolve artifact with Singpass |
| APP_SINGPASS_IDENTITY_PROVIDER_ARTIFACT_SERVICE_PROXY_PASSWORD | Whitelisted proxy password to resolve artifact with Singpass |
| APP_SINGPASS_IDENTITY_PROVIDER_ARTIFACT_LIFETIME_CLOCK_SKEW    | Allowed time different to validate resolved artifact lifetime |
| APP_SINGPASS_ADDITIONAL_INFO_REQUEST_URL                       | Url to fetch additional info into the token            |
| APP_SINGPASS_ADDITIONAL_INFO_REQUEST_HTTP_METHOD               | Http method use to fetch the additional info url e.g GET/POST |
| APP_SINGPASS_ADDITIONAL_INFO_REQUEST_BODY                      | Any body to be passed in for fetch additional info     |
| APP_SINGPASS_ADDITIONAL_INFO_REQUEST_STATIC_JSON               | Static JSON payload to be added into token             |
| **`Corppass`**                                                 |                                                        |
| APP_CORPPASS_HOMEPAGE_URL                                      | Same as Singpass but for Corppass                      |
| APP_CORPPASS_SERVICE_PROVIDER_PRIVATE_KEY                      | Same as Singpass but for Corppass                      |
| APP_CORPPASS_SERVICE_PROVIDER_METADATA_PATH                    | Same as Singpass but for Corppass                      |
| APP_CORPPASS_SERVICE_PROVIDER_METADATA_ID                      | Same as Singpass but for Corppass                      |
| APP_CORPPASS_SERVICE_PROVIDER_LOGIN_URL                        | Same as Singpass but for Corppass                      |
| APP_CORPPASS_IDENTITY_PROVIDER_HOST                            | Same as Singpass but for Corppass                      |
| APP_CORPPASS_IDENTITY_PROVIDER_SERVICE_ID                      | Same as Singpass but for Corppass                      |
| APP_CORPPASS_IDENTITY_PROVIDER_METADATA_PATH                   | Same as Singpass but for Corppass                      |
| APP_CORPPASS_IDENTITY_PROVIDER_METADATA_ID                     | Same as Singpass but for Corppass                      |
| APP_CORPPASS_IDENTITY_PROVIDER_ARTIFACT_SERVICE_PROXY_HOST     | Same as Singpass but for Corppass                      |
| APP_CORPPASS_IDENTITY_PROVIDER_ARTIFACT_SERVICE_PROXY_PORT     | Same as Singpass but for Corppass                      |
| APP_CORPPASS_IDENTITY_PROVIDER_ARTIFACT_SERVICE_PROXY_USERNAME | Same as Singpass but for Corppass                      |
| APP_CORPPASS_IDENTITY_PROVIDER_ARTIFACT_SERVICE_PROXY_PASSWORD | Same as Singpass but for Corppass                      |
| APP_CORPPASS_IDENTITY_PROVIDER_ARTIFACT_LIFETIME_CLOCK_SKEW    | Same as Singpass but for Corppass                      |
| APP_CORPPASS_MOCK_USER_LIST_URL                                | Url to get a list of mock corppass users' info (dev, qa only) |
| APP_CORPPASS_ADDITIONAL_INFO_REQUEST_URL                       | Url to fetch additional info into the token            |
| APP_CORPPASS_ADDITIONAL_INFO_REQUEST_HTTP_METHOD               | Http method use to fetch the additional info url e.g GET/POST |
| APP_CORPPASS_ADDITIONAL_INFO_REQUEST_BODY                      | Any body to be passed in for fetch additional info     |
| APP_CORPPASS_ADDITIONAL_INFO_REQUEST_STATIC_JSON               | Static JSON payload to be added into token             |
| **`Service`**                                                  |                                                        |
| APP_SERVICE_SERVICES_FOLDER_PATH                               | Url to configuration details of service in yaml file |
| APP_SERVICE_SIGNATURE_LIFETIME_CLOCK_SKEW                      | Allowed time different to validate `nonce` parameter in signature payload |

#### Services YAML Configuaration

For each service that need to interface with `SVC-AUTH` for a valid JWT token, create a yaml configuration file in `resources/services` directory
Each file should contain the guid identifier to the service, public key to decode and verify signature sent by the service and the payload to insert into the returned JWT token

```
// Arbitrary identifier to locate yaml configuration file of requesting service
// The guid can be generated either by the hash of the service name or a timestamp
// The filename of the yaml file follow the value of the guid `${guid}.yaml`
guid: ...
// public rsa key to verify signature sent by requesting service
public-key: ...
// As required by service. Can be anything. The payload will be inserted in the issued JWT as it is
payload:
  // Sample payload content
  userId: api-mailer
  authorization:
    - id: '*'
      scopes:
        - jobalert:read
```


#### `How to convert RSA private key to pcks8 format`
```bash
openssl pkcs8 -topk8 -inform PEM -in private_key.pem -out private_key_pkcs8.pem -nocrypt
```

#### `Creating new asymmetric key pairs for new services`
Generate private key:
```bash
  openssl genpkey -algorithm RSA -out private_key.pem -pkeyopt rsa_keygen_bits:512
```
Generate public key using the private key:
```bash
  openssl rsa -pubout -in private_key.pem -out public_key.pem
```

### Actuator

By default /health and /info are accessible via WEB

Refer to https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-endpoints.html

Install it by adding this line in build.gradle

```
compile("org.springframework.boot:spring-boot-starter-actuator:$spring_boot_version")
```

By default all endpoints are set to false

./src/main/resources/application.yml

```
management:
    endpoints:
        enabled-by-default: false
    endpoint:
        health:
            enabled: true
```

### Troubleshooting

**Invalid Metadata or Private Key**

```
Remember the metadata/and private key body shouldn't contain "\n" or other special characters.
(This issue is applicable to Nectar)

echo -n "<?xml>..." > /sp_singpass.xml
```
