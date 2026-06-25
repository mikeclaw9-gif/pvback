package com.pventabase.common.exception;

import com.pventabase.common.constants.ErrorCodes;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resource, Object id) {
        super(ErrorCodes.RESOURCE_NOT_FOUND,
                resource + " no encontrado con id: " + id);
    }

    public ResourceNotFoundException(String resource, String field, Object value) {
        super(ErrorCodes.RESOURCE_NOT_FOUND,
                resource + " no encontrado con " + field + ": " + value);
    }
}
