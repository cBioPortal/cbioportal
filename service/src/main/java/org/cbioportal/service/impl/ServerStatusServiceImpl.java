package org.cbioportal.service.impl;

import java.io.Serializable;
import java.util.List;

import org.cbioportal.model.TypeOfCancer;
import org.cbioportal.persistence.CancerTypeRepository;
import org.cbioportal.service.ServerStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ServerStatusServiceImpl implements  ServerStatusService {

    public static final String MESSAGE_RUNNING = "UP";
    public static final String MESSAGE_DOWN = "DOWN";
    
    private static final ServerStatusMessage objRunning = new ServerStatusMessage(MESSAGE_RUNNING);
    private static final ServerStatusMessage objDown = new ServerStatusMessage(MESSAGE_DOWN);

    @Autowired
    private CancerTypeRepository cancerTypeRepository;

    @Override
    public ServerStatusMessage getServerStatus() {
        List<TypeOfCancer> allCancerTypes = cancerTypeRepository.getAllCancerTypes("SUMMARY", null, null, null, null);
        if (allCancerTypes.size() > 0) {
            return objRunning;
        }
        return objDown;
    }

    public final static class ServerStatusMessage implements Serializable {
        
        private static final long serialVersionUID = 1L;
        String status;
        
        ServerStatusMessage(String message) {
            this.status = message;
        }

        public String getStatus() {
            return this.status;
        }

    }

}
