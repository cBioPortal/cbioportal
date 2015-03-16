package org.mskcc.cbio.importer.darwin;

/**
 * Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
 * <p/>
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.
 * <p/>
 * Created by criscuof on 1/27/15.
 */

    import java.io.BufferedReader;
    import java.io.IOException;
    import java.io.InputStreamReader;
    import java.net.Authenticator;
    import java.net.InetAddress;
    import java.net.MalformedURLException;
    import java.net.PasswordAuthentication;
    import java.net.URL;

    public class TestDarwinExcelAccess {

        public static void main(String[] args) {

            try {

                // Sets the authenticator that will be used by the networking code
                // when a proxy or an HTTP server asks for authentication.
                Authenticator.setDefault(new CustomAuthenticator());

                URL url = new URL("http://darwin.mskcc.org/portal/cgi-bin/cognos_module?ui.name=DMP%20IDs%20to%20Darwin%20DEID&run.outputFormat=spreadsheetML&run.prompt=false&ui.action=run&b_action=cognosViewer&encoding=UTF-8&ui.object=%2fcontent%2fpackage%5b%40name%3d%27DAVInCI%27%5d%2ffolder%5b%40name%3d%27DataLine%20DAVInCI%20Portal%20Delivered%20Reports%27%5d%2ffolder%5b%40name%3d%27PTH9914%27%5d%2freport%5b%40name%3d%27DMP%20IDs%20to%20Darwin%20DEID%27%5d");

                // read text returned by server
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println(line);
                }
                in.close();

            }
            catch (MalformedURLException e) {
                System.out.println("Malformed URL: " + e.getMessage());
            }
            catch (IOException e) {
                System.out.println("I/O Error: " + e.getMessage());
            }

        }

        public static class CustomAuthenticator extends Authenticator {

            // Called when password authorization is needed
            protected PasswordAuthentication getPasswordAuthentication() {

                // Get information about the request
                String prompt = getRequestingPrompt();
                String hostname = getRequestingHost();
                InetAddress ipaddr = getRequestingSite();
                int port = getRequestingPort();

                String username = "username";
                String password = "password";

                // Return the information (a data holder that is used by Authenticator)
                return new PasswordAuthentication(username, password.toCharArray());

            }

        }


}
