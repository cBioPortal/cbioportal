/**
 * Copyright (c) 2000-2008 Liferay, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.mskcc.cbio.portal.util;

import org.apache.log4j.Logger;

/**
 *
 *
 * @author Brian Wing Shun Chan
 *
 */
public class ServerDetector {
    private static final Logger logger = Logger.getLogger(ServerDetector.class);
    private static boolean isInstanceInitialized = false;
    private static ServerDetector instance = new ServerDetector();

    private String serverId;
    private Boolean geronimo;
    private Boolean glassfish;
    private Boolean jBoss;
    private Boolean jetty;
    private Boolean jonas;
    private Boolean oc4j;
    private Boolean orion;
    private Boolean pramati;
    private Boolean resin;
    private Boolean rexIP;
    private Boolean tomcat;
    private Boolean webLogic;
    private Boolean webSphere;

    public static final String GERONIMO_CLASS =
        "/org/apache/geronimo/system/main/Daemon.class";

    public static final String GERONIMO_ID = "geronimo";

    public static final String GLASSFISH_ID = "glassfish";

    public static final String GLASSFISH_SYSTEM_PROPERTY =
        "com.sun.aas.instanceRoot";

    public static final String JBOSS_CLASS = "/org/jboss/Main.class";

    public static final String JBOSS_ID = "jboss";

    public static final String JETTY_CLASS = "/org/mortbay/jetty/Server.class";

    public static final String JETTY_ID = "jetty";

    public static final String JONAS_CLASS =
        "/org/objectweb/jonas/server/Server.class";

    public static final String JONAS_ID = "jonas";

    public static final String OC4J_CLASS = "oracle.oc4j.util.ClassUtils";

    public static final String OC4J_ID = "oc4j";

    public static final String ORION_CLASS =
        "/com/evermind/server/ApplicationServer.class";

    public static final String ORION_ID = "orion";

    public static final String PRAMATI_CLASS = "/com/pramati/Server.class";

    public static final String PRAMATI_ID = "pramati";

    public static final String RESIN_CLASS =
        "/com/caucho/server/resin/Resin.class";

    public static final String RESIN_ID = "resin";

    public static final String REXIP_CLASS = "/com/tcc/Main.class";

    public static final String REXIP_ID = "rexip";

    public static final String TOMCAT_BOOTSTRAP_CLASS =
        "/org/apache/catalina/startup/Bootstrap.class";

    public static final String TOMCAT_EMBEDDED_CLASS =
        "/org/apache/catalina/startup/Embedded.class";

    public static final String TOMCAT_ID = "tomcat";

    public static final String WEBLOGIC_CLASS = "/weblogic/Server.class";

    public static final String WEBLOGIC_ID = "weblogic";

    public static final String WEBSPHERE_CLASS =
        "/com/ibm/websphere/product/VersionInfo.class";

    public static final String WEBSPHERE_ID = "websphere";

    public static String getServerId() {
        ServerDetector sd = instance;

        if (!isInstanceInitialized) {
            if (isGeronimo()) {
                sd.serverId = GERONIMO_ID;
            } else if (isGlassfish()) {
                sd.serverId = GLASSFISH_ID;
            } else if (isJBoss()) {
                sd.serverId = JBOSS_ID;
            } else if (isJOnAS()) {
                sd.serverId = JONAS_ID;
            } else if (isOC4J()) {
                sd.serverId = OC4J_ID;
            } else if (isOrion()) {
                sd.serverId = ORION_ID;
            } else if (isPramati()) {
                sd.serverId = PRAMATI_ID;
            } else if (isResin()) {
                sd.serverId = RESIN_ID;
            } else if (isRexIP()) {
                sd.serverId = REXIP_ID;
            } else if (isWebLogic()) {
                sd.serverId = WEBLOGIC_ID;
            } else if (isWebSphere()) {
                sd.serverId = WEBSPHERE_ID;
            }

            if (isJetty()) {
                if (sd.serverId == null) {
                    sd.serverId = JETTY_ID;
                } else {
                    sd.serverId += "-" + JETTY_ID;
                }
            } else if (isTomcat()) {
                if (sd.serverId == null) {
                    sd.serverId = TOMCAT_ID;
                } else {
                    sd.serverId += "-" + TOMCAT_ID;
                }
            }

            if (sd.serverId == null && logger.isInfoEnabled()) {
                logger.info("Detected server " + sd.serverId);
            }
            isInstanceInitialized = true;
        }

        return sd.serverId;
    }

