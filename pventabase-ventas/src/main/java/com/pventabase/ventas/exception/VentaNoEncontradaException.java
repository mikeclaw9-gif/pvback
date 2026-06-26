package com.pventabase.ventas.exception;

import com.pventabase.common.exception.BusinessException;
import com.pventabase.common.constants.ErrorCodes;

public class VentaNoEncontradaException extends BusinessException {

    public VentaNoEncontradaException(Long ventaId) {
        super(ErrorCodes.RESOURCE_NOT_FOUND,
                "Venta no encontrada con ID: " + ventaId);
    }

    public VentaNoEncontradaException(String mensaje) {
        super(ErrorCodes.RESOURCE_NOT_FOUND, mensaje);
    }
}
