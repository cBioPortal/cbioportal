#! /usr/bin/env python

# ------------------------------------------------------------------------------
# Common components used by various cbioportal scripts.
# ------------------------------------------------------------------------------


import os
import sys
import csv
from subprocess import Popen, PIPE, STDOUT


# ------------------------------------------------------------------------------
# globals

ERROR_FILE = sys.stderr
OUTPUT_FILE = sys.stdout

IMPORT_STUDY_CLASS = "org.mskcc.cbio.portal.scripts.ImportCancerStudy"
REMOVE_STUDY_CLASS = "org.mskcc.cbio.portal.scripts.RemoveCancerStudy"
IMPORT_CANCER_TYPE_CLASS = "org.mskcc.cbio.portal.scripts.ImportTypesOfCancers"
IMPORT_CASE_LIST_CLASS = "org.mskcc.cbio.portal.scripts.ImportPatientList"

class MetaFileTypes(object):
    """how we differentiate between data types."""
    STUDY = 'meta_study'
    CANCER_TYPE = 'meta_cancer_type'
    CLINICAL = 'meta_clinical'
    CNA = 'meta_CNA'
    LOG2 = 'meta_log2CNA'
    SEG = 'meta_segment'
    EXPRESSION = 'meta_expression'
    MUTATION = 'meta_mutations_extended'
    METHYLATION = 'meta_methylation'
    FUSION = 'meta_fusions'
    RPPA = 'meta_rppa'
    TIMELINE = 'meta_timeline'
    CASE_LIST = 'case_list'

# fields allowed in each meta file type, maps to True if required
META_FIELD_MAP = {
    MetaFileTypes.CANCER_TYPE: {
        'type_of_cancer': True,
        'name': True,
        'clinical_trial_keywords': True,
        'dedicated_color': True,
        'short_name': True
    },
    MetaFileTypes.STUDY: {
        'cancer_study_identifier': True,
        'type_of_cancer': True,
        'name': True,
        'description': True,
        'short_name': True,
        'citation': False,
        'pmid': False,
        'groups': False
    },
    MetaFileTypes.CLINICAL: {
        'cancer_study_identifier': True,
        'genetic_alteration_type': True,
        'datatype': True,
        'show_profile_in_analysis_tab': True,
        'profile_name': True,
        'profile_description': True,
        'data_filename': True
    },
    MetaFileTypes.CNA: {
        'cancer_study_identifier': True,
        'genetic_alteration_type': True,
        'datatype': True,
        'stable_id': True,
        'show_profile_in_analysis_tab': True,
        'profile_name': True,
        'profile_description': True,
        'data_filename': True
    },
    MetaFileTypes.LOG2: {
        'cancer_study_identifier': True,
        'genetic_alteration_type': True,
        'datatype': True,
        'stable_id': True,
        'show_profile_in_analysis_tab': True,
        'profile_name': True,
        'profile_description': True,
        'data_filename': True
    },
    MetaFileTypes.SEG: {
        'cancer_study_identifier': True,
        'genetic_alteration_type': True,
        'datatype': True,
        'show_profile_in_analysis_tab': True,
        'reference_genome_id': True,
        'data_filename': True,
        'description': True
    },
    MetaFileTypes.MUTATION: {
        'cancer_study_identifier': True,
        'genetic_alteration_type': True,
        'datatype': True,
        'stable_id': True,
        'show_profile_in_analysis_tab': True,
        'profile_name': True,
        'profile_description': True,
        'data_filename': True,
        'normal_samples_list': False
    },
    MetaFileTypes.EXPRESSION: {
        'cancer_study_identifier': True,
        'genetic_alteration_type': True,
        'datatype': True,
        'stable_id': True,
        'show_profile_in_analysis_tab': True,
        'profile_name': True,
        'profile_description': True,
        'data_filename': True
    },
    MetaFileTypes.METHYLATION: {
        'cancer_study_identifier': True,
        'genetic_alteration_type': True,
        'datatype': True,
        'stable_id': True,
        'show_profile_in_analysis_tab': True,
        'profile_name': True,
        'profile_description': True,
        'data_filename': True
    },
    MetaFileTypes.RPPA: {
        'cancer_study_identifier': True,
        'genetic_alteration_type': True,
        'datatype': True,
        'stable_id': True,
        'show_profile_in_analysis_tab': True,
        'profile_name': True,
        'profile_description': True,
        'data_filename': True
    },
    MetaFileTypes.FUSION: {
        'cancer_study_identifier': True,
        'genetic_alteration_type': True,
        'datatype': True,
        'stable_id': True,
        'show_profile_in_analysis_tab': True,
        'profile_name': True,
        'profile_description': True,
        'data_filename': True
    },
    MetaFileTypes.TIMELINE: {
        'cancer_study_identifier': True,
        'genetic_alteration_type': True,
        'data_filename': True
    },
    MetaFileTypes.CASE_LIST: {
        'cancer_study_identifier': True,
        'stable_id': True,
        'case_list_name': True,
        'case_list_description': True,
        'case_list_ids': True,
        'case_list_category': False
    }
}

