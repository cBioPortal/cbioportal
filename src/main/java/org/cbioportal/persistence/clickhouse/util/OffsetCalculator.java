package org.cbioportal.persistence.clickhouse.util;

public class OffsetCalculator {

	public static Integer calculate(Integer pageSize, Integer pageNumber) {
		return pageSize == null || pageNumber == null ? null : pageSize * pageNumber;

	}
}
