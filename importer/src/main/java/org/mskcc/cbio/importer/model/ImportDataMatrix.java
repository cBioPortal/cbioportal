/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

// package
package org.mskcc.cbio.importer.model;

// imports
import org.mskcc.cbio.importer.Admin;
import org.mskcc.cbio.importer.CaseIDs;
import org.mskcc.cbio.importer.Converter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Vector;
import java.util.HashSet;
import java.util.Collection;
import java.util.LinkedList;

import java.io.PrintWriter;
import java.io.OutputStream;

/**
 * This class recreates the functionality found in 
 * Perl Data-CTable module. Essentially it provides
 * read, write, and manipulation of tabular data.
 * The given matrix must be square.
 */
public final class ImportDataMatrix {

	// our logger
	private static final Log LOG = LogFactory.getLog(ImportDataMatrix.class);

	// inner class which encapsulates a column header w/its column data
	private class ColumnHeader {
		public String label;
		public Vector<String> columnData;
		public boolean ignoreColumn;
	}

	// keeps track of number of rows of tabular data
	private int numberOfRows;

	// this is a list of rows to ignore when dumping the matrix
	private HashSet<Integer> rowsToIgnore;

	// a list of "column" objects - 
	// each element has a column heading and a vector of column data
	private LinkedList<ColumnHeader> columnHeaders;

	// ref to caseids
	private CaseIDs caseIDsFilter;

	// our collection of case ids
	private HashSet<String> caseIDs;

	// gene id column heading - may be null
	private String geneIDColumnHeading;

	/**
	 * Constructor.
	 *
	 * @param rowData Vector
	 * @param columnNames Vector
	 */
	public ImportDataMatrix(final Vector<Vector<String>> rowData, final Vector<String> columnNames) {

		// set numberOfRows
		numberOfRows = rowData.size();

		// some collections
		rowsToIgnore = new HashSet<Integer>();
		caseIDs = new HashSet<String>();

		// geneIDColumnHeading
		geneIDColumnHeading = "";

		// create our linked list of column header objects
		columnHeaders = new LinkedList<ColumnHeader>();

		// populate our column headers list
		int columnIndex = -1;
		for (String columnName : columnNames) {
			// drop column if its missing label
			if (columnName.length() == 0) { 
				if (LOG.isInfoEnabled()) {
					LOG.info("columnName is empty, skipping...");
				}
				continue;
			}
			// create a new ColumnHeader object
			ColumnHeader columnHeader = new ColumnHeader();
			columnHeader.label = columnName;
			columnHeader.columnData = new Vector<String>();
			// interate over all rows and grab the data at column 'index'
			++columnIndex;
			for (Vector<String> row : rowData) {
				// we may have a situation where there are more columns than data in a row (empty cells)
				if (columnIndex < row.size()) {
					columnHeader.columnData.add(row.elementAt(columnIndex));
				}
				else {
					columnHeader.columnData.add("");
				}
			}
			columnHeader.ignoreColumn = false;
			// add this ColumnHeader object to our linked list
			columnHeaders.add(columnHeader);
		}
		
		// init our case id's object
		initCaseIDs();
	}

	/**
	 * Converts full TCGA bar code to abbreviated version for use in portal.
	 * Ignores any column in which the case ID is not a tumor.
	 * Columns to ignore is used to specify columns not to process (like Gene Symbol).
	 * which may be null.
	 *
	 * @param columnsToIgnore Collection<String>
	 */
	public void convertCaseIDs(final Collection<String> columnsToIgnore) {

		// reset our caseIDs vector
		caseIDs.clear();

		// iterate over columns 
		for (ColumnHeader columnHeader : columnHeaders) {
			// are we already ignoring column?
			if (columnHeader.ignoreColumn) {
				continue;
			}
			// ignore column in during filtering if desired
			else if (columnsToIgnore != null && columnsToIgnore.contains(columnHeader.label)) {
				continue;
			}
			// ignore column (case) if its not a tumor id
			if (!caseIDsFilter.isTumorCaseID(columnHeader.label)) {
				columnHeader.ignoreColumn = true;
				continue;
			}
			// made it here, convert the id
			columnHeader.label = caseIDsFilter.convertCaseID(columnHeader.label);
			caseIDs.add(columnHeader.label);
		}
	}

