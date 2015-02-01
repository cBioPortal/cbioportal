package org.mskcc.cbio.importer.util;

import com.google.common.base.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.config.internal.ImporterSpreadsheetService;
import org.mskcc.cbio.importer.model.OncotreePropertiesMetadata;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import rx.Observable;
import rx.Subscriber;
import rx.observables.StringObservable;
import scala.Tuple2;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
 * <p/>
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.
 * <p/>
 * Created by criscuof on 1/30/15.
 */
public  enum OncoTreeService {
    INSTANCE;

    private final static Logger logger = Logger.getLogger(OncoTreeService.class);
    private final Map<String,OncoTreeNode> oncoTreeNodeMap = Suppliers.memoize(new OncoTreeMapSupplier()).get();

    public Optional<OncoTreeNode> getNodeByKey(String aKey){
        Preconditions.checkArgument(!Strings.isNullOrEmpty(aKey),"An oncotree key is required");
        if(oncoTreeNodeMap.containsKey(aKey)){
            return Optional.of(this.oncoTreeNodeMap.get(aKey));
        }
        return Optional.absent();
    }

    private class OncoTreeMapSupplier implements Supplier<Map<String,OncoTreeNode>>{

        private Map<String,OncoTreeNode> oncoTreeNodeMap = Maps.newTreeMap();
        private final String newLine = System.getProperty("line.separator");
        private Splitter tabSplitter = Splitter.on("\t");
        private List<OncotreePropertiesMetadata> majorCancerTypeList =
            OncotreePropertiesMetadata.getOncotreePropertiesMetadataByAttributeValue("MAJOR_CANCER_TYPE");

        public OncoTreeMapSupplier(){

        }

        Function<String, Tuple2<String, String>> parseOncoTreeEntry =
                new Function<String, Tuple2<String, String>>(){
                    @Override
                    public Tuple2<String, String> apply(String input) {
                        String name = input.substring(0,input.indexOf('(')).trim();
                        String key =  input.substring(input.indexOf("(")+1, input.indexOf(')')).trim();
                        return new Tuple2<String,String>(key,name);

                    }
                };

        @Override
        public Map<String, OncoTreeNode> get() {

                Observable<StringObservable.Line>  lineObservable =
                        StringObservable.byLine(Observable.from
                                (this.formatOncoTreeWorksheet()));
                lineObservable.subscribe(new Subscriber<StringObservable.Line>() {
                    @Override
                    public void onCompleted() {

                            logger.info("OncoTreeMap completed");
                    }

                    @Override
                    public void onError(Throwable throwable) {
                            logger.error(throwable.getMessage());
                            throwable.printStackTrace();
                    }

                    @Override
                    public void onNext(StringObservable.Line line) {
                            List<String>fields = tabSplitter.splitToList(line.getText());
                            String parentKey = fields.get(0);
                            if (!oncoTreeNodeMap.containsKey(parentKey)){
                                OncoTreeNode parentNode = new OncoTreeNode(parentKey,parentKey);
                                parentNode.setMajorCancerType(parentKey);
                                oncoTreeNodeMap.put(parentKey, parentNode);
                            }
                            for(String field : fields){
                                if (field.contains("(")) { // ignore primary value
                                    Tuple2<String,String> keyNameTuple = parseOncoTreeEntry.apply(field);
                                    if(!oncoTreeNodeMap.containsKey(keyNameTuple._1())) {
                                        OncoTreeNode node = new OncoTreeNode(keyNameTuple._1(), keyNameTuple._2(),
                                                oncoTreeNodeMap.get(parentKey));
                                        //node.setMajorCancerType(oncoTreeNodeMap.get(parentKey).getMajorCancerType());
                                        oncoTreeNodeMap.put(keyNameTuple._1(),node);
                                        // add this node to the parent's list of  children
                                        oncoTreeNodeMap.get(parentKey).getChildrenNodeList().add(node);
                                       resolveMajorCancerType(node);
                                    }
                                    parentKey = keyNameTuple._1();
                                }
                            }

                        }
                });

            return this.oncoTreeNodeMap;
        }

        /*
        private method to format the oncotree worksheet as a list of TSV lines in original order
         */
        private List<String> formatOncoTreeWorksheet() {
            Table<Integer, String, String> table = ImporterSpreadsheetService.INSTANCE.getWorksheetTableByName("oncotree_src");
            List<String> lineList = Lists.newArrayList();
            for (Integer key : table.rowKeySet()) {
                Map<String, String> rowMap = table.row(key);
                lineList.add(StagingCommonNames.tabJoiner.join(
                        rowMap.get("primary"),rowMap.get("secondary"),
                        rowMap.get("tertiary"),rowMap.get("quaternary"),"\n"
                ));
            }
            return lineList;
        }

        private void resolveMajorCancerType(OncoTreeNode child){
            for(OncotreePropertiesMetadata md : majorCancerTypeList) {
                if (md.getNode().equals(child.getOncoTreeCode())) {
                    child.setMajorCancerType(child.getOncoTreeCode());
                    return;
                }
            }
            child.setMajorCancerType(child.getParentOncoTreeNode().getMajorCancerType());
        }
    }

    public static void main  (String...args){

        OncoTreeNode node01 = OncoTreeService.INSTANCE.getNodeByKey("BRCA").get();
        logger.info("Key " +node01.getOncoTreeCode() +" name: " +node01.getOncoTreeName() +" nChilderen = "
            +node01.getChildrenNodeList().size() +" major Cancer type = " +node01.getMajorCancerType());
        // find the children
        for (OncoTreeNode node : node01.getChildrenNodeList()){
            logger.info("Key " + node.getOncoTreeCode() + " name: " + node.getOncoTreeName() + " nChilderen = "
                    + node.getChildrenNodeList().size() + " major Cancer type = " + node.getMajorCancerType());
        }
        OncoTreeNode parent = node01.getParentOncoTreeNode();
        logger.info("Parent code " + parent.getOncoTreeCode());

    }
}
