package org.mskcc.cbio.portal.util;

/**
 * Encapsulates a single XDebug log message.
 * An XDebug log message consists of three parts:
 * <UL>
 * <LI>name of the class that invoked the logger
 * <LI>the log message
 * <LI>color of the log message (defaults to black)
 * </UL>
 */
public class XDebugMessage {
    private String message;
    private String className;
    private String color;
    private static final String DEFAULT_COLOR = "black";

    /**
     * Constructor.
     *
     * @param className name of the class that invoke the logger
     * @param message   the log message
     */
    public XDebugMessage(String className, String message) {
        this.className = className;
        this.message = message;
        this.color = DEFAULT_COLOR;
    }

    /**
     * Constructor with Color Parameter.
     *
     * @param className name of the class that invoke the logger
     * @param message   the log message
     * @param color     the log message color
     */
    public XDebugMessage(String className, String message, String color) {
        this.className = className;
        this.message = message;
        this.color = color;
    }

    /**
     * Get Debug Message.
     *
     * @return the log message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get Class Name.
     *
     * @return the class name of the object that invoked the logger
     */
    public String getClassName() {
        return className;
    }

    /**
     * Get Color.
     *
     * @return color of the log message
     */
    public String getColor() {
        return color;
    }
}