IMPORTER_CLASSNAME_BY_META_TYPE = {
    MetaFileTypes.STUDY: IMPORT_STUDY_CLASS,
    MetaFileTypes.CANCER_TYPE: IMPORT_CANCER_TYPE_CLASS,
    MetaFileTypes.CLINICAL: "org.mskcc.cbio.portal.scripts.ImportClinicalData",
    MetaFileTypes.CNA: "org.mskcc.cbio.portal.scripts.ImportProfileData",
    # TODO: check if this is correct
    MetaFileTypes.LOG2: "org.mskcc.cbio.portal.scripts.ImportProfileData",
    MetaFileTypes.SEG: "org.mskcc.cbio.portal.scripts.ImportCopyNumberSegmentData",
    MetaFileTypes.EXPRESSION: "org.mskcc.cbio.portal.scripts.ImportProfileData",
    MetaFileTypes.MUTATION: "org.mskcc.cbio.portal.scripts.ImportProfileData",
    MetaFileTypes.METHYLATION: "org.mskcc.cbio.portal.scripts.ImportProfileData",
    MetaFileTypes.FUSION: "org.mskcc.cbio.portal.scripts.ImportProfileData",
    MetaFileTypes.RPPA: "org.mskcc.cbio.portal.scripts.ImportProteinArrayData",
    MetaFileTypes.TIMELINE: "org.mskcc.cbio.portal.scripts.ImportTimelineData",
    MetaFileTypes.CASE_LIST: IMPORT_CASE_LIST_CLASS
    # TODO: enable when documented
    #MetaFileTypes.GISTIC: "org.mskcc.cbio.portal.scripts.ImportGisticData",
    #MetaFileTypes.MUTATION_SIGNIFICANCE: "org.mskcc.cbio.portal.scripts.ImportMutSigData"
}

IMPORTER_REQUIRES_METADATA = {
    "org.mskcc.cbio.portal.scripts.ImportClinicalData" : False,
    "org.mskcc.cbio.portal.scripts.ImportCopyNumberSegmentData" : False,
    "org.mskcc.cbio.portal.scripts.ImportGisticData" : False,
    "org.mskcc.cbio.portal.scripts.ImportMutSigData" : False,
    "org.mskcc.cbio.portal.scripts.ImportProteinArrayData" : False,
    "org.mskcc.cbio.portal.scripts.ImportProfileData" : True,
    "org.mskcc.cbio.portal.scripts.ImportTimelineData" : True
}

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
                 profile_description, profile_name, meta_file_type, data_filename):
        self.cancer_study_identifier = cancer_study_identifier
        self.genetic_alteration_type = genetic_alteration_type
        self.datatype = datatype
        self.stable_id = stable_id
        self.show_profile_in_analysis_tab = show_profile_in_analysis_tab
        self.profile_description = profile_description
        self.profile_name = profile_name
        self.meta_file_type = meta_file_type
        self.data_filename = data_filename


# ------------------------------------------------------------------------------
# sub-routines

