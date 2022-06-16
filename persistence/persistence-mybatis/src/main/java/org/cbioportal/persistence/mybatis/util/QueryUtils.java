/*
 * Copyright (C) 2017-2021 Dremio Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.cbioportal.persistence.mybatis.util;

import org.apache.arrow.vector.VectorSchemaRoot;

/**
 * Utilitary class for helping queries out with cross-cutting concerns,
 * such as printing and saving resources to a file.
 */
public final class QueryUtils {

  private QueryUtils() {
    // Prevent instantiation.
  }

  /**
   * Prints a message out that the client has authenticated with the server.
   *
   * @param host the server that the client connected to.
   * @param port the port on the server that the client connected to.
   */
  public static void printAuthenticated(String host, int port) {
    print(Prefixes.INFORMATION,
        "Authenticated with Dremio server at " + host + ":" + port + " sucessfully");
  }

  /**
   * Prints out the header.
   */
  public static void printPreamble() {
    print(Prefixes.INFORMATION, "Printing query results from Dremio.");
  }

  /**
   * Prints out the query that was run.
   *
   * @param query the query that was run
   */
  public static void printRunningQuery(String query) {
    print(Prefixes.INFORMATION, "Running query.");
  }

  /**
   * Prints the query results to the console.
   *
   * @param vectorSchemaRoot the vector schema root from which to get
   *                         the results.
   */
  public static void printResults(VectorSchemaRoot vectorSchemaRoot) {

    print(Fillers.HEADER, "Query results");

    /*
     * This prints out the results, each value in its corresponding
     * row and column, with different columns separated by tabs and
     * properly aligned with their field names.
     *
     * If you intend to read from a binary file containing the data of
     * the VectorSchemaRoot with the query results, the following code
     * snippet should help you out:
     *
     * ============================ CODE =============================
     *
     * public static void printFromBinaryFile(File file) {
     *
     *     try (ArrowStreamReader reader = new ArrowStreamReader(
     *          new FileOutputStream(file), new RootAllocator(Long.MAX_VALUE))) {
     *
     *         // Instantiating a new VectorSchemaRoot based on the binary info.
     *         VectorSchemaRoot batchRead = reader.getVectorSchemaRoot;
     *
     *         // Updating the data inside the VectorSchemaRoot.
     *         reader.loadNextBatch();
     *
     *         // Printing out the results.
     *         System.out.println(batchRead.contentToTSVString());
     *     }
     * }
     *
     * ============================ INFO =============================
     *
     * For more information on this, please refer to the documentation:
     *     <https://arrow.apache.org/docs/java/ipc.html>
     */
    System.out.println(vectorSchemaRoot.contentToTSVString());

    print(Fillers.FOOTER, "Number of records retrieved: " + vectorSchemaRoot.getRowCount());
  }

  /**
   * Prints the provided exception to the console.
   *
   * @param e the exception to be printed out.
   */
  public static void printExceptionOnClosed(Exception e) {
    print(Prefixes.ERROR, e.getMessage());
    e.printStackTrace();
  }

  private static void print(Prefixes prefixes, String message) {
    System.out.println(prefixes.toFormattedString() + " " + message);
  }

  private static void print(Fillers fillers, String message) {
    String filler = fillers.toFormattedString();
    System.out.println(filler + " " + message + " " + filler);
  }
}
