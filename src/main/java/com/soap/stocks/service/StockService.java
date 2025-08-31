package com.soap.stocks.service;

import https.soap_stocks_up_railway.Stock;

import java.util.List;
import java.util.Map;

public interface StockService {

    Map<String, Stock> getStocksMarketData(List<String> stockSymbols);

}