    public static boolean isGeronimo() {
        ServerDetector sd = instance;

        if (sd.geronimo == null) {
            sd.geronimo = _detect(GERONIMO_CLASS);
        }

        return sd.geronimo.booleanValue();
    }

    public static boolean isGlassfish() {
        ServerDetector sd = instance;

        if (sd.glassfish == null) {
            String value = System.getProperty(GLASSFISH_SYSTEM_PROPERTY);

            if (value != null) {
                sd.glassfish = Boolean.TRUE;
            } else {
                sd.glassfish = Boolean.FALSE;
            }
        }

        return sd.glassfish.booleanValue();
    }

    public static boolean isJBoss() {
        ServerDetector sd = instance;

        if (sd.jBoss == null) {
            sd.jBoss = _detect(JBOSS_CLASS);
        }

        return sd.jBoss.booleanValue();
    }

    public static boolean isJetty() {
        ServerDetector sd = instance;

        if (sd.jetty == null) {
            sd.jetty = _detect(JETTY_CLASS);
        }

        return sd.jetty.booleanValue();
    }

    public static boolean isJOnAS() {
        ServerDetector sd = instance;

        if (sd.jonas == null) {
            sd.jonas = _detect(JONAS_CLASS);
        }

        return sd.jonas.booleanValue();
    }

    public static boolean isOC4J() {
        ServerDetector sd = instance;

        if (sd.oc4j == null) {
            sd.oc4j = _detect(OC4J_CLASS);
        }

        return sd.oc4j.booleanValue();
    }

    public static boolean isOrion() {
        ServerDetector sd = instance;

        if (sd.orion == null) {
            sd.orion = _detect(ORION_CLASS);
        }

        return sd.orion.booleanValue();
    }

    public static boolean isPramati() {
        ServerDetector sd = instance;

        if (sd.pramati == null) {
            sd.pramati = _detect(PRAMATI_CLASS);
        }

        return sd.pramati.booleanValue();
    }

    public static boolean isResin() {
        ServerDetector sd = instance;

        if (sd.resin == null) {
            sd.resin = _detect(RESIN_CLASS);
        }

        return sd.resin.booleanValue();
    }

    public static boolean isRexIP() {
        ServerDetector sd = instance;

        if (sd.rexIP == null) {
            sd.rexIP = _detect(REXIP_CLASS);
        }

        return sd.rexIP.booleanValue();
    }

    public static boolean isTomcat() {
        ServerDetector sd = instance;

        if (sd.tomcat == null) {
            sd.tomcat = _detect(TOMCAT_BOOTSTRAP_CLASS);
        }

        if (sd.tomcat == null) {
            sd.tomcat = _detect(TOMCAT_EMBEDDED_CLASS);
        }

        return sd.tomcat.booleanValue();
    }

    public static boolean isWebLogic() {
        ServerDetector sd = instance;

        if (sd.webLogic == null) {
            sd.webLogic = _detect(WEBLOGIC_CLASS);
        }

        return sd.webLogic.booleanValue();
    }

    public static boolean isWebSphere() {
        ServerDetector sd = instance;

        if (sd.webSphere == null) {
            sd.webSphere = _detect(WEBSPHERE_CLASS);
        }

        return sd.webSphere.booleanValue();
    }

    private static Boolean _detect(String className) {
        try {
            ClassLoader.getSystemClassLoader().loadClass(className);

            return Boolean.TRUE;
        } catch (ClassNotFoundException cnfe) {
            ServerDetector sd = instance;

            Class<?> c = sd.getClass();

            if (c.getResource(className) != null) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        }
    }

    private ServerDetector() {}
}
