'''
Copyright (c) 2016 The Hyve B.V.
This code is licensed under the GNU Affero General Public License (AGPL),
version 3, or (at your option) any later version.
'''

import unittest
from importer import validateData

# globals:
server = 'http://localhost:8080/cbioportal'

# Test cases around running the complete validateData script (such as "does it return the correct exit status?" 
# or "does it generate the html report when requested?", etc)
class ValidateDataSystemTester(unittest.TestCase):
    
        
    def test_exit_status(self):
        '''
        If there are errors, the script should return 
                0: 'succeeded',
                1: 'failed',
                2: 'not performed as problems occurred',
                3: 'succeeded with warnings'
        '''
        
        # ======= study 0 : no errors, expected exit_status = 0
        
        #Build up arguments and run
        print "===study 0"
        args = ['--study_directory','test_data/study_es_0/', 
                    '--url_server', server, '-v'] # -q instead ??
        args = validateData.interface(args)
        # Execute main function with arguments provided through sys.argv
        exit_status = validateData.main_validate(args)
        self.assertEquals(0, exit_status)
        
        # ======= study 1 : errors, expected exit_status = 1
        #Build up arguments and run
        print "===study 1"
        args = ['--study_directory','test_data/study_es_1/', 
                    '--url_server', server, '-v'] # -q instead ??
        args = validateData.interface(args)
        # Execute main function with arguments provided through sys.argv
        exit_status = validateData.main_validate(args)
        self.assertEquals(1, exit_status)
                
        # ======= test to fail: give wrong hugo file, or let a meta file point to a non-existing data file, expected exit_status = 2
        #Build up arguments and run
        print "===study invalid"
        args = ['--study_directory','test_data/study_es_invalid/', 
                    '--url_server', server, '-v'] # -q instead ??
        args = validateData.interface(args)
        # Execute main function with arguments provided through sys.argv
        exit_status = validateData.main_validate(args)
        self.assertEquals(2, exit_status)
        
        
        # ======= study 3 : warnings only, expected exit_status = 3
        # data_filename: test
        #        C1orf159    54991    -0.005    -0.550    -0.021    0.010    -0.045    -0.397    0.142    -0.061    0.091    -0.612    -0.360    -0.487    0.187    -0.007    0.015    0.208    -0.026    -0.904    -0.187    1.280    -0.076    0.182    0.024    0.372    -0.039    0.138    -0.006    0.006    -0.548    -0.420    -0.040    0.024    -0.633    -0.249    0.379    -0.005    -0.020    0.006    -0.436    0.036    -0.430    -0.321    0.001    0.011    -0.079    -0.452    -0.050    -0.008    -0.025    -0.020    -0.337    -0.067    0.006    -0.222    0.244    0.064    -0.079    0.135    0.070    0.994    -0.021    -0.466    0.024    -0.046    -0.449    0.050    0.005    -0.291    -0.593    -0.152    -0.249    -0.479    -0.306    0.066    0.008    -0.005    0.055    0.004    -0.097    -0.403    -0.010    0.388    -0.176    -0.015    -0.024    0.015    -0.084    -0.100    -0.647    0.024    -0.005    -0.049    -0.110    0.152    -0.012    0.064    -0.196    -0.030    -0.281    -0.019    0.280    0.053    -0.518    -0.043    -0.291    -0.382    0.149    -0.031    -0.040    -0.019    0.002    -0.127    0.132    -0.054    0.036    -0.326    -0.320    0.016    -0.840    0.576    -0.585    -0.692    -0.417    -0.307    -0.046    -0.571    -0.159    0.086    0.033    -0.722    0.125    -0.099    -0.093    0.017    -0.157    -0.026    -0.062    -0.569    0.154    -0.015    -0.521    -0.031    0.011    0.090    -0.114    -0.032    0.051    -0.006    -0.134    -0.417    -0.631    0.255    -0.379    -0.151    -0.155    -0.759    0.030    -0.030    0.093    -0.263    0.005    -0.345    -0.015    -0.011    0.045    0.043    -0.147    -0.759    -0.606    -0.245    -0.425    0.036    -0.574    -0.122    -0.709    -0.406    -0.264    -0.010    -0.400    0.134    0.106    -0.709    0.264    0.015    0.017    -0.377    0.039    -0.233    -0.034    -0.184    -0.386    0.069    -0.065    -0.138    0.018    0.004    -0.016    0.017    0.021    -0.477    -0.472    -0.414    -0.157    -0.557    -0.448    -0.142    -0.820    -0.439    -0.393    -0.726    0.042    -0.003    0.044    -0.025    -0.326    0.066    0.693    -0.343    0.950    0.038    0.035    -0.549    -0.327    -0.022    0.006    0.362    0.138    0.059    -0.149    0.027    -0.016    -0.274    -0.814    -0.659    -0.034    -0.067    -0.305    -0.016    -0.449    0.002    -0.268    0.021    -0.023    -0.020    -0.027    -0.077    -0.532    1.200    -0.249    -0.611    -0.566    0.005    -0.336    -0.465    0.016    0.013    0.059    -0.473    -0.031    -0.494    -0.757    0.024    -0.282    -0.448    -0.152    -0.096    0.096    0.183    -0.418    -0.286    -0.104    0.027    0.072    -1.152    0.014    0.004    -0.028    -0.675    -0.407    0.082    -0.694    -0.670    -0.035    0.228    0.273    -0.384    -0.021    0.025    -0.049    -0.276    -0.060    -0.179    -0.057    -0.110    0.372    0.028    -0.714    -0.891    0.013    0.603    -0.114    -0.748    0.025    1.422    0.291    0.000    -0.123    -0.061    -0.027    0.069    -0.209    -0.109    0.014    0.063    0.007    -0.034    0.382    -0.042    -0.690    -0.739    0.134    -0.432    0.144    0.017    -0.011    -0.008    0.443    -0.369    -0.582    -0.346    0.002    -0.012    -0.047    -0.029    -0.054    -0.575    0.401    -0.163    -0.328    -0.400    -0.518    -0.064    0.024    -0.461    0.000    -0.363    -0.010    -0.043    -0.202    0.033    -0.058    0.055    0.069    -0.158    -0.047    -0.173    -0.425    -0.855    0.045    0.007    -0.552    -0.075    -0.025    -0.109    -0.355    -0.019    -0.514    0.165    -0.344    0.041    0.041    -0.020    -0.380    -0.602    -0.498    -0.231    0.033    0.002    0.722    -0.011    -0.084    -0.039    -0.539    -0.507    -0.142    -0.569    -0.277    -0.453    0.114    0.035    -0.049    -0.011    -0.180    -0.380    -0.275    0.016    -0.401    -0.219    0.015    -0.017    -0.149    -0.056    -0.027    -0.056    -0.066    -0.013    -0.305    -0.009    -0.017    0.029    -0.665    -0.254    0.025    -0.094    0.206    -0.414    0.041    -0.565    -0.639    -0.493    1.405    -0.062    -0.025    -0.498    0.445    -0.004    -0.028    -0.078    -0.045    -0.012    -0.025    0.034    1.610    -0.046    0.062    -0.626    -0.182    -0.002    -0.222    -0.019    -0.159    0.249    0.024    -0.462    -0.673    0.008    -0.012    0.047    -0.013    -0.536    -0.335    0.030    0.083    -0.040    0.052    0.032    0.047    -0.723    -0.410    -0.122    -0.065    -0.410    0.117    -0.001    0.002    -0.018    -0.263    0.002    -0.054    0.005    -0.436    -0.463    -0.240    0.041    -0.102    -0.332    -0.557    -0.501    -0.132    -0.098    -0.330    0.656    -0.527    0.012    -0.019    -0.007    -0.244    2.422    0.016    0.008    -0.538    -0.151    -0.730    -0.111    0.021    -0.730    -0.092    -0.202    0.074    -0.128    -0.081    -0.011    0.002    -0.045    -0.699    -0.051    0.031    0.018    -0.048    -0.053    -0.041    -0.827    0.092    -0.128    -0.011    0.163    -0.490    -0.149    -0.055    -0.197    0.000    -0.017    -0.002    0.041    -0.254    -0.117    0.037    -0.650    -0.119    -0.277    -0.184    -0.098    -0.583    -0.156    -0.105    -0.367    -0.094    -0.040    0.000    -0.099    -0.786    -0.327    -0.348    -0.242    -0.158    -0.075    0.010    -0.639    -0.014    0.101    -0.100    -0.046    -0.505    0.015    -0.154    -0.745    -0.290    -0.856    -0.001    -0.046    -0.255    0.230    -0.008    -0.233    0.307    -0.654    -0.047    -0.789    -0.144    -0.703    -0.363    -0.369    -0.463    0.026    0.079    -0.504    -0.419    0.142    0.454    -0.770    -0.061    0.008    -0.133    -0.082    -0.124    -0.444    -0.001    -0.053    -0.491    0.002    0.022    -0.078    -0.034    0.060    -0.454    -0.758    -0.666    -0.383    -0.379    -0.076    -0.025    -0.206    -0.315    -0.309    -0.493    -0.596    -0.226    -0.073    -0.350    -0.923    -0.552    -0.418    -0.039    -0.253    -0.023    -0.571    0.002    0.729    -0.113    0.045    -0.045    0.095    -0.034    -0.040    -0.119    -0.292    -0.011    -0.269    0.150    -0.360    0.039    -0.364    -0.820    -0.061    -0.179    -0.722    -0.051    -0.101    -0.438    -0.039    0.054    -0.710    -0.027    -0.310    0.049    0.144    0.040    0.254    0.360    -0.017    -0.025    -0.526    0.007    0.005    -0.954    0.400    0.147    -0.113    -0.634    -0.026    0.008    0.073    0.006    -0.758    -0.034    0.554    0.250    0.071    0.078    -0.057    0.000    -0.096    0.015    0.016    -0.679    0.011    -0.055    -0.553    -0.364    -0.011    -0.141    -0.014    -0.124    0.076    -0.081    -0.040    -0.365    -0.381    -0.115    -0.492    -0.637    -0.551    0.530    -0.109    0.023    0.654    -0.080    -0.005    0.011    -0.262    -0.099    1.140    0.251    -0.447    -0.239    -0.018    -0.115    -0.012    -0.317    -0.263    -0.129    -0.119    0.420    -0.335    -0.100    -0.734    -0.076    0.675    0.030    0.134    -0.577    -0.026    -0.527    0.317    -0.292    -0.168    0.046    -0.451    -0.037    -0.020    -0.353    -0.419    0.080    -0.097    -0.189    -0.574    0.012    -0.122    -0.407    -0.016    -0.470    -0.029    -0.221    0.045    -0.345    -0.600    0.025    -0.151    -0.034    -0.014    0.004    -0.016    -0.116    -0.639    -0.450    -0.644    -0.047    -0.320    -0.080    -0.017    -0.018    -0.135    0.001    0.025    -0.036    0.045    -0.079    0.003    -0.003    -0.063    -0.294    -0.368    -0.009    -0.261    -0.128    -0.029    -0.951    -0.340
        #Build up arguments and run
        print "===study 3"
        args = ['--study_directory','test_data/study_es_3/', 
                    '--url_server', server, '-v'] # -q instead ??
        args = validateData.interface(args)
        # Execute main function with arguments provided through sys.argv
        exit_status = validateData.main_validate(args)
        self.assertEquals(3, exit_status)

    def test_html_output(self):
        '''
        Test if html file is correctly generated when 'html_table' is given
        '''
        #Build up arguments and run
        args = ['--study_directory','test_data/study_es_0/', 
                    '--url_server', server, '-v', '--html_table', 'test_data/study_es_0/result_report.html']
        args = validateData.interface(args)
        # Execute main function with arguments provided through sys.argv
        exit_status = validateData.main_validate(args)
        # TODO - assert if html file is present
        self.assertEquals(0, exit_status)
     
    def test_problem_in_clinical(self):
        '''
        When clinical file has a problem, we want the program to abort and give just this error 
        before validating other files (because other files cannot be validated in case clinical is wrong).
        Here we validate if script is giving proper error. 
        '''
        #Build up arguments and run
        print '==test_problem_in_clinical=='
        args = ['--study_directory','test_data/study_wr_clin/', 
                    '--url_server', server, '-v', '--html_table', 'test_data/study_wr_clin/result_report.html']
        args = validateData.interface(args)
        # Execute main function with arguments provided through sys.argv
        exit_status = validateData.main_validate(args)
        self.assertEquals(1, exit_status)
        # TODO - set logger in main_validate and read out buffer here to assert on nr of errors
        
    def test_normal_samples_list_in_maf(self):
        '''
        For mutations MAF files there is a column called "Matched_Norm_Sample_Barcode". 
        In the respective meta file it is possible to give a list of sample codes against which this 
        column "Matched_Norm_Sample_Barcode" is validated. Here we test if this 
        validation works well.
        '''
        #Build up arguments and run
        print '==test_normal_samples_list_in_maf=='
        args = ['--study_directory','test_data/study_maf_test/', 
                    '--url_server', server, '-v', '--html_table', 'test_data/study_maf_test/result_report.html']
        args = validateData.interface(args)
        # Execute main function with arguments provided through sys.argv
        exit_status = validateData.main_validate(args)
        # should fail because of errors with invalid Matched_Norm_Sample_Barcode values
        self.assertEquals(1, exit_status)
        