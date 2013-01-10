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

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;

import java.io.PrintWriter;
import java.io.OutputStream;

/**
 * This class recreates the functionality found in 
 * Perl Data-CTable module. Essentially it provides
 * read, write, and manipulation of tabular data.
 * The given matrix must be square.
 */
public class DataMatrix {

	// our logger
	private static Log LOG = LogFactory.getLog(DataMatrix.class);

	// inner class which encapsulates a column header w/its column data
	private class ColumnHeader {
		public String label;
		public LinkedList<String> columnData;
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
	 * @param rowData List<LinkedList<String>>
	 * @param columnNames List<String>
	 */
	public DataMatrix(List<LinkedList<String>> rowData, List<String> columnNames) {

		// set numberOfRows
		numberOfRows = rowData.size();

		// some collections
		rowsToIgnore = new HashSet<Integer>();
		caseIDs = new HashSet<String>();

		// geneIDColumnHeading
		geneIDColumnHeading = Converter.GENE_ID_COLUMN_HEADER_NAME;

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
			columnHeader.columnData = new LinkedList<String>();
			// interate over all rows and grab the data at column 'index'
			++columnIndex;
			for (List<String> row : rowData) {
				// we may have a situation where there are more columns than data in a row (empty cells)
				if (columnIndex < row.size()) {
					columnHeader.columnData.add(row.get(columnIndex));
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
	 * @param columnsToIgnore List<String>
	 */
	public void convertCaseIDs(List<String> columnsToIgnore) {

		// reset our caseIDs list
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
	 * Converts full TCGA bar code to abbreviated version for use in portal.
	 * This routine is used when the case IDs exist not in the header row, but in a column.
	 * This is true for MAF files.
	 *
	 * @param caseIDColumn String
	 */
	public void convertCaseIDs(String caseIDColumn) {

		// reset our caseIDs list
		caseIDs.clear();

		List<String> caseIDColumnData = getColumnData(caseIDColumn).get(0);
		for (int lc = 0; lc < caseIDColumnData.size(); lc++) {
			String caseID = caseIDColumnData.get(lc);
			if (caseIDsFilter.isTumorCaseID(caseID)) {
				caseIDColumnData.set(lc, caseIDsFilter.convertCaseID(caseID));
				caseIDs.add(caseID);
			}
		}
	}

	/**
	 * Set column order.  Any columns in the data matrix
	 * that are not in the given column order will be dropped.
	 *
	 * @param sortedColumnNames List<String>
	 * @throws Exception
	 */
	public void setColumnOrder(List<String> newColumnOrder) throws Exception {

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
				throw new IllegalArgumentException("column not found in list: " +  column);
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
	public void addColumn(String newColumnName, List<String> columnData) {

		if (columnData.size() < numberOfRows) {
			int columnDataSize = columnData.size();
			for (int lc = 0; lc < numberOfRows-columnDataSize; lc++) {
				columnData.add(new String());
			}
		}
		else if (columnData.size() > numberOfRows) {
			throw new IllegalArgumentException("columnData size > matrix size, aborting.");
		}

		// create new columnHeader object
		ColumnHeader columnHeader = new ColumnHeader();
		columnHeader.label = newColumnName;
		columnHeader.columnData = new LinkedList(columnData);
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
	public void ignoreColumn(String columnName, boolean ignoreColumn) {

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
	public void ignoreColumn(int columnIndex, boolean ignoreColumn) {
		columnHeaders.get(columnIndex).ignoreColumn = ignoreColumn;
	}

	/**
	 * Rename a column
	 *
	 * @param columnName String
	 * @param newColumnName String
	 * @throws Exception
	 */
	public void renameColumn(String columnName, String newColumnName) throws Exception {
		
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
	 * @return List<String>
	 */
	public List<String> getColumnHeaders() {

		LinkedList<String> toReturn = new LinkedList<String>();
		for (ColumnHeader columnHeader : columnHeaders) {
			toReturn.add(columnHeader.label);
		}

		// outta here
		return toReturn;
	}

	/**
	 * Gets the data for a given column name.  Returns
	 * the data stored in the internal data structure,
	 * so changes in the returned List will be reflected
	 * in subsequent calls into the class.
	 *
	 * @param columnName String
	 * @return List<LinkedList<String>>
	 */
	public List<LinkedList<String>> getColumnData(String columnName) {

		LinkedList<LinkedList<String>> toReturn = new LinkedList<LinkedList<String>>();

		for (ColumnHeader columnHeader : columnHeaders) {
			if (columnHeader.label.equals(columnName)) {
				toReturn.add(columnHeader.columnData);
			}
		}

		// outta here
		return toReturn;
	}

	/**
	 * Gets the data for the given column index.  This
	 * method is motivated by the fact that certain files
	 * may have the same column header - in particular,
	 * *_genes.conf_99.txt may have multiple cytoband columns.
	 *
	 * @param columnIndex int
	 * @return List<String>
	 */
	public List<String> getColumnData(int columnIndex) {

		return columnHeaders.get(columnIndex).columnData;
	}

	/**
	 * Returns the list of case id's within this matrix.
	 * Note: filterAndConvertCaseIDs should be called before
	 * this collection is returned or it will just return an
	 * empty collection.
	 *
	 * @return Set<String>
	 */
	public Set<String> getCaseIDs() {
		return caseIDs;
	}

	/**
	 * Used to set geneIDColumnHeading.  See
	 * getGeneIDs().
	 *
	 * @param geneIDColumnHeading String
	 */
	public void setGeneIDColumnHeading(String geneIDColumnHeading) {
		this.geneIDColumnHeading = geneIDColumnHeading;
	}

	/**
	 * Returns the collection of Gene id's within this matrix.
	 * setGeneIDColumnHeading() must be called prior to calling getGeneIDs().
	 *
	 * @return List<String>
	 */
	public Set<String> getGeneIDs() {

		// collection we will return
		HashSet<String> toReturn = new HashSet<String>();

		List<String> geneColumnData = getColumnData(geneIDColumnHeading).get(0);
		for (String geneID : geneColumnData) {
			toReturn.add(geneID);
		}

		// outta here
		return toReturn;
	}

	/**
	 * Inserts row data to beginning of matrix.
	 * 
	 * @param List<String> rowData
	 */
	public void insertRow(List<String> rowData) {
		addRow(rowData, 0);
	}

	/**
	 * Appends a row to the end of the matrix.
	 * 
	 * @param List<String> rowData
	 */
	public void appendRow(List<String> rowData) {
		addRow(rowData, -1);
	}

	/**
	 * Adds the given row number into our rowsToIgnore set.
	 * Note row indices start an 0.
	 *
	 * @param rowNumber int
	 */
	public void ignoreRow(int rowNumber, boolean ignoreColumn) {
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
	public void write(OutputStream out) throws Exception {

		PrintWriter writer = new PrintWriter(out);

		// print the column header
		for (ColumnHeader columnHeader : columnHeaders) {
			if (columnHeader.ignoreColumn) continue;
			writer.print(columnHeader.label);
			if (columnHeader != columnHeaders.getLast()) {
				writer.print(Converter.VALUE_DELIMITER);
			}
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
					writer.print(Converter.VALUE_DELIMITER);
				}
			}
			writer.println();
		}

		// clean up
		writer.flush();
	}

	/**
	 * Private helper function to insert row at a given given index.
	 *
	 * @param List<String> rowData
	 * @param rowIndex long
	 */
	private void addRow(List<String> rowData, int rowIndex) {

		// sanity checks
		if (rowData.size() < columnHeaders.size()) {
			throw new IllegalArgumentException("rowData size < number in matrix, aborting.");
		}

		// iterate across all column headers
		for (int lc = 0; lc < columnHeaders.size(); lc++) {
			ColumnHeader columnHeader = columnHeaders.get(lc);
			// for each columnHeader->columnData, insert row data at rowIndex
			if (rowIndex == 0) {
				columnHeader.columnData.addFirst(rowData.get(lc));
			}
			else {
				columnHeader.columnData.addLast(rowData.get(lc));
			}
		}

		// adjust ignoreRow set
		// we just inserted at beginning, increment all values in existing set by 1.
		if (rowIndex == 0) {
			HashSet<Integer> rowsToIgnoreCopy = (HashSet<Integer>)rowsToIgnore.clone();
			rowsToIgnore.clear();
			for (Integer integer : rowsToIgnoreCopy) {
				rowsToIgnore.add(++integer);
			}
		}

		// inc number of rows property
		++numberOfRows;
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

		LinkedList columnNames = new LinkedList(columnHeaders);
		LinkedList<LinkedList<String>> rowData = new LinkedList<LinkedList<String>>();
		rowData.add(new LinkedList<String>(rowOne));
		rowData.add(new LinkedList<String>(rowTwo));
		rowData.add(new LinkedList<String>(rowThree));

		// create matrix and dump
		DataMatrix dataMatrix = new DataMatrix(rowData, columnNames);
		dataMatrix.write(System.out);
		System.out.println();
		System.out.println();

		// insert a row
		List<String> newRowToInsert = java.util.Arrays.asList("-2", "-1", "0");
		dataMatrix.insertRow(newRowToInsert);
		dataMatrix.write(System.out);
		System.out.println();
		System.out.println();

		// append a row
		List<String> newRowToAppend = java.util.Arrays.asList("d", "e", "f");
		dataMatrix.appendRow(newRowToAppend);
		dataMatrix.write(System.out);
		System.out.println();
		System.out.println();

		// add a column and dump
		dataMatrix.addColumn("H4", new LinkedList<String>());
		dataMatrix.write(System.out);
		System.out.println();
		System.out.println();

		// remove a column and dump
		dataMatrix.ignoreColumn("H1", true);
		dataMatrix.write(System.out);
		System.out.println();
		System.out.println();

		// reorder the columns and dump
		java.util.List newColumnOrder = java.util.Arrays.asList("H3", "H2", "H4");
		dataMatrix.setColumnOrder(new LinkedList<String>(newColumnOrder));
		dataMatrix.write(System.out);
		System.out.println();
		System.out.println();

		// reorder again and dump
		java.util.List anotherNewColumnOrder = java.util.Arrays.asList("H4", "H3", "H2");
		dataMatrix.setColumnOrder(new LinkedList<String>(anotherNewColumnOrder));
		dataMatrix.write(System.out);
		System.out.println();
		System.out.println();

		// remove a column and dump
		dataMatrix.ignoreColumn("H4", true);
		dataMatrix.write(System.out);
		System.out.println();
		System.out.println();

		// reorder a last time
		java.util.List lastNewColumnOrder = java.util.Arrays.asList("H2", "H3");
		dataMatrix.setColumnOrder(new LinkedList<String>(lastNewColumnOrder));
		dataMatrix.write(System.out);
		System.out.println();
		System.out.println();

		// change some values in a column
		List<String> columnValues = dataMatrix.getColumnData("H2").get(0);
		for (int lc = 0; lc < columnValues.size(); lc++) {
			if (columnValues.get(lc).equals("2")) {
				columnValues.set(lc, "2.7");
			}
		}

		dataMatrix.write(System.out);
		System.out.println();
		System.out.println();

		// ignore a few rows
		dataMatrix.ignoreRow(0, true);
		dataMatrix.ignoreRow(2, true);
		dataMatrix.write(System.out);
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

		columnNames = new LinkedList(columnHeaders);
		rowData = new LinkedList<LinkedList<String>>();
		rowData.add(new LinkedList<String>(rowOne));
		rowData.add(new LinkedList<String>(rowTwo));
		rowData.add(new LinkedList<String>(rowThree));

		// create matrix and dump
		dataMatrix = new DataMatrix(rowData, columnNames);
		dataMatrix.write(System.out);
		System.out.println();
		System.out.println();

		// filter and convert, then dump
		String[] columnsToIgnore = { "Gene Symbol", "Locus ID" };
		dataMatrix.convertCaseIDs(java.util.Arrays.asList(columnsToIgnore));
		dataMatrix.write(System.out);
		System.out.println();
		System.out.println();
	}
}
