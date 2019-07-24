package org.cbioportal.persistence.spark.util;

import org.cbioportal.persistence.GeneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class SpringApplicationContext {
    
    private static GeneRepository geneRepository;
    private static ApplicationContext ctx;

    @Autowired
    public void setGeneRepository(GeneRepository geneRepository) {
        SpringApplicationContext.geneRepository = geneRepository;
    }
    
    public static GeneRepository getGeneRepository() { return geneRepository; }

    public static ApplicationContext getApplicationContext() {
        return ctx;
    }

    public static void setApplicationContext(ApplicationContext ctx) {
        SpringApplicationContext.ctx = ctx;
    }

    public static void init()
    {
        if (SpringApplicationContext.ctx == null) {
            ctx = new ClassPathXmlApplicationContext("classpath:applicationContext-spark.xml");
        }
    }

}
