package com.pventabase.common.exception;

import com.pventabase.common.constants.ErrorCodes;

public class DuplicateResourceException extends BusinessException {

    public DuplicateResourceException(String resource, String field, Object value) {
        super(ErrorCodes.DUPLICATE_RESOURCE,
                resource + " ya existe con " + field + ": " + value);
    }
}