def get_meta_file_type(metaDictionary, logger=None, filename=''):
    """
     Returns one of the metatypes :
        MetaFileTypes.SEG = 'meta_segment'
        MetaFileTypes.STUDY = 'meta_study'
        MetaFileTypes.CANCER_TYPE = 'meta_cancer_type'
        MetaFileTypes.MUTATION = 'meta_mutations_extended'
        MetaFileTypes.CNA = 'meta_CNA'
        MetaFileTypes.CLINICAL = 'meta_clinical'
        MetaFileTypes.LOG2 = 'meta_log2CNA'
        MetaFileTypes.EXPRESSION = 'meta_expression'
        MetaFileTypes.FUSION = 'meta_fusions'
        MetaFileTypes.METHYLATION = 'meta_methylation'
        MetaFileTypes.RPPA = 'meta_rppa'
        MetaFileTypes.TIMELINE = 'meta_timeline'
    """
    # GENETIC_ALTERATION_TYPE    DATATYPE    meta
    alt_type_datatype_to_meta = {
        #clinical and timeline
        ("CLINICAL", "CLINICAL"): MetaFileTypes.CLINICAL,
        ("CLINICAL", "TIMELINE"): MetaFileTypes.TIMELINE,
        #rppa
        ("PROTEIN_LEVEL", "LOG2-VALUE"): MetaFileTypes.RPPA,
        ("PROTEIN_LEVEL", "Z-SCORE"): MetaFileTypes.RPPA,
        #cna
        ("COPY_NUMBER_ALTERATION", "DISCRETE"): MetaFileTypes.CNA,
        #("COPY_NUMBER_ALTERATION", "CONTINUOUS"): MetaFileTypes.CNA, ?? TODO - add later, when documented
        #log2cna
        ("COPY_NUMBER_ALTERATION", "LOG2-VALUE"): MetaFileTypes.LOG2,
        #expression
        ("MRNA_EXPRESSION", "CONTINUOUS"): MetaFileTypes.EXPRESSION,
        ("MRNA_EXPRESSION", "Z-SCORE"): MetaFileTypes.EXPRESSION,
        ("MRNA_EXPRESSION", "DISCRETE"): MetaFileTypes.EXPRESSION,
        #mutations
        ("MUTATION_EXTENDED", "MAF"): MetaFileTypes.MUTATION,
        #others
        ("COPY_NUMBER_ALTERATION", "SEG"): MetaFileTypes.SEG,
        ("METHYLATION", "CONTINUOUS"): MetaFileTypes.METHYLATION,
        ("FUSION", "FUSION"): MetaFileTypes.FUSION
    }
    result = None
    if 'genetic_alteration_type' in metaDictionary and 'datatype' in metaDictionary:
        genetic_alteration_type = metaDictionary['genetic_alteration_type']
        data_type = metaDictionary['datatype']
        if (genetic_alteration_type, data_type) not in alt_type_datatype_to_meta:
            if logger is not None:
                logger.error('Could not determine the file type. Please check your meta files for correct configuration.',
                             extra={'filename_': os.path.basename(filename),
                                    'cause': 'genetic_alteration_type: ' + metaDictionary['genetic_alteration_type'] +
                                             ', datatype: ' + metaDictionary['datatype']})
        else:
            result = alt_type_datatype_to_meta[(genetic_alteration_type, data_type)]
    elif 'cancer_study_identifier' in metaDictionary and 'type_of_cancer' in metaDictionary:
        result = MetaFileTypes.STUDY
    elif 'type_of_cancer' in metaDictionary:
        result = MetaFileTypes.CANCER_TYPE
    else:
        if logger is not None:
            logger.error('Could not determine the file type. Did not find expected meta file fields. Please check your meta files for correct configuration.',
                             extra={'filename_': os.path.basename(filename)})

    return result


