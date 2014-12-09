package org.mskcc.cbio.importer.cvr.darwin.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.cvr.darwin.transformer.DarwinTransformer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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
 * Created by criscuof on 12/8/14.
 */
public class DarwinImporterService {
    private static final Logger logger = Logger.getLogger(DarwinImporterService.class);
    private List<Class> transformerClassList;
    // the List of transformers is populated by Spring IOC
    private List<DarwinTransformer> transformerList = Lists.newArrayList();

    public DarwinImporterService(){
    }

    public void setTransformerClassList(List<Class> aList) {
        this.transformerClassList = aList;
    }


   private void registerStagingFile(Path stagingFilePath){
        Class[] cArg = new Class[1];
        cArg[0] = java.nio.file.Path.class;
        for (Class aClass : this.transformerClassList){
            try {
                this.transformerList.add((DarwinTransformer) aClass.getDeclaredConstructor(cArg).newInstance(stagingFilePath));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException  e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /*
    public method to invoke all registered DarwinTransformers
     */
    public void transformDarwinData(Path aPath) {
        Preconditions.checkArgument(null != aPath,"A staging file Path is required");
        this.registerStagingFile(aPath);
        for (DarwinTransformer transformer : this.transformerList){
            logger.info("Invoking transformer: " +transformer.getClass().getName());
            transformer.transform();
        }
    }

    /*
    main method for standalone testing
     */
    public static void main (String...args){
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/applicationContext-importer.xml");
        DarwinImporterService service = (DarwinImporterService) applicationContext.getBean("darwinImporterService");
        service.transformDarwinData(Paths.get("/tmp/class"));
        logger.info("FINIS...");

    }


}