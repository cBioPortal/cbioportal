#! /usr/bin/env python

# ------------------------------------------------------------------------------
# Common components used by various cbioportal scripts.
# ------------------------------------------------------------------------------

import os
import sys
from subprocess import *

# ------------------------------------------------------------------------------
# globals

ERROR_FILE = sys.stderr
OUTPUT_FILE = sys.stdout

IMPORT_STUDY_CLASS = "org.mskcc.cbio.portal.scripts.ImportCancerStudy";
REMOVE_STUDY_CLASS = "org.mskcc.cbio.portal.scripts.RemoveCancerStudy";
IMPORT_CANCER_TYPE_CLASS = "org.mskcc.cbio.portal.scripts.ImportTypesOfCancers"
IMPORT_CASE_LIST_CLASS = "org.mskcc.cbio.portal.scripts.ImportPatientList"

IMPORTER_CLASSNAME_BY_ALTERATION_TYPE = { "CLINICAL" : "org.mskcc.cbio.portal.scripts.ImportClinicalData",
                                            "COPY_NUMBER_ALTERATION" : "org.mskcc.cbio.portal.scripts.ImportProfileData",
                                            "FUSION" : "org.mskcc.cbio.portal.scripts.ImportProfileData",
                                            "GISTIC" : "org.mskcc.cbio.portal.scripts.ImportGisticData",
                                            "METHYLATION" : "org.mskcc.cbio.portal.scripts.ImportProfileData",
                                            "MRNA_EXPRESSION" : "org.mskcc.cbio.portal.scripts.ImportProfileData",
                                            "MUTATION_EXTENDED" : "org.mskcc.cbio.portal.scripts.ImportProfileData",
                                            "MUTATION_SIGNIFICANCE" : "org.mskcc.cbio.portal.scripts.ImportMutSigData",
                                            "RPPA" : "org.mskcc.cbio.portal.scripts.ImportProteinArrayData",
                                            "SEGMENT" : "org.mskcc.cbio.portal.scripts.ImportCopyNumberSegmentData",
                                            "TIMELINE" : "org.mskcc.cbio.portal.scripts.ImportTimelineData" }

IMPORTER_REQUIRES_METADATA = { "org.mskcc.cbio.portal.scripts.ImportClinicalData" : False,
                                "org.mskcc.cbio.portal.scripts.ImportCopyNumberSegmentData" : False,
                                "org.mskcc.cbio.portal.scripts.ImportGisticData" : False,
                                "org.mskcc.cbio.portal.scripts.ImportMutSigData" : False,
                                "org.mskcc.cbio.portal.scripts.ImportProteinArrayData" : False,
								"org.mskcc.cbio.portal.scripts.ImportProfileData" : True,
                                "org.mskcc.cbio.portal.scripts.ImportTimelineData" : True }

# ------------------------------------------------------------------------------
# class definitions

class MetastudyProperties(object):
    def __init__(self,
                 type_of_cancer, cancer_study_identifier,
                 name, description, short_name):
        self.type_of_cancer = type_of_cancer
        self.cancer_study_identifier = cancer_study_identifier
        self.name = name
        self.description = description
        self.short_name = short_name

class MetafileProperties(object):
    def __init__(self,
                 cancer_study_identifier, genetic_alteration_type,
                 datatype, stable_id, show_profile_in_analysis_tab,
                 profile_description, profile_name):
        self.cancer_study_identifier = cancer_study_identifier
        self.genetic_alteration_type = genetic_alteration_type
        self.datatype = datatype
        self.stable_id = stable_id
        self.show_profile_in_analysis_tab = show_profile_in_analysis_tab
        self.profile_description = profile_description
        self.profile_name = profile_name

# ------------------------------------------------------------------------------
# sub-routines

def importer_requires_metadata_file(importer_classname):
	return IMPORTER_REQUIRES_METADATA[importer_classname]

def get_properties(filename):
    properties = {}
    file = open(filename, 'r')
    for line in file:
        line = line.strip()
        # skip line if its blank or a comment
        if len(line) == 0:
            continue
        # store name/value
        property = line.split(': ', 1)
        if (len(property) != 2):
            print >> ERROR_FILE, 'Skipping invalid entry in file: ' + line
            continue
        properties[property[0]] = property[1].strip()
    file.close()
    return properties

def get_metastudy_properties(meta_filename):
    properties = get_properties(meta_filename)

    # ignoring groups, pmid, citation - not needed
    if ("type_of_cancer" not in properties or len(properties["type_of_cancer"]) == 0 or
        "cancer_study_identifier" not in properties or len(properties["cancer_study_identifier"]) == 0 or
        "name" not in properties or len(properties["name"]) == 0 or
        "description" not in properties or len(properties["description"]) == 0 or
        "short_name" not in properties or len(properties["short_name"]) == 0):
        print >> ERROR_FILE, 'Missing one or more required properties, please check metastudy file'
        return None

    # return an instance of PortalProperties
    return MetastudyProperties(properties["type_of_cancer"],
                            properties["cancer_study_identifier"],
                            properties["name"],
                            properties["description"],
                            properties["short_name"])
	
def get_metafile_properties(meta_filename):
    properties = get_properties(meta_filename)
    if ("show_profile_in_analysis_tab not in analysis_tab" not in properties):
        properties['show_profile_in_analysis_tab'] = 'false'

    # error check
    if ("cancer_study_identifier" not in properties or len(properties["cancer_study_identifier"]) == 0 or
    	"genetic_alteration_type" not in properties or len(properties["genetic_alteration_type"]) == 0 or
    	"datatype" not in properties or len(properties["datatype"]) == 0 or
    	"stable_id" not in properties or len(properties["stable_id"]) == 0 or
    	"show_profile_in_analysis_tab" not in properties or len(properties["show_profile_in_analysis_tab"]) == 0 or
    	"profile_name" not in properties or len(properties["profile_name"]) == 0 or
    	"profile_description" not in properties or len(properties["profile_description"]) == 0):
        print >> ERROR_FILE, 'Missing one or more required properties, please check metadata file'
        return None
    
    # return an instance of PortalProperties
    return MetafileProperties(properties["cancer_study_identifier"],
    						properties["genetic_alteration_type"],
                            properties["datatype"],
                            properties["stable_id"],
                            properties["show_profile_in_analysis_tab"],
                            properties["profile_name"],
                            properties["profile_description"])

def run_java(*args):
    java_home = os.environ['JAVA_HOME']
    if len(java_home) == 0:
        print >> ERROR_FILE, "$JAVA_HOME must be defined"
        return
    print >> OUTPUT_FILE, ("Executing command: " + java_home +
                           "/bin/java {}\n".format(args).replace('(\'', '').replace('\', \'', ' ').replace('\')', ''))
    process = Popen([ java_home + '/bin/java']+list(args), stdout=PIPE, stderr=STDOUT)
    ret = []
    while process.poll() is None:
        line = process.stdout.readline()
        if line != '' and line.endswith('\n'):
            print >> OUTPUT_FILE, line.strip()
            ret.append(line[:-1])
    return ret
