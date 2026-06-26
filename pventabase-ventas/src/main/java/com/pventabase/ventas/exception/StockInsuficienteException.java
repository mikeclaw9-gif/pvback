package com.pventabase.ventas.exception;

import com.pventabase.common.exception.BusinessException;
import com.pventabase.common.constants.ErrorCodes;

public class StockInsuficienteException extends BusinessException {

    public StockInsuficienteException(String productoNombre, int stockDisponible, int cantidadSolicitada) {
        super(ErrorCodes.INVALID_STATE,
                "Stock insuficiente para el producto: " + productoNombre +
                ". Stock disponible: " + stockDisponible +
                ", Cantidad solicitada: " + cantidadSolicitada);
    }
}
