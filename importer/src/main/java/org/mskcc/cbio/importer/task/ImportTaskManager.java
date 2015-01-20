package org.mskcc.cbio.importer.task;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.cvr.dmp.importer.MockConfig;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

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
 * Created by criscuof on 12/7/14.
 */
public class ImportTaskManager {

    private final ServiceManager manager;

    private static final Logger logger = Logger.getLogger(ImportTaskManager.class);

    public ImportTaskManager(List<Service> serviceList) {
        Preconditions.checkArgument(null != serviceList,"A list of import tasks is required");
        Preconditions.checkArgument(!serviceList.isEmpty(),"The list of import tasks is empty");
        this.manager = new ServiceManager(serviceList);
    }

    private void startImportTasks() {
        this.manager.startAsync();
        logger.info("Import tasks started");
    }

    private void stopTasks() {
        this.manager.stopAsync();
        logger.info("Import tasks stopped");
    }

    private boolean isHealthy() {
        return this.manager.isHealthy();
    }

    private String displayServiceStatus() {
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<Service.State,Service> entry : this.manager.servicesByState().entries()) {
            sb.append("\nService: " +entry.getValue().getClass().getName() +" state: "
                + entry.getKey().toString());
        }
        return sb.toString();
    }

    /*
    main method for application invocation
    method should loop on a continuous basis
     */
    public static void main(String...args){
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/applicationContext-importer.xml");

        ImportTaskManager taskManager = (ImportTaskManager) applicationContext.getBean("importTaskManager");
        taskManager.startImportTasks();
        // run infinite loop
        while (true){
            try {
                Thread.sleep(60000L);
                if(taskManager.isHealthy()){
                    logger.info("All import tasks are running");
                } else {
                    logger.info(taskManager.displayServiceStatus());
                }
            } catch (InterruptedException e){
                logger.error(e.getMessage());
                taskManager.stopTasks();
                return;
            }
        }
    }

}
