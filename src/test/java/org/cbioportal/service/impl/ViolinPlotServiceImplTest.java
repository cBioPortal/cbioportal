package org.cbioportal.service.impl;

import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalViolinPlotData;
import org.cbioportal.model.ClinicalViolinPlotRowData;
import org.cbioportal.model.Sample;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.ArgumentMatchers.anyList;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ViolinPlotServiceImplTest {
    private static final String[][] FGA_VALUES = new String[][]{
        new String[]{"0.1", "0.5", "0.2", "0.351", "0.44", "0.87", "0.75", "0.9",
        "0.2", "0.25"},

        new String[]{"0.62", "0.451", "0.224", "1.0", "0.95"},

        new String[]{"0.91", "0.3", "0.15", "0.42", "0.951", "0.724", "0.6", "0.45", "0.11",
        "0.1", "0.5", "0.2", "0.351"},

        new String[]{"0.44", "0.87"},

        new String[]{"0.75", "0.9", "0.2", "0.25", "0.62", "0.451", "0.224", "1.0", "0.95", "0.91"},

        new String[]{"0.3"},

        new String[]{"0.15", "0.42", "0.951", "0.724", "0.6", "0.45", "0.11"}
    };
    
    private static final String[] EXPECTED_ROW_CATEGORIES = new String[]{};
    
    @InjectMocks
    private ViolinPlotServiceImpl violinPlotService;

    @Test
    public void getClinicalViolinPlotData() throws Exception {
        List<ClinicalData> sampleClinicalData = new ArrayList<>();
        List<Sample> filteredSamples = new ArrayList<>();
        for (int j=0; j<FGA_VALUES.length; j++){
            for (int i=0; i<FGA_VALUES[j].length; i++) {
                ClinicalData num = new ClinicalData();
                num.setAttrId("FRACTION_GENOME_ALTERED");
                num.setAttrValue(FGA_VALUES[j][i]);
                num.setStudyId("test_study_id");
                num.setSampleId("test_sample_id_"+i+"_"+j);
                num.setUniqueSampleKey("test_sample_id_"+i+"_"+j);
                sampleClinicalData.add(num);

                ClinicalData cat = new ClinicalData();
                cat.setAttrId("CANCER_TYPE_DETAILED");
                cat.setAttrValue("cancer_type_"+j);
                cat.setStudyId("test_study_id");
                cat.setSampleId("test_sample_id_"+i+"_"+j);
                cat.setUniqueSampleKey("test_sample_id_"+i+"_"+j);
                sampleClinicalData.add(cat);
                
                Sample s = new Sample();
                s.setUniqueSampleKey("test_sample_id_"+i+"_"+j);
                filteredSamples.add(s);
            }
        }

        final int NUM_CURVE_POINTS = 100;
        ClinicalViolinPlotData result = violinPlotService.getClinicalViolinPlotData(
            sampleClinicalData,
            filteredSamples,
            new BigDecimal(0),
            new BigDecimal(1),
            new BigDecimal(NUM_CURVE_POINTS),
            false,
            new BigDecimal(1),
            new StudyViewFilter()
        );
        
        
        // sort the rows so that they're in the same order as FGA_VALUES so that it's easy to access them in order for the test 
        result.getRows().sort((a,b)->{
            return a.getCategory().compareTo(b.getCategory());
        });
        
        Assert.assertEquals(FGA_VALUES.length, result.getRows().size());
        Assert.assertEquals(NUM_CURVE_POINTS, result.getRows().get(0).getCurveData().size()); // curve data of the right count when there's enough points
        Assert.assertEquals(0, result.getRows().get(1).getCurveData().size()); // no curve data when there's too few points
        Assert.assertEquals(FGA_VALUES[1].length, result.getRows().get(1).getIndividualPoints().size()); // all data as individual points when theres few
        Assert.assertEquals(NUM_CURVE_POINTS, result.getRows().get(2).getCurveData().size()); // curve data of the right count when there's enough points
        Assert.assertEquals(0, result.getRows().get(3).getCurveData().size()); // no curve data when there's too few points
        Assert.assertEquals(FGA_VALUES[3].length, result.getRows().get(3).getIndividualPoints().size()); // all data as individual points when theres few
        Assert.assertEquals(NUM_CURVE_POINTS, result.getRows().get(4).getCurveData().size()); // curve data of the right count when there's enough points
        Assert.assertEquals(0, result.getRows().get(5).getCurveData().size()); // no curve data when there's too few points
        Assert.assertEquals(FGA_VALUES[5].length, result.getRows().get(5).getIndividualPoints().size()); // all data as individual points when theres few
        Assert.assertEquals(0, result.getRows().get(6).getCurveData().size()); // no curve data when there's too few points
        Assert.assertEquals(FGA_VALUES[6].length, result.getRows().get(6).getIndividualPoints().size()); // all data as individual points when theres few
        Assert.assertEquals(
            "ClinicalViolinPlotData{rows=[ClinicalViolinPlotRowData{category='cancer_type_0', numSamples=10, curveData=[2.0603650520008255E-20, 2.490647719181926E-16, 1.1076076652208455E-12, 1.8120288948454348E-9, 1.090560598843556E-6, 2.4145711384344356E-4, 0.019666891667884497, 0.5893007095578116, 6.4959656626929885, 26.342439739976722, 39.29830220022416, 21.567365526222503, 4.354376002067154, 0.323415093753096, 0.008840819153424564, 8.731643281319479E-4, 0.057805819777250377, 1.5672594138804623, 15.632131348621245, 57.35893005962437, 77.42695132836181, 38.48374585923057, 7.92423207121134, 9.01350402771472, 29.824276510741445, 38.28024971198188, 18.082259808682327, 3.142225532171844, 0.20087576275102062, 0.004726772934205448, 5.411635860928458E-4, 0.03503823201531945, 0.9027413660709811, 8.556410917840077, 29.83495870707217, 38.27055558846335, 18.05966172025932, 3.1351619223612133, 0.20023152249979403, 0.005909899633813545, 0.0699456171757767, 1.490902145686011, 11.697572211073982, 33.763533100833605, 35.85136402095853, 14.006118947628552, 2.098902804893868, 1.84169440953853, 12.824311118235457, 34.85448215358368, 34.85446738862994, 12.822241970960828, 1.7353017488632785, 0.08639558680953024, 0.001582390369583597, 1.0662062442117748E-5, 2.642861048615092E-8, 2.4099773276986683E-11, 8.08457327535297E-15, 9.977156040356584E-19, 4.529621834615629E-23, 7.565238860446793E-28, 1.0718685085686528E-31, 1.299713343010475E-26, 6.060567016595081E-22, 1.0396435331670962E-17, 6.560866081409529E-14, 1.5231525710557055E-10, 1.300861799974408E-7, 4.087186278743814E-5, 0.004724145017579533, 0.20087575767268165, 3.1422255317310923, 18.082259453513366, 38.280143563482106, 29.812605783325406, 8.541454563824558, 0.9002626876575123, 0.03490697255455146, 4.979215825253348E-4, 2.886267512257377E-6, 7.619593216682073E-5, 0.0078106380228383536, 0.2945614709023054, 4.086762339314846, 20.866706525926265, 39.486371749494865, 31.405574658411524, 28.441263369046265, 39.94087916293252, 26.364537728689402, 6.496245229664247, 0.5893020106991916, 0.019666893895645545, 2.414571152466279E-4, 1.0905605991686674E-6, 1.812028894873151E-9, 1.1076076652216555E-12, 2.4906477191819726E-16, 2.0603650520008884E-20], boxData=ClinicalViolinPlotBoxData{whiskerLower=0.1, whiskerUpper=0.9, median=0.39549999999999996, q1=0.2, q3=0.78}, individualPoints=[]}, ClinicalViolinPlotRowData{category='cancer_type_1', numSamples=5, curveData=[], boxData=ClinicalViolinPlotBoxData{whiskerLower=0.224, whiskerUpper=1.0, median=0.62, q1=0.3375, q3=0.975}, individualPoints=[ClinicalViolinPlotIndividualPoint{sampleId='test_sample_id_2_1', studyId='test_study_id', value=0.224}, ClinicalViolinPlotIndividualPoint{sampleId='test_sample_id_3_1', studyId='test_study_id', value=1.0}, ClinicalViolinPlotIndividualPoint{sampleId='test_sample_id_4_1', studyId='test_study_id', value=0.95}, ClinicalViolinPlotIndividualPoint{sampleId='test_sample_id_0_1', studyId='test_study_id', value=0.62}, ClinicalViolinPlotIndividualPoint{sampleId='test_sample_id_1_1', studyId='test_study_id', value=0.451}]}, ClinicalViolinPlotRowData{category='cancer_type_2', numSamples=13, curveData=[2.0604349714417075E-20, 2.4908751857701826E-16, 1.1078798994931478E-12, 1.8132274927165033E-9, 1.0925019751422748E-6, 2.4261389568468555E-4, 0.019920462163832454, 0.6097487405937532, 7.102575977934906, 32.96267917619901, 65.87787308350975, 60.848297696187764, 26.365137919687257, 11.721512020481352, 27.842869938204167, 39.062416249449676, 20.41667808368713, 4.699084133526758, 8.092697255277999, 28.686653690486928, 38.71329545117342, 19.224419685158946, 3.511984692095103, 0.23602473900518278, 0.005838842523441374, 6.836869426203608E-4, 0.0420534243691329, 1.031672546850052, 9.310858665390146, 30.913142031226013, 37.75789402231517, 17.00052871466201, 3.7071181023371595, 8.726945242806329, 29.83877369156282, 38.27058700048275, 18.059668660116213, 3.136262373779872, 0.26531165553642155, 1.4209179865630728, 11.337277869403325, 33.45324934951163, 37.69048995607363, 26.291739969127615, 36.06418308407382, 35.807699035046355, 13.892282505285074, 3.699216335820821, 12.92503186350542, 34.856446537583444, 34.85448136906267, 12.822242007455431, 1.735301748949051, 0.08639563718213138, 0.001600778251844697, 0.0024799667201574832, 0.12198972410744431, 2.2170584992135836, 14.823019097938793, 36.45874389002913, 32.98923568628809, 10.981162633265924, 1.3447139541348578, 0.06057829108126272, 0.0010039443608221134, 6.1290835423397E-6, 4.0013852695867196E-6, 7.057096490584219E-4, 0.04594516388423852, 1.100421531585217, 9.695804992159552, 31.427814626145288, 37.47572226043353, 16.43960424960614, 2.653005947433887, 0.15750364422145563, 0.0034399213092719486, 2.76383382224506E-5, 8.169225128672376E-8, 8.88291118889242E-11, 3.5533282719167566E-14, 5.031913846093932E-17, 2.424772067091923E-13, 4.796959342481572E-10, 3.4911352431404435E-7, 9.347012578266495E-5, 0.009206286776276391, 0.33358129436224565, 4.446562704058479, 21.804895004152677, 39.342873190143955, 26.38262037695624, 10.297268206509107, 20.98356751811011, 39.078206388966755, 27.497337388621183, 7.121233546216458, 0.6784671743735528, 0.02377979084850563, 3.066145357775355E-4], boxData=ClinicalViolinPlotBoxData{whiskerLower=0.1, whiskerUpper=0.951, median=0.42, q1=0.175, q3=0.6619999999999999}, individualPoints=[]}, ClinicalViolinPlotRowData{category='cancer_type_3', numSamples=2, curveData=[], boxData=ClinicalViolinPlotBoxData{whiskerLower=0.44, whiskerUpper=0.87, median=0.655, q1=0.44, q3=0.87}, individualPoints=[ClinicalViolinPlotIndividualPoint{sampleId='test_sample_id_0_3', studyId='test_study_id', value=0.44}, ClinicalViolinPlotIndividualPoint{sampleId='test_sample_id_1_3', studyId='test_study_id', value=0.87}]}, ClinicalViolinPlotRowData{category='cancer_type_4', numSamples=10, curveData=[2.925101346412698E-84, 7.047326918847208E-76, 6.2461647820480575E-68, 2.0366104799983836E-60, 2.4429131610500767E-53, 1.0779871957744748E-46, 1.7499461861510412E-40, 1.0450601867428694E-34, 2.2959553094837114E-29, 1.8556286783192092E-24, 5.517270022518955E-20, 6.034800548564917E-16, 2.428326734129904E-12, 3.5946519073313903E-9, 1.957546564696557E-6, 3.921688697608263E-4, 0.02890295167045124, 0.7836898183811377, 7.822518053870328, 28.93425860883489, 42.41505870517847, 39.039761397420484, 43.30054281995553, 36.90344839886546, 37.3018866669242, 39.012685837114354, 18.108635475219785, 3.1425749272248065, 0.20087746035344534, 0.00472414807007391, 4.087186480061479E-5, 1.300861804858761E-7, 1.5231525714916123E-10, 6.560866081552088E-14, 1.0396444702718048E-17, 2.401813560688031E-19, 2.253213032113778E-15, 7.795931015163072E-12, 9.922914255174207E-9, 4.646393721258539E-6, 8.003838098203277E-4, 0.05072080066123851, 1.1824408475626524, 10.140941225977224, 31.995037636972278, 37.13577597396173, 15.856520709161023, 2.4907423957196735, 0.14393129906586433, 0.003059758820145182, 2.3929006278898765E-5, 6.884424635652973E-8, 7.286453085144085E-11, 5.0629959529312694E-14, 5.885062782719107E-11, 5.723966796249049E-8, 2.0480882189325674E-5, 0.0026959121195483347, 0.13054742829045168, 2.325607550461973, 15.240880389500784, 36.74423405658725, 32.58921211630563, 10.633201436234723, 1.2763204846816174, 0.05635869859959091, 9.155196933961355E-4, 5.471318963429264E-6, 1.4211429893991378E-7, 4.0871872515380816E-5, 0.004724145017582428, 0.20087575767268165, 3.1422255317310923, 18.082259453513366, 38.280143563482106, 29.812605783325406, 8.541454563824558, 0.9002626876575122, 0.03490697255437616, 4.979212215706467E-4, 2.6128507715759102E-6, 5.044212246476146E-9, 4.5066541275640675E-10, 3.289502369812991E-7, 8.917622163190966E-5, 0.008930374186319114, 0.3326213733402558, 4.687957296745932, 26.013928432023253, 61.103242586623175, 65.68892703656729, 32.97824827968543, 11.793099318468258, 23.350427210600866, 39.46524983902706, 25.165326772772932, 6.338629923434066, 5.854227137880277, 23.971264111864187, 39.49547451323124], boxData=ClinicalViolinPlotBoxData{whiskerLower=0.2, whiskerUpper=1.0, median=0.685, q1=0.2435, q3=0.92}, individualPoints=[]}, ClinicalViolinPlotRowData{category='cancer_type_5', numSamples=1, curveData=[], boxData=ClinicalViolinPlotBoxData{whiskerLower=0.3, whiskerUpper=0.3, median=0.3, q1=0.3, q3=0.3}, individualPoints=[ClinicalViolinPlotIndividualPoint{sampleId='test_sample_id_0_5', studyId='test_study_id', value=0.3}]}, ClinicalViolinPlotRowData{category='cancer_type_6', numSamples=7, curveData=[], boxData=ClinicalViolinPlotBoxData{whiskerLower=0.11, whiskerUpper=0.951, median=0.45, q1=0.15, q3=0.724}, individualPoints=[ClinicalViolinPlotIndividualPoint{sampleId='test_sample_id_1_6', studyId='test_study_id', value=0.42}, ClinicalViolinPlotIndividualPoint{sampleId='test_sample_id_2_6', studyId='test_study_id', value=0.951}, ClinicalViolinPlotIndividualPoint{sampleId='test_sample_id_3_6', studyId='test_study_id', value=0.724}, ClinicalViolinPlotIndividualPoint{sampleId='test_sample_id_4_6', studyId='test_study_id', value=0.6}, ClinicalViolinPlotIndividualPoint{sampleId='test_sample_id_0_6', studyId='test_study_id', value=0.15}, ClinicalViolinPlotIndividualPoint{sampleId='test_sample_id_5_6', studyId='test_study_id', value=0.45}, ClinicalViolinPlotIndividualPoint{sampleId='test_sample_id_6_6', studyId='test_study_id', value=0.11}]}], axisStart=0.0, axisEnd=1.0}", 
            result.toString()
        );
    }
}
