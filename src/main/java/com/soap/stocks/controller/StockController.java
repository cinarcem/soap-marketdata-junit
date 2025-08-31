package com.soap.stocks.controller;

import com.soap.stocks.service.StockService;
import https.soap_stocks_up_railway.GetStockRequest;
import https.soap_stocks_up_railway.GetStockResponse;
import https.soap_stocks_up_railway.Stock;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Endpoint
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @PayloadRoot(namespace = "https://soap-stocks.up.railway.app", localPart = "GetStockRequest")
    @ResponsePayload
    public GetStockResponse getStockResponse(@RequestPayload GetStockRequest request){

        GetStockResponse response = new GetStockResponse();
        List<String> givenStockSymbols = request.getStockSymbol();

        Map<String, Stock> serviceResponse = stockService.getStocksMarketData(givenStockSymbols);

        List<Stock> stocksMarketData = new ArrayList<>(serviceResponse.values());

        int differenceCount = givenStockSymbols.size() - stocksMarketData.size();
        boolean isPartialResponse = differenceCount > 0;

        if(isPartialResponse) {
            List<String> missingStockSymbols = givenStockSymbols.stream()
                    .filter(key -> !serviceResponse.containsKey(key))
                    .toList();
            String missingSymbolsText = String.join(",", missingStockSymbols);
            response.setInvalidSymbols(missingSymbolsText);
        }

        for( Stock s : stocksMarketData){
            Stock stock = new Stock();
            stock.setStockSymbol(s.getStockSymbol());
            stock.setLatestPrice(s.getLatestPrice());
            stock.setDailyChangePct(s.getDailyChangePct());
            stock.setDailyChangeInTL(s.getDailyChangeInTL());
            stock.setTradingVolumeTL(s.getTradingVolumeTL());
            stock.setTradeVolumeCount(s.getTradeVolumeCount());

            response.getStock().add(stock);
        }

        response.setIsPartialResponse(isPartialResponse);
        return response;
    }

}
