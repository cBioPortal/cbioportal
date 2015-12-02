/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.scripts;

import java.io.*;
import java.util.*;
import joptsimple.*;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.SpringUtil;

/**
 *
 * @author jgao, inodb
 */
public class ImportTimelineData {

	private static void importData(String dataFile, int cancerStudyId) throws IOException, DaoException {
		MySQLbulkLoader.bulkLoadOn();
		SpringUtil.initDataSource();

		System.out.print("Reading file " + dataFile + "\n");
		FileReader reader = new FileReader(dataFile);
		BufferedReader buff = new BufferedReader(reader);

		String line = buff.readLine();

		// Check event category agnostic headers
		String[] headers = line.split("\t");
		int indexCategorySpecificField = -1;
		if (headers[0].equals("PATIENT_ID") && headers[1].equals("START_DATE")) {
			if ("STOP_DATE".equals(headers[2]) && "EVENT_TYPE".equals(headers[3])) {
				indexCategorySpecificField = 4;
			} else if (headers[2].equals("EVENT_TYPE")) {
				indexCategorySpecificField = 3;
			}
		}
		if (indexCategorySpecificField == -1) {
			throw new RuntimeException("The first line must start with\n'PATIENT_ID\tSTART_DATE\tEVENT_TYPE'\nor\n"
				+ "PATIENT_ID\tSTART_DATE\tSTOP_DATE\tEVENT_TYPE");
		}

		long clinicalEventId = DaoClinicalEvent.getLargestClinicalEventId();

		while ((line = buff.readLine()) != null) {
			line = line.trim();

			String[] fields = line.split("\t");
			if (fields.length > headers.length) {
				System.err.println("more attributes than header: " + line);
				continue;
			}

			Patient patient = DaoPatient.getPatientByCancerStudyAndPatientId(cancerStudyId, fields[0]);
			if (patient == null) {
				continue;
			}
			ClinicalEvent event = new ClinicalEvent();
			event.setClinicalEventId(++clinicalEventId);
			event.setPatientId(patient.getInternalId());
			event.setStartDate(Long.valueOf(fields[1]));
			if (indexCategorySpecificField != 3 && !fields[2].isEmpty()) {
				event.setStopDate(Long.valueOf(fields[2]));
			}
			event.setEventType(fields[indexCategorySpecificField - 1]);
			Map<String, String> eventData = new HashMap<String, String>();
			for (int i = indexCategorySpecificField; i < fields.length; i++) {
				if (!fields[i].isEmpty()) {
					eventData.put(headers[i], fields[i]);
				}
			}
			event.setEventData(eventData);

			DaoClinicalEvent.addClinicalEvent(event);
		}

		MySQLbulkLoader.flushAll();
	}

	public static void main(String[] args) throws Exception {
		//args = new String[] {"--data","/Users/debruiji/hg/cbio-portal-data/msk-impact/caisis/data_timeline_status_caisis_gbm.txt",
		//   "--meta","/Users/debruiji/hg/cbio-portal-data/msk-impact/caisis/meta_timeline_status_caisis_gbm_test.txt",
		//    "--loadMode", "bulkLoad"};
		if (args.length < 4) {
			System.out.println("command line usage:  importTimelineData --data <data_clinical_events.txt> --meta <meta_clinical_events.txt>");
			return;
		}

		OptionParser parser = new OptionParser();
		OptionSpec<String> data = parser.accepts("data",
			"clinial events data file").withRequiredArg().describedAs("data_clinical_events.txt").ofType(String.class);
		OptionSpec<String> meta = parser.accepts("meta",
			"meta (description) file").withRequiredArg().describedAs("meta_clinical_events.txt").ofType(String.class);
		parser.acceptsAll(Arrays.asList("dbmsAction", "loadMode"));
		OptionSet options = null;
		try {
			options = parser.parse(args);
			//exitJVM = !options.has(returnFromMain);
		} catch (OptionException e) {
			e.printStackTrace();
		}

		String dataFile = null;
		if (options.has(data)) {
			dataFile = options.valueOf(data);
		} else {
			throw new Exception("'data' argument required.");
		}

		String descriptorFile = null;
		if (options.has(meta)) {
			descriptorFile = options.valueOf(meta);
		} else {
			throw new Exception("'meta' argument required.");
		}

		Properties properties = new Properties();
		properties.load(new FileInputStream(descriptorFile));

		CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(properties.getProperty("cancer_study_identifier"));
		if (cancerStudy == null) {
			throw new Exception("Unknown cancer study: " + properties.getProperty("cancer_study_identifier"));
		}

		//int cancerStudyId = cancerStudy.getInternalId();
		//DaoClinicalEvent.deleteByCancerStudyId(cancerStudyId);
		importData(dataFile, cancerStudy.getInternalId());

		System.out.println("Done!");
	}
}
