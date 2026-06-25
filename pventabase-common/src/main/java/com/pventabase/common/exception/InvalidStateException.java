package com.pventabase.common.exception;

import com.pventabase.common.constants.ErrorCodes;

public class InvalidStateException extends BusinessException {

    public InvalidStateException(String message) {
        super(ErrorCodes.INVALID_STATE, message);
    }
}
