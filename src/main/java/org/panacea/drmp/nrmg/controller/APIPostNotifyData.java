package org.panacea.drmp.nrmg.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.panacea.drmp.nrmg.domain.notifications.DataNotification;
import org.panacea.drmp.nrmg.service.OrchestratorNotificationHandlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping(path = "/nrmg", produces = "application/json")
public class APIPostNotifyData {

    @Autowired
    OrchestratorNotificationHandlerService orchestratorNotificationHandler;

    @Operation(description = "Post a Data Notification")
    @PostMapping(value = "/notify/data")
    public DataNotificationResponse postNotifyData(@RequestBody DataNotification request) {
        return orchestratorNotificationHandler.perform(request);
    }

    // Response object
    @Value
    @Schema(description = "Contains the parameters for the DataNotificationResponse to be returned to the orchestrator")
    public static class DataNotificationResponse {
        @Schema(description = "Emulation environment name")
        private String environment;
        @Schema(description = "Data collection snapshot id")
        private String snapshotId;
        @Schema(description = "Data collection snapshot timestamp")
        private String snapshotTime;
    }
}
