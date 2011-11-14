package org.mskcc.cgds.test.servlet;

import java.io.StringWriter;

/**
 * A JUnit-related class required for NullHttpServletResponse.
 */
public class MyPrintWriter extends java.io.PrintWriter {
   public MyPrintWriter(StringWriter myStringWriter) {
      super(myStringWriter);
   }

}
