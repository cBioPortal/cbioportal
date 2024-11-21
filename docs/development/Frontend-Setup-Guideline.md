# Accessing Frontend on localhost with a Self-Signed Certificate

When running the frontend locally with a self-signed SSL certificate, most browsers will block access to `https://localhost` due to security policies. To bypass this and open localhost with a self-signed certificate, follow these steps to start your browser in insecure mode.

## Chrome (Insecure Mode)

1. Open a terminal or command prompt.

2. Start Chrome with the following command, which disables certificate checking and web security for the session:

**For macOS or Linux:**
   
    google-chrome --ignore-certificate-errors --disable-web-security --user-data-dir=/tmp/temp-chrome-profile 

    
**For Windows:**

    C:\ProgramFiles\Google\Chrome\Application\chrome.exe" --ignore-certificate-errors --disable-web-security --user-data-dir=C:\tmp\temp-chrome-profile 


This command opens Chrome in a temporary profile with relaxed security settings, allowing access to https://localhost without SSL warnings.

After testing, close the browser. Do not use this browser session for regular browsing as it is less secure.


## Firefox (Disable Certificate Checking)
 1. Open Firefox and navigate to about:config in the address bar.

 2. Modify the following settings:

    1. security.ssl.enable_ocsp_stapling → false

    2. security.cert_pinning.enforcement_level → 0

    3. network.stricttransportsecurity.preloadlist → false

    After applying these changes, you can access https://localhost with a self-signed certificate.


## Security Note
Starting a browser in insecure mode or disabling certificate checks can expose you to risks. These methods should be used strictly for local development and testing purposes. Close the browser after testing and reopen it in normal mode for daily browsing.








