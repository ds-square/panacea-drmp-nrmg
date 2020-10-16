package org.panacea.drmp.nrmg.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.panacea.drmp.nrmg.NRMGenerator;
import org.panacea.drmp.nrmg.controller.APIPostNotifyData;
import org.panacea.drmp.nrmg.domain.notifications.DataNotification;
import org.panacea.drmp.nrmg.exception.NRMGException;
import org.panacea.drmp.nrmg.service.OrchestratorNotificationHandlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrchestratorNotificationHandlerServiceImpl implements OrchestratorNotificationHandlerService {

    public static final String INVALID_NOTIFICATION_ERR_MSG = "Invalid Data Notification Body.";

    @Autowired
    NRMGenerator nrmGenerator;

    @Override
    public APIPostNotifyData.DataNotificationResponse perform(DataNotification notification) throws NRMGException {
        log.info("Received Data Notification from Orchestrator: {}", notification);
        try {
            if (notification.getEnvironment() == null) {
                throw new NRMGException("No environment defined for notification.");
            }
            nrmGenerator.generateNRMG(notification);

            return new APIPostNotifyData.DataNotificationResponse(notification.getEnvironment(), notification.getSnapshotId(), notification.getSnapshotTime());
        } catch (NRMGException e) {
            log.info("NRMGException occurred: ", e);
            throw new NRMGException(INVALID_NOTIFICATION_ERR_MSG, e);
        }
    }
}
