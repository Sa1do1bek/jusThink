package com.example.backend.services.auditLogging;

import com.example.backend.enums.Action;
import com.example.backend.enums.ResourceType;
import com.example.backend.models.UserModel;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

public interface IAuditLoggingService {

    void createAuditLog(
            UserModel actor, Action action, ResourceType resourceType,
            UUID resourceId, HttpServletRequest request, boolean success,
            String metaData
    );
}
