# MCF Authentication with Singpass / Corppass

### Run Locally

```bash
./gradlew start
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
| APP_HOMEPAGE_URL                                               | Homepage of the app using svc-auth                     |
| **`Zipkin`**                                                   |                                                        |
| APP_INSTRUMENTATION_ZIPKIN_URL                                 | The hostname to the Zipkin server. For example, if you normally access the Zipkin server API at `https://zipkin.yourdomain.com/api/v1/spans`, specify the value as `https://zipkin.yourdomain.com`. |
| APP_INSTRUMENTATION_ZIPKIN_ENV                                 | Specifies the environment of the application. When this is specified ,it is appended to the service name for identification in the Zipkin UI. For example, if `"development"` is specified as the value, the resultant service name in Zipkin UI will be `"svc-auth-development"`. |
| **`Jwt Token`**                                                |                                                        |
| APP_TOKEN_PRIVATE_KEY                                          | Private key(pcks8 format) used to sign the Jwt         |
| APP_TOKEN_SIGNATURE_ALGORITHM                                  | Signature Algorithm used to sign Jwt. Only support RSA algo |
| APP_TOKEN_EXPIRATION_TIME                                      | How many milliseconds will the Jwt be valid for        |
| **`Singpass`**                                                 |                                                        |
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
| **`Corppass`**                                                 |                                                        |
| APP_CORPPASS_SERVICE_PROVIDER_PRIVATE_KEY                      | Same as Singpass                                       |
| APP_CORPPASS_SERVICE_PROVIDER_METADATA_PATH                    | Same as Singpass                                       |
| APP_CORPPASS_SERVICE_PROVIDER_METADATA_ID                      | Same as Singpass                                       |
| APP_CORPPASS_SERVICE_PROVIDER_LOGIN_URL                        | Same as Singpass                                       |
| APP_CORPPASS_IDENTITY_PROVIDER_HOST                            | Same as Singpass                                       |
| APP_CORPPASS_IDENTITY_PROVIDER_SERVICE_ID                      | Same as Singpass                                       |
| APP_CORPPASS_IDENTITY_PROVIDER_METADATA_PATH                   | Same as Singpass                                       |
| APP_CORPPASS_IDENTITY_PROVIDER_METADATA_ID                     | Same as Singpass                                       |
| APP_CORPPASS_IDENTITY_PROVIDER_ARTIFACT_SERVICE_PROXY_HOST     | Same as Singpass                                       |
| APP_CORPPASS_IDENTITY_PROVIDER_ARTIFACT_SERVICE_PROXY_PORT     | Same as Singpass                                       |
| APP_CORPPASS_IDENTITY_PROVIDER_ARTIFACT_SERVICE_PROXY_USERNAME | Same as Singpass                                       |
| APP_CORPPASS_IDENTITY_PROVIDER_ARTIFACT_SERVICE_PROXY_PASSWORD | Same as Singpass                                       |
| APP_CORPPASS_IDENTITY_PROVIDER_ARTIFACT_LIFETIME_CLOCK_SKEW    | Same as Singpass                                       |
| APP_CORPPASS_MOCK_USER_LIST_URL                                | Url to get a list of mock corppass users' info (dev, qa only) |


#### `How to convert RSA private key to pcks8 format`
```bash
openssl pkcs8 -topk8 -inform PEM -in private_key.pem -out private_key_pkcs8.pem -nocrypt
```
