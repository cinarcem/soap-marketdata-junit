package com.soap.stocks.exception;

import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;

@SoapFault(faultCode = FaultCode.SERVER)
public class NoMarketDataFoundException extends RuntimeException{

    public NoMarketDataFoundException(){
        super("No market data found. Check if stock symbols fetched properly.");
    }
}
