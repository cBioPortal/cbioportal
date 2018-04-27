import sys
import logging
import argparse
from sqlalchemy import create_engine
from sqlalchemy import exc
import smtplib
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText
import cbioportalImporter
import validateData

def send_mail(physician, patient, sample):
    me = "cbioportal@uhnresearch.ca"
    physician = "kzhu@uhnresearch.ca"

    # Create message container - the correct MIME type is multipart/alternative.
    msg = MIMEMultipart('alternative')
    msg['Subject'] = "Link"
    msg['From'] = me
    msg['To'] = physician
    
    #http://localhost:8081/cbioportal/case.do#/patient?studyId=OCTANE&sampleId=OCT-01-0001_Tumour
    # Create the body of the message (a plain-text and an HTML version).
    text = "Dear Dr. %s!\nnew sample is available for your patient %s\n"\
        "Here is the link to view new sample:\n"\
        "http://localhost:8081/cbioportal/case.do#/patient?studyId=OCTANE&sampleId=%s"%(physician, patient, sample)
    html = """\
    <html>
      <head>New Sample Availabe for your Review</head>
      <body>
        <p>Dear Dr. %s<br>
           new sample is available for your patient %s<br>
           Here is the <a href="http://localhost:8081/cbioportal/case.do#/patient?studyId=OCTANE&sampleId=%s">link</a> 
           to view new sample.
        </p>
      </body>
    </html>
    """%(physician, patient, sample)
    
    # Record the MIME types of both parts - text/plain and text/html.
    part1 = MIMEText(text, 'plain')
    part2 = MIMEText(html, 'html')
    
    # Attach parts into message container.
    # According to RFC 2046, the last part of a multipart message, in this case
    # the HTML message, is best and preferred.
    msg.attach(part1)
    msg.attach(part2)
    
    # Send the message via local SMTP server.
    s = smtplib.SMTP('smtp.uhnresearch.ca')
    # sendmail function takes 3 arguments: sender's address, recipient's address
    # and message to send - here it is sent as one string.
    s.sendmail(me, physician, msg.as_string())
    s.quit()

def get_options():
    parser = argparse.ArgumentParser(description='cBioPortal Importer')
    parser.add_argument('-u', '--url_server',
                                   type=str,
                                   default='http://localhost/cbioportal',
                                   help='URL to cBioPortal server. You can '
                                        'set this if your URL is not '
                                        'http://localhost/cbioportal')
    parser.add_argument('-html', '--html_table', type=str, required=False,
                        help='path to html report output file')
    parser.add_argument('-s', '--study_directory', type=str, required=False,
                        help='path to directory.')
    parser.add_argument('-r', '--relaxed_clinical_definitions', required=False,
                        action='store_true', default=False,
                        help='Option to enable relaxed mode for validator when '
                             'validating clinical data without header definitions')
    parser.add_argument('-m', '--strict_maf_checks', required=False,
                        action='store_true', default=False,
                        help='Option to enable strict mode for validator when '
                             'validating mutation data')
    parser.add_argument('-n', '--no_portal_checks', default=False,
                                   action='store_true',
                                   help='Skip tests requiring information '
                                        'from the cBioPortal installation')
    parser.add_argument('-P', '--portal_properties', type=str,
                        help='portal.properties file path (default: assumed hg19)',
                        required=False)
    parser.add_argument('-jar', '--jar_path', type=str, required=False,
                        help='Path to scripts JAR file (default: $PORTAL_HOME/scripts/target/scripts-*.jar)')
    parser.add_argument('-c', '--cancer_study', type=str, required=True,
                        help='Cancer study identifier')
    parser.add_argument('-o', '--override_warning', action='store_true',
                        help='override warnings and continue importing')
    parser.add_argument('-v', '--verbose', required=False, action='store_true',
                        help='report status info messages in addition ')
    parser = parser.parse_args()
    return parser

def get_sample_info(connection):
    try:
        sql_str = """
        select p.stable_id as patient_id, s.STABLE_ID as sample_id, cp.ATTR_VALUE as physician
        from patient p 
        join sample s on s.PATIENT_ID = p.INTERNAL_ID
        join clinical_patient cp on cp.INTERNAL_ID = p.INTERNAL_ID
        where p. CANCER_STUDY_ID = %s
        and cp.ATTR_ID = 'TREATING_PHYSICIAN'
        """%(cancer_study_id)
        return connection.execute(sql_str)
    except:
        raise

def get_study_id(cancer_study_identifier):
    try:
        sql_str = """
            select cancer_study_id from cancer_study where CANCER_STUDY_IDENTIFIER = '%s'
        """%cancer_study_identifier
        result = connection.execute(sql_str)
        for row in result:
            return row['cancer_study_id']
    except:
        raise 

def get_db_connection():
    try:
        # mysql-python
        engine = create_engine('mysql+mysqldb://cbio_user:cbi0pass@localhost/cgds')
        return engine.connect()
    except exc.SQLAlchemyError:
        raise 

def get_logger():
    logging.basicConfig(level=logging.DEBUG, format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
    return logging.getLogger('cbio_importer')

def get_sample_dic(sample_list):
    res_dic = {}
    for row in sample_list:
        patient_id = row['patient_id']
        sample_id = row['sample_id']
        if patient_id not in res_dic.keys():
            res_dic[patient_id] = [sample_id]
            res_dic['physician'] = row['physician']
        else:
            res_dic[patient_id].append(sample_id)
    return res_dic

if __name__ == '__main__':
    # Parse user input
    args = get_options()

    
    connection = get_db_connection()
    logger = get_logger()
    
    logger.info(args)

    cancer_study_id = get_study_id(args.cancer_study)
    logger.info("Cancer Study ID: %s"%cancer_study_id)
    
    try:
        logger.info("check exisiting samples...")
        sample_list_old = get_sample_info(connection)

        old_samples = get_sample_dic(sample_list_old)
        logger.info(old_samples)
            
        # Import study
        # exit_code = validateData.main_validate(args)
        # if not exit_code in [1,2]:    
        #     cbioportalImporter.main(args)
        # else:
        #     logger.error('Validation of study {status}.'.format(
        #         status={0: 'succeeded',
        #                 1: 'failed',
        #                 2: 'not performed as problems occurred',
        #                 3: 'succeeded with warnings'}.get(exit_code, 'unknown')))
        #     system.exit(exit_code)
        # get sample list again
        logger.info("check new samples...")
        sample_list_new = get_sample_info(connection)
        new_samples = get_sample_dic(sample_list_new)
        logger.info(new_samples)
    # try import study
    except exc.SQLAlchemyError as e:
        logger.error(e.message)

    finally:
        connection.close()