def get_properties(filename):
    properties = {}
    file_ = open(filename, 'r')
    for line in file_:
        line = line.strip()
        # skip line if its blank or a comment
        if len(line) == 0:
            continue
        # store name/value
        property_ = line.split(': ', 1)
        if (len(property_) != 2):
            print >> ERROR_FILE, 'Skipping invalid entry in file_: ' + line
            continue
        properties[property_[0]] = property_[1].strip()
    file_.close()
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
    return MetafileProperties(
        properties["cancer_study_identifier"],
        properties["genetic_alteration_type"],
        properties["datatype"],
        properties["stable_id"],
        properties["show_profile_in_analysis_tab"],
        properties["profile_name"],
        properties["profile_description"],
        properties["meta_file_type"],
        properties["data_filename"])


def validate_types_and_id(metaDictionary, logger, filename):
    """Validate a genetic_alteration_type, datatype (and stable_id in some cases) against the predefined
    allowed combinations found in ./allowed_data_types.txt
    """
    result = True
    # this validation only applies to items that have genetic_alteration_type and datatype and stable_id
    if 'genetic_alteration_type' in metaDictionary and 'datatype' in metaDictionary and 'stable_id' in metaDictionary:
        alt_type_datatype_and_stable_id = {}
        script_dir = os.path.dirname(__file__)
        allowed_data_types_file_name = os.path.join(script_dir, "allowed_data_types.txt")
        data_line_nr = 0
        # build up map alt_type_datatype_and_stable_id:
        with open(allowed_data_types_file_name) as allowed_data_types_file:
            for line in allowed_data_types_file:
                if line.startswith("#"):
                    continue
                data_line_nr += 1
                # skip header, so if line is not header then process as tab separated:
                if (data_line_nr > 1):
                    line_cols = csv.reader([line], delimiter='\t').next()
                    genetic_alteration_type = line_cols[0]
                    data_type = line_cols[1]
                    # add to map:
                    if (genetic_alteration_type, data_type) not in alt_type_datatype_and_stable_id:
                        alt_type_datatype_and_stable_id[(genetic_alteration_type, data_type)] = []
                    alt_type_datatype_and_stable_id[(genetic_alteration_type, data_type)].append(line_cols[2])
        # init:
        stable_id = metaDictionary['stable_id']
        genetic_alteration_type = metaDictionary['genetic_alteration_type']
        data_type = metaDictionary['datatype']
        # validate the genetic_alteration_type/data_type combination:
        if (genetic_alteration_type, data_type) not in alt_type_datatype_and_stable_id:
            # unexpected as this is already validated in get_meta_file_type
            raise RuntimeError('Unexpected error: genetic_alteration_type and data_type combination not found in allowed_data_types.txt.',
                               genetic_alteration_type, data_type)
        # validate stable_id:
        elif stable_id not in alt_type_datatype_and_stable_id[(genetic_alteration_type, data_type)]:
            logger.error("Invalid stable id for genetic_alteration_type '%s', "
                         "data_type '%s'; expected one of [%s]",
                        genetic_alteration_type,
                        data_type,
                        ', '.join(alt_type_datatype_and_stable_id[(genetic_alteration_type, data_type)]),
                        extra={'filename_': filename,
                               'cause': stable_id}
                        )
            result = False

    return result


