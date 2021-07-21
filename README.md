# di-ipv-core-back

The core backend service for IPV.

## Running locally

First generate some test keys,

```shell
./scripts/generate-key-pair-for-dev.sh
```

Add the keys as an environment variables, example below:

```shell
export DI_IPV_CORE_BACK_SIGNING_KEY=$(cat signing-key.pem)
export DI_IPV_CORE_BACK_SIGNING_CERT=$(cat signing-cert.pem)
```

Then build or run the service with gradle by either executing `bootRun` task or `build`.

## Environment Variables

| Environment Variable | Description |
|-|-|
| `DI_IPV_CORE_BACK_SIGNING_KEY` | JWS signing key |
| `DI_IPV_CORE_BACK_SIGNING_CERT` | JWS signing certificate |
| `DI_IPV_CORE_BACK_REDIS_ENDPOINT` | Redis endpoint URI |
| `DI_IPV_CORE_BACK_GPG_45_ENDPOINT` | GPG-45 service endpoint URI |
| `PORT` | Port to run this service on (default is 8081) |

## Paths

| Path | Description |
|-|-|
| `/start-session` | Creates a new session and persists the incoming authorization request in session. |
| `/{session-id}/return` | Returns the user session data to be passed to the orchestrator via the initial /oauth2/authorize callback. |
| `/{session-id}/get-route` | Gets the next route for the IPV frontend based on the session data. |
| `/{session-id}/add-evidence` | Adds a new piece of evidence to the identity verification bundle, which gets calculated and matched against the GPG45 engine. |
| `/oauth2/token` | Exchange access code received after returning from IPV for an access token. |
| `/oauth2/userinfo` | Retrieves the userinfo that is tied to the access token. |
| `/.well-known/jwks.json` | Contains public keys used for signing JWS. |
