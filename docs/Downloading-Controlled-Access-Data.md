# Downloading Controlled-Access Data

## [Introduction](introduction)
A user authentication token (data access token) is required for downloading controlled-access data programmatically from the cBioPortal. See [Obtaining an Authentication Token](#obtaining-an-authentication-token) for instructions on how to download a token and [Using Authentication Tokens](#using-authentication-tokens) for how to use it.

## [Obtaining an Authentication Token](obtaining-an-authentication-token)
To obtain an authentication token, simply select the `Download Token` option from the dropdown menu at the top right corner of the cBioPortal home page (where your username is displayed). Your token will be downloaded as file with the following format:

```
token: your-authentication-token
creation_date: YYYY-MM-DD HH:MM:SS
expiration_date: YYYY-MM-DD HH:MM:SS
```

_Note that the expiration date may vary between different controlled-access instances of the cBioPortal._

## [Using Authentication Tokens](using-authentication-tokens)
Once downloaded, the token can be sent in the `Authorization` header when making requests to the [cBioPortal web API](cBioPortal-Web-API.md).
```
Authorization: Bearer <token>
```
