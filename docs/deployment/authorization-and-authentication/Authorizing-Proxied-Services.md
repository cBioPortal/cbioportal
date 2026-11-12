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
| `401 Unauthorized` | The portal requires authentication and the request has none. |
| `403 Forbidden` | The caller may not read the study, the study does not exist, or it is being (re)imported. |

The endpoint never answers `404` for an unknown study. A proxy treats any status other than 2xx, 401 and 403 as an error, and a distinct status would reveal which studies exist.

A `204` response carries these headers:

| Header | Value |
|--------|-------|
| `X-Auth-Request-User` | The name of the authenticated user. Absent for anonymous callers, for example when `authenticate=false`. |
| `X-Auth-Request-Study` | The identifier of the study that was checked. |
| `Cache-Control` | `no-store` |

The check uses the same study permissions as the rest of the API (see [User Authorization](User-Authorization.md)). When `authenticate=false`, every existing study is readable and unknown studies are still refused.

## Sending credentials

The proxy passes the original request's credentials to cBioPortal: the session cookie for browser requests, or the `Authorization` header for clients that use [data access tokens](Authenticating-Users-via-Tokens.md). For browsers to send the session cookie, serve the protected service from the same host as cBioPortal.

## nginx

With [`auth_request`](https://nginx.org/en/docs/http/ngx_http_auth_request_module.html), nginx makes a subrequest before passing each request to the protected service. This example protects a tile server whose paths start with the study identifier, `/tiles/<studyId>/...`:

```nginx
location ~ ^/tiles/(?<study>[^/]+)/ {
    auth_request /_cbioportal_study_access;

    # Forward the user cBioPortal authenticated. proxy_set_header replaces any
    # X-Auth-Request-User header sent by the client, and omits it when empty.
    auth_request_set $auth_user $upstream_http_x_auth_request_user;
    proxy_set_header X-Auth-Request-User $auth_user;

    proxy_pass http://tile-server:8080;
}

location = /_cbioportal_study_access {
    internal;
    proxy_pass http://cbioportal:8080/api/studies/$study/access;
    proxy_pass_request_body off;
    proxy_set_header Content-Length "";
    proxy_set_header Cookie $http_cookie;
    proxy_set_header Authorization $http_authorization;
}
```

nginx returns `401` and `403` from the subrequest to the client unchanged and passes the request on after a `2xx`.

## ingress-nginx

On Kubernetes with [ingress-nginx](https://kubernetes.github.io/ingress-nginx/user-guide/nginx-configuration/annotations/#external-authentication), the same check is configured with annotations on the protected service's Ingress. ingress-nginx forwards the original request headers, including cookies, to `auth-url`, and copies the headers listed in `auth-response-headers` onto the request to the service:

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: tile-server
  annotations:
    nginx.ingress.kubernetes.io/use-regex: "true"
    nginx.ingress.kubernetes.io/auth-url: "http://cbioportal.default.svc.cluster.local:8080/api/studies/$study/access"
    nginx.ingress.kubernetes.io/auth-response-headers: "X-Auth-Request-User"
spec:
  ingressClassName: nginx
  rules:
    - host: cbioportal.example.org
      http:
        paths:
          - path: /tiles/(?<study>[^/]+)/
            pathType: ImplementationSpecific
            backend:
              service:
                name: tile-server
                port:
                  number: 8080
```

nginx interpolates `auth-url`, so `$study` is the named capture from the regex path.

## Trusting the headers

The protected service trusts `X-Auth-Request-User` because the proxy sets it. It must therefore be reachable **only through the proxy**: if clients can reach it directly, they can send any user name and skip the study check. Keep it off public networks, for example with a Kubernetes `NetworkPolicy` or by binding it to a private interface.