	/**
	 * Set column order.  Any columns in the data matrix
	 * that are not in the given column order will be dropped.
	 *
	 * @param sortedColumnNames Collection<String>
	 * @throws Exception
	 */
	public void setColumnOrder(final Collection<String> newColumnOrder) throws Exception {

		LinkedList<ColumnHeader> newColumnHeaderList = new LinkedList<ColumnHeader>();

		for (String column : newColumnOrder) {
			// find column in columnHeaders
			boolean foundColumnHeader = false;
			for (ColumnHeader columnHeader : columnHeaders) {
				if (columnHeader.label.equals(column)) {
					newColumnHeaderList.add(columnHeader);
					foundColumnHeader = true;
					break;
				}
			}
			if (!foundColumnHeader) {
				throw new IllegalArgumentException("column not found in vector: " +  column);
			}
		}

		// set our ref to the new column header list
		columnHeaders = newColumnHeaderList;
	}

	/**
	 * Adds a column to the end of the table.
	 *
	 * @param newColumnName String
	 * @param columnData Vector<String>
	 */
	public void addColumn(final String newColumnName, final Vector<String> columnData) {

		if (columnData.size() < numberOfRows) {
			columnData.setSize(numberOfRows);
			for (int lc = 0; lc < numberOfRows; lc++) {
				if (columnData.get(lc) == null) {
					columnData.add(lc, new String());
				}
			}
		}
		else if (columnData.size() > numberOfRows) {
			throw new IllegalArgumentException("columnData size > matrix size, aborting.");
		}

		// create new columnHeader object
		ColumnHeader columnHeader = new ColumnHeader();
		columnHeader.label = newColumnName;
		columnHeader.columnData = columnData;
		columnHeader.ignoreColumn = false;

		// add columnHeader object to our list
		columnHeaders.add(columnHeader);
	}

	/**
	 * Sets the ignore boolean on all columns matching this column name.
	 * If the column is ignored, it will not be written.
	 *
	 * @param columnName String
	 */
	public void ignoreColumn(final String columnName, final boolean ignoreColumn) {

		// find column header to remove
		for (ColumnHeader columnHeader : columnHeaders) {
			if (columnHeader.label.equals(columnName)) {
				columnHeader.ignoreColumn = ignoreColumn;
			}
		}
	}

	/**
	 * Sets the ignore boolean on the column indexed by columnIndex.
	 * Removes the given column (by index) in the table.
	 *
	 * @param columnName String
	 */
	public void ignoreColumn(final int columnIndex, final boolean ignoreColumn) {
		columnHeaders.get(columnIndex).ignoreColumn = ignoreColumn;
	}

	/**
	 * Rename a column
	 *
	 * @param columnName String
	 * @param newColumnName String
	 * @throws Exception
	 */
	public void renameColumn(final String columnName, final String newColumnName) throws Exception {
		
		boolean foundColumnHeader = false;
		for (ColumnHeader columnHeader : columnHeaders) {
			if (columnHeader.label.equals(columnName)) {
				columnHeader.label = newColumnName;
				foundColumnHeader = true;
			}
		}
		if (!foundColumnHeader) {
			throw new IllegalArgumentException("column name not found: " + columnName);
		}
	}

	/**
	 * Gets the column headers.
	 * Returns a new copy.
	 *
	 * @return Vector<String>
	 */
	public Vector<String> getColumnHeaders() {

		Vector<String> toReturn = new Vector<String>();
		for (ColumnHeader columnHeader : columnHeaders) {
			toReturn.add(columnHeader.label);
		}

		// outta here
		return toReturn;
	}

