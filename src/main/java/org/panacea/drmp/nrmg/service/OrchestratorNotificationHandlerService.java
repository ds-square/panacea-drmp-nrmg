package org.panacea.drmp.nrmg.service;

import org.panacea.drmp.nrmg.controller.APIPostNotifyData.DataNotificationResponse;
import org.panacea.drmp.nrmg.domain.notifications.DataNotification;
import org.panacea.drmp.nrmg.exception.NRMGException;

public interface OrchestratorNotificationHandlerService {
    DataNotificationResponse perform(DataNotification dataNotification) throws NRMGException;
}
