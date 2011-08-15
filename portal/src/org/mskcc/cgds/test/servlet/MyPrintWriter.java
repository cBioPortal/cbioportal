package org.mskcc.cgds.test.servlet;

import java.io.StringWriter;

public class MyPrintWriter extends java.io.PrintWriter
{

   public MyPrintWriter(StringWriter myStringWriter) {
      super(myStringWriter);
   }

}
