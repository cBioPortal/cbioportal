# saml-idp

A test SAML Identity Provider. It is used by cbioportal integration tests.
It's modified version of [a saml IDP sample code](https://github.com/spring-projects/spring-security-saml/tree/develop/samples/boot/simple-identity-provider) of the spring security.
`application.yml` specify a test user email that is added to assertions.

## License

The sample code is distributed under Apache License, Version 2.0.

## Known limitations

- The IDP does not sign assertions. That's why we had to set the following flag for the test cbioportal application: 

```$xml
    <!--FIXME Our test saml idp does not sing assertions for some reason-->
    <saml.sp.metadata.wantassertionsigned>false</saml.sp.metadata.wantassertionsigned>
```