	/**
	 * Gets the data for a given column name.  Returns
	 * the data stored in the internal data structure,
	 * so changes in the returned vector will be reflected
	 * in subsequent calls into the class.
	 *
	 * @param columnName String
	 * @return Vector<String>
	 */
	public Vector<Vector<String>> getColumnData(final String columnName) {

		Vector<Vector<String>> toReturn = new Vector<Vector<String>>();

		for (ColumnHeader columnHeader : columnHeaders) {
			if (columnHeader.label.equals(columnName)) {
				toReturn.add(columnHeader.columnData);
			}
		}

		// should not make it here
		return toReturn;
	}

	/**
	 * Gets the data for the given column index.  This
	 * method is motivated by the fact that certain files
	 * may have the same column header - in particular,
	 * *_genes.conf_99.txt may have multiple cytoband columns.
	 *
	 * @param columnIndex int
	 * @return Vector<String>
	 */
	public Vector<String> getColumnData(final int columnIndex) {

		return columnHeaders.get(columnIndex).columnData;
	}

	/**
	 * Returns the collection of case id's within this matrix.
	 * Note: filterAndConvertCaseIDs should be called before
	 * this collection is returned or it will just return an
	 * empty collection.
	 *
	 * @return Collection<String>
	 */
	public Collection<String> getCaseIDs() {
		return caseIDs;
	}

	/**
	 * Used to set geneIDColumnHeading.  See
	 * getGeneIDs().
	 *
	 * @param geneIDColumnHeading String
	 */
	public void setGeneIDColumnHeading(final String geneIDColumnHeading) {
		this.geneIDColumnHeading = geneIDColumnHeading;
	}

	/**
	 * Returns the collection of Gene id's within this matrix.
	 * setGeneIDColumnHeading() must be called prior to calling getGeneIDs().
	 *
	 * @return Collection<String>
	 */
	public Collection<String> getGeneIDs() {

		// collection we will return
		HashSet<String> toReturn = new HashSet<String>();

		Vector<String> geneColumnData = getColumnData(geneIDColumnHeading).get(0);
		for (String geneID : geneColumnData) {
			toReturn.add(geneID);
		}

		// outta here
		return toReturn;
	}

	/**
	 * Adds the given row number into our rowsToIgnore set.
	 * Note row indices start an 0.
	 *
	 * @param rowNumber int
	 */
	public void ignoreRow(final int rowNumber, final boolean ignoreColumn) {
		if (ignoreColumn) {
			rowsToIgnore.add(rowNumber);
		}
		else if (rowsToIgnore.contains(rowNumber)) {
			rowsToIgnore.remove(rowNumber);
		}
	}

	/**
	 * Writes the tabular data to the given OutputStream
	 * in a TSV format.
	 *
	 * @param out OutputStream
	 * @throws Exception
	 */
	public void write(final OutputStream out) throws Exception {

		PrintWriter writer = new PrintWriter(out);

		// print the column header
		for (ColumnHeader columnHeader : columnHeaders) {
			if (columnHeader.ignoreColumn) continue;
			writer.print(columnHeader.label);
			writer.print(Converter.CASE_DELIMITER);
		}
		writer.println();

		for (int rowIndex = 0; rowIndex < numberOfRows; rowIndex++) {
			// skip row if its contained in our rowsToIgnore set.
			if (rowsToIgnore.contains(rowIndex)) {
				continue;
			}
			for (ColumnHeader columnHeader : columnHeaders) {
				if (columnHeader.ignoreColumn) continue;
				writer.print(columnHeader.columnData.get(rowIndex));
				if (columnHeader != columnHeaders.getLast()) {
					writer.print(Converter.CASE_DELIMITER);
				}
			}
			writer.println();
		}

		// clean up
		writer.flush();
	}

	/**
	 * Private function to init ref to CaseId.
	 */
	private void initCaseIDs() {

		ApplicationContext context = new ClassPathXmlApplicationContext(Admin.contextFile);
		caseIDsFilter = (CaseIDs)context.getBean("caseIDs");
	}