def parse_metadata_file(filename,
                        logger,
                        study_id=None,
                        known_cancer_types=None,
                        genome_name=None,
                        case_list=False):

    """Validate a metafile and return a dictionary of values read from it and
    the meta_file_type according to get_meta_file_type.

    Return `None` if the file is invalid. If `case_list` is True,
    validate the file as a case list instead of a meta file.

    :param filename: name of the meta file
    :param logger: the logging.Logger instance to log warnings and errors to
    :param study_id: cancer study id found in previous files (or None). All subsequent
                     meta files should comply to this in the field 'cancer_study_identifier'
    :param known_cancer_types: dict of cancer types defined in the portal,
                               for validation
    :param genome_name: supported reference genome name, for validation
    :param case_list: whether this meta file is a case list (special case)
    """

    metaDictionary = {}
    with open(filename, 'rU') as metafile:
        for line_index, line in enumerate(metafile):
            if ':' not in line:
                logger.error(
                    "Invalid %s file entry, no ':' found",
                    {True: 'case list', False: 'meta'}[case_list],
                    extra={'filename_': filename,
                           'line_number': line_index + 1})
                return None
            key_value = line.split(':', 1)
            if len(key_value) == 2:
                metaDictionary[key_value[0]] = key_value[1].strip()

    if case_list:
        meta_file_type = MetaFileTypes.CASE_LIST
    else:
        meta_file_type = get_meta_file_type(metaDictionary, logger, filename)
        if meta_file_type is None:
            # skip this file (can't validate unknown file types)
            return None

    missing_fields = []
    for field in META_FIELD_MAP[meta_file_type]:
        mandatory = META_FIELD_MAP[meta_file_type][field]
        if field not in metaDictionary and mandatory:
            logger.error("Missing field '%s' in %s file",
                         field,
                         {True: 'case list', False: 'meta'}[case_list],
                         extra={'filename_': filename})
            missing_fields.append(field)

    if missing_fields:
        # skip this file (the fields may be required for validation)
        return None

    # validate genetic_alteration_type, datatype, stable_id
    stable_id_mandatory = 'stable_id' in META_FIELD_MAP[meta_file_type] and META_FIELD_MAP[meta_file_type]['stable_id']
    if stable_id_mandatory:
        valid_types_and_id = validate_types_and_id(metaDictionary, logger, filename)
        if not valid_types_and_id:
            return None

    for field in metaDictionary:
        if field not in META_FIELD_MAP[meta_file_type]:
            logger.warning(
                'Unrecognized field in %s file',
                {True: 'case list', False: 'meta'}[case_list],
                extra={'filename_': filename,
                       'cause': field})

    # check that cancer study identifiers across files so far are consistent.
    if (
            study_id is not None and
            'cancer_study_identifier' in metaDictionary and
            study_id != metaDictionary['cancer_study_identifier']):
        logger.error(
            "Cancer study identifier is not consistent across "
            "files, expected '%s'",
            study_id,
            extra={'filename_': filename,
                   'cause': metaDictionary['cancer_study_identifier']})
        return None

    if meta_file_type == MetaFileTypes.CANCER_TYPE:
        # compare a meta_cancer_type file with the portal instance
        if known_cancer_types is not None:
            file_cancer_type = metaDictionary.get('type_of_cancer')
            if file_cancer_type not in known_cancer_types:
                logger.warning(
                    'New disease type will be added to the portal',
                    extra={'filename_': filename,
                           'cause': file_cancer_type})
            else:
                existing_info = known_cancer_types[file_cancer_type]
                invalid_fields_found = False
                for field in metaDictionary:
                    if (
                            field in existing_info and
                            field != 'cancer_type_id' and
                            metaDictionary[field] != existing_info[field]):
                        logger.error(
                            "%s field of cancer type does not match the "
                            "portal, '%s' expected",
                            field,
                            existing_info[field],
                            extra={'filename_': filename,
                                   'cause': metaDictionary[field]})
                        invalid_fields_found = True
                if invalid_fields_found:
                    return None
    # check fields specific to seg meta file
    elif meta_file_type == MetaFileTypes.SEG:
        if metaDictionary['reference_genome_id'] != genome_name:
            logger.error(
                'Reference_genome_id is not %s',
                genome_name,
                extra={'filename_': filename,
                       'cause': metaDictionary['reference_genome_id']})
            return None

    return metaDictionary,meta_file_type


def run_java(*args):
    java_home = os.environ['JAVA_HOME']
    if len(java_home) == 0:
        print >> ERROR_FILE, "$JAVA_HOME must be defined"
        return
    #print >> OUTPUT_FILE, ("Executing command: " + java_home +
    #                       "/bin/java {}\n".format(args).replace('(\'', '').replace('\', \'', ' ').replace('\')', ''))
    process = Popen([ java_home + '/bin/java']+list(args), stdout=PIPE, stderr=STDOUT)
    ret = []
    while process.poll() is None:
        line = process.stdout.readline()
        if line != '' and line.endswith('\n'):
            print >> OUTPUT_FILE, line.strip()
            ret.append(line[:-1])
    return ret
