package com.soap.stocks.exception;

import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;

@SoapFault(faultCode = FaultCode.CLIENT)
public class InvalidSymbolsException extends RuntimeException{

    public InvalidSymbolsException(String givenStockSymbols){

        super(String.format("Symbols are not valid. Given stock symbols are '%s'",givenStockSymbols));

    }
}