	public static void main(String[] args) throws Exception {

		java.util.List columnHeaders = java.util.Arrays.asList("H1", "H2", "H3");
		java.util.List rowOne = java.util.Arrays.asList("A", "B", "C");
		java.util.List rowTwo = java.util.Arrays.asList("1", "2", "3");
		java.util.List rowThree = java.util.Arrays.asList("X", "Y", "Z");

		Vector columnNames = new Vector(columnHeaders);
		Vector<Vector<String>> rowData = new Vector<Vector<String>>();
		rowData.add(new Vector<String>(rowOne));
		rowData.add(new Vector<String>(rowTwo));
		rowData.add(new Vector<String>(rowThree));

		// create matrix and dump
		ImportDataMatrix importDataMatrix = new ImportDataMatrix(rowData, columnNames);
		importDataMatrix.write(System.out);
		System.out.println();
		System.out.println();

		// add a column and dump
		importDataMatrix.addColumn("H4", new Vector<String>());
		importDataMatrix.write(System.out);
		System.out.println();
		System.out.println();

		// remove a column and dump
		importDataMatrix.ignoreColumn("H1", true);
		importDataMatrix.write(System.out);
		System.out.println();
		System.out.println();

		// reorder the columns and dump
		java.util.List newColumnOrder = java.util.Arrays.asList("H3", "H2", "H4");
		importDataMatrix.setColumnOrder(new Vector<String>(newColumnOrder));
		importDataMatrix.write(System.out);
		System.out.println();
		System.out.println();

		// reorder again and dump
		java.util.List anotherNewColumnOrder = java.util.Arrays.asList("H4", "H3", "H2");
		importDataMatrix.setColumnOrder(new Vector<String>(anotherNewColumnOrder));
		importDataMatrix.write(System.out);
		System.out.println();
		System.out.println();

		// remove a column and dump
		importDataMatrix.ignoreColumn("H4", true);
		importDataMatrix.write(System.out);
		System.out.println();
		System.out.println();

		// reorder a last time
		java.util.List lastNewColumnOrder = java.util.Arrays.asList("H2", "H3");
		importDataMatrix.setColumnOrder(new Vector<String>(lastNewColumnOrder));
		importDataMatrix.write(System.out);
		System.out.println();
		System.out.println();

		// change some values in a column
		Vector<String> columnValues = importDataMatrix.getColumnData("H2").get(0);
		for (int lc = 0; lc < columnValues.size(); lc++) {
			if (columnValues.elementAt(lc).equals("2")) {
				columnValues.setElementAt("2.7", lc);
			}
		}

		importDataMatrix.write(System.out);
		System.out.println();
		System.out.println();

		// ignore a few rows
		importDataMatrix.ignoreRow(0, true);
		importDataMatrix.ignoreRow(2, true);
		importDataMatrix.write(System.out);
		System.out.println();
		System.out.println();

		// test case id filtering & conversion

		columnHeaders = java.util.Arrays.asList("Gene Symbol", "Locus ID", "Cytoband",
												"TCGA-A1-A0SB-01A-11D-A141-01", "TCGA-A1-A0SD-Tumor",
												"TCGA-A1-A0SE-01A-11D-A087-01", "TCGA-A1-A0SF-Normal",
												"TCGA-A1-A0SG-01A-11D-A141-01");
		rowOne = java.util.Arrays.asList("ACAP3", "116983", "1p36.33", "1", "2", "3", "4", "5");
		rowTwo = java.util.Arrays.asList("ACTRT2", "140625", "1p36.32", "1", "2", "3", "4",  "5");
		rowThree = java.util.Arrays.asList("AGRN", "375790", "1p36.33", "1", "2", "3", "4", "5");

		columnNames = new Vector(columnHeaders);
		rowData = new Vector<Vector<String>>();
		rowData.add(new Vector<String>(rowOne));
		rowData.add(new Vector<String>(rowTwo));
		rowData.add(new Vector<String>(rowThree));

		// create matrix and dump
		importDataMatrix = new ImportDataMatrix(rowData, columnNames);
		importDataMatrix.write(System.out);
		System.out.println();
		System.out.println();

		// filter and convert, then dump
		String[] columnsToIgnore = { "Gene Symbol", "Locus ID" };
		importDataMatrix.convertCaseIDs(java.util.Arrays.asList(columnsToIgnore));
		importDataMatrix.write(System.out);
		System.out.println();
		System.out.println();
	}
}
