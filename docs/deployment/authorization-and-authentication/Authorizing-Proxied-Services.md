# Authorizing Proxied Services by Study

Services that run beside cBioPortal, such as image tile servers or annotation services, often serve data that belongs to a study. Instead of implementing cBioPortal's login and study permissions themselves, they can sit behind a reverse proxy that asks cBioPortal, on every request, whether the caller may read the study.

cBioPortal answers this question at:

```
GET /api/studies/{studyId}/access
```

The response has no body. The status is the answer:

| Status | Meaning |
|--------|---------|
| `204 No Content` | The caller may read the study. |
| `401 Unauthorized` | The portal requires authentication for the API and the request has none. Never returned with `authenticate=optional_oauth2`; see [Authentication modes](#authentication-modes). |
| `403 Forbidden` | The caller may not read the study, the study does not exist, or it is being (re)imported. |

The endpoint never answers `404` for an unknown study. A proxy treats any status other than 2xx, 401 and 403 as an error, and a distinct status would reveal which studies exist.

A `204` response carries these headers:

| Header | Value |
|--------|-------|
| `X-Auth-Request-User` | The name of the authenticated user; see [The user header](#the-user-header). Absent for anonymous callers. |
| `X-Auth-Request-Study` | The identifier of the study that was checked. |
| `Cache-Control` | `no-store` |

The check uses the same study permissions as the rest of the API (see [User Authorization](User-Authorization.md)). Where study permissions are enforced, a newly imported study is refused until cBioPortal's study permission cache includes it, which takes up to `cache.cancer_study_permission.ttl_seconds` (60 by default).

Because of `Cache-Control: no-store`, ingress-nginx's `auth-cache-key` and `auth-cache-duration` do not cache the answer: every proxied request makes one call to cBioPortal.

## Authentication modes

What the endpoint answers depends on the `authenticate` and `security.method_authorization_enabled` properties:

| Mode | Behavior |
|------|----------|
| `saml`, `oauth2`, `saml_plus_basic` | `401` without credentials. Otherwise `204` or `403` by study permission. |
| `false` | `204` for every existing study, with no `X-Auth-Request-User` header. Unsuitable when the protected service needs to know the user. |
| `optional_oauth2`, method authorization off | `204` for every existing study, for anonymous and logged-in callers alike. `X-Auth-Request-User` is set only for logged-in callers. |
| `optional_oauth2`, method authorization on | Never `401`. Anonymous callers get `403`, or `204` for studies in `always_show_study_group`. A proxy therefore cannot tell an anonymous caller apart from a logged-in one without access, and cannot redirect anonymous callers to the login page. |

## The user header

`X-Auth-Request-User` is the name of the authenticated principal, which depends on how the caller logged in:

| Login | Value |
|-------|-------|
| SAML | The SAML `NameID`. Depending on the identity provider this may be transient or opaque rather than a user name or email address. |
| OAuth2 session | The attribute named by the client registration's `user-name-attribute`. |
| OAuth2 data access token | The `sub` claim of the token. |
| UUID data access token | The user name stored with the token. |
| Basic authentication (`saml_plus_basic`) | The user name. |

A client that calls the protected service with a [data access token](Authenticating-Users-via-Tokens.md) can therefore be named differently than the same person in the browser. If cBioPortal authenticates a caller whose name is empty, it answers `403` rather than `204` with an empty header.

## Sending credentials

The proxy passes the original request's credentials to cBioPortal: the session cookie for browser requests, or the `Authorization` header for clients that use [data access tokens](Authenticating-Users-via-Tokens.md). For browsers to send the session cookie, serve the protected service from the same host as cBioPortal.

## nginx

With [`auth_request`](https://nginx.org/en/docs/http/ngx_http_auth_request_module.html), nginx makes a subrequest before passing each request to the protected service. This example protects a tile server whose paths start with the study identifier, `/tiles/<studyId>/...`:

```nginx
upstream cbioportal {
    server cbioportal:8080;
}

location ~ ^/tiles/(?<study>[A-Za-z0-9_.-]+)/ {
    auth_request /_cbioportal_study_access;

    # Forward what cBioPortal checked. proxy_set_header replaces any header of
    # the same name sent by the client, and omits it when the value is empty.
    auth_request_set $auth_user $upstream_http_x_auth_request_user;
    auth_request_set $auth_study $upstream_http_x_auth_request_study;
    proxy_set_header X-Auth-Request-User $auth_user;
    proxy_set_header X-Auth-Request-Study $auth_study;

    # The tile server does not need cBioPortal's credentials.
    proxy_set_header Cookie "";
    proxy_set_header Authorization "";

    proxy_pass http://tile-server:8080;
}

location = /_cbioportal_study_access {
    internal;
    proxy_method GET;
    proxy_pass http://cbioportal/api/studies/$study/access;
    proxy_pass_request_body off;
    proxy_set_header Content-Length "";
    proxy_set_header Cookie $http_cookie;
    proxy_set_header Authorization $http_authorization;
}
```

nginx returns `401` and `403` from the subrequest to the client unchanged and passes the request on after a `2xx`. `proxy_method GET` makes the check a `GET` whatever the method of the original request.

`proxy_pass` with a variable resolves its host name at request time, which needs a `resolver` unless the name is an `upstream` block, as here.

If the protected service takes the study from a query parameter instead of the path, extract it with a `map` on `$request_uri`, since `$arg_*` variables are empty inside the auth subrequest.

## ingress-nginx

On Kubernetes with [ingress-nginx](https://kubernetes.github.io/ingress-nginx/user-guide/nginx-configuration/annotations/#external-authentication), the same check is configured with annotations on the protected service's Ingress. ingress-nginx forwards the original request headers, including cookies, to `auth-url`, and sets the headers listed in `auth-response-headers` on the request to the service, replacing any the client sent:

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: tile-server
  annotations:
    nginx.ingress.kubernetes.io/use-regex: "true"
    nginx.ingress.kubernetes.io/auth-url: "http://cbioportal.default.svc.cluster.local:8080/api/studies/$study/access"
    nginx.ingress.kubernetes.io/auth-method: "GET"
    nginx.ingress.kubernetes.io/auth-response-headers: "X-Auth-Request-User,X-Auth-Request-Study"
spec:
  ingressClassName: nginx
  rules:
    - host: cbioportal.example.org
      http:
        paths:
          - path: /tiles/(?<study>[A-Za-z0-9_.-]+)/
            pathType: ImplementationSpecific
            backend:
              service:
                name: tile-server
                port:
                  number: 8080
```

nginx interpolates `auth-url`, so `$study` is the named capture from the regex path.

ingress-nginx has no per-Ingress annotation that removes request headers, so the session cookie and `Authorization` header also reach the service. Clear them with a `configuration-snippet` (`proxy_set_header Cookie "";` and `proxy_set_header Authorization "";`) where snippet annotations are allowed, which they are not by default; or with the controller-wide `proxy-set-headers` ConfigMap option, which applies to every Ingress of that controller, cBioPortal's included; or make the service ignore them.

## Trusting the headers

The protected service trusts `X-Auth-Request-User` and `X-Auth-Request-Study` because the proxy sets them. Every header the service trusts must be set by the proxy, overwriting whatever the client sent; a header the proxy only passes through can be forged.

The service must also be reachable **only through the proxy**: if clients can reach it directly, they can send any user name and skip the study check. Keep it off public networks, for example with a Kubernetes `NetworkPolicy` or by binding it to a private interface.

The proxy and the service parse the request URI separately, so they can disagree about which study it names. nginx matches locations against the decoded and normalized path, but passes the original URI on: for `/tiles/restricted%2F..%2Fallowed/0.jpg` it checks `allowed`, while a service that decodes the path first sees `restricted`. Restrict the captured study to the characters study identifiers use, as the examples do, and have the service compare the study it parsed from the request with `X-Auth-Request-Study`, refusing the request when they differ.

The protected service does not need cBioPortal's credentials. Unless it calls cBioPortal itself, remove the `Cookie` and `Authorization` headers from requests to it, as in the nginx example.
