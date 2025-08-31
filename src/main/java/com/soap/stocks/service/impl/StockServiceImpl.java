package com.soap.stocks.service.impl;

import com.soap.stocks.exception.InvalidSymbolsException;
import com.soap.stocks.exception.NoMarketDataFoundException;
import com.soap.stocks.service.StockService;
import https.soap_stocks_up_railway.Stock;
import jakarta.annotation.PostConstruct;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StockServiceImpl implements StockService {

    @Autowired
    private Environment env;

    private Document document;

    @Value("${STOCK_DATA_URL}")
    private String stockDataUrl;

    private Map<String, Stock> stocksMarketData = new ConcurrentHashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(StockServiceImpl.class);

    @Override
    public Map<String, Stock>  getStocksMarketData(List<String> stockSymbols) {
        Map<String, Stock>  response = new LinkedHashMap<>();

        if(stocksMarketData.keySet().isEmpty()){
            throw new NoMarketDataFoundException();
        }

        for (String symbol : stockSymbols){
            if (stocksMarketData.containsKey(symbol)) {
                response.put(symbol,stocksMarketData.get(symbol));
            }
        }

        if(response.isEmpty()){
            String givenStockSymbols = String.join(",", stockSymbols);
            throw new InvalidSymbolsException(givenStockSymbols);
        }

        return response;
    }

    @PostConstruct
    @Scheduled(cron = "0 */3 8-21 * * *", zone = "Europe/Istanbul")
    private void updateStockData() {

        if (stockDataUrl == null || stockDataUrl.trim().isEmpty()) {
            logger.error("STOCK_DATA_URL for stock market data is null or empty. " +
                    "Failed to fetch stock data from {}", stockDataUrl
            );

            return;
        }

        try {
            logger.info("Fetching stock data from {}", stockDataUrl);
            document = Jsoup.connect(Objects.requireNonNull(stockDataUrl)).get();
            logger.info("Successfully fetched stock data from {} ", stockDataUrl);
            updateStocksMarketData();
        } catch (Exception e) {
            logger.error("Failed to fetch stock data from {}. Error: {}", stockDataUrl, e.getMessage(), e);
        }
    }

    private void  updateStocksMarketData(){
        Map<String, Stock> updatedStocksMarketData = new HashMap<>();

        if (document == null){
            logger.error("Document is null. Failed to update stocksMarketData. " +
                            "Check if url fetched properly or not from {}"
                    , stockDataUrl
            );
            return;
        }

        Elements rows = document.select("table.dataTable tbody tr");
        if (rows.isEmpty()) {
            logger.warn("No stock data rows found in the table for URL: {}", stockDataUrl);
            return;
        }

        for(Element row : rows){
            try{
                String stockCode = row.selectFirst("td a").text().trim();
                BigDecimal latestPrice = parseBigDecimal(row.select("td.text-right").get(0).text());
                BigDecimal dailyChangePct = parseBigDecimal(row.select("td.text-right").get(1).text());
                BigDecimal dailyChangeInTL = parseBigDecimal(row.select("td.text-right").get(2).text());
                BigDecimal tradingVolumeTL = parseBigDecimal(row.select("td.text-right").get(3).text());
                BigDecimal tradeVolumeCount = parseBigDecimal(row.select("td.text-right").get(4).text());

                Stock stock = new Stock();
                stock.setStockSymbol(stockCode);
                stock.setLatestPrice(latestPrice);
                stock.setDailyChangePct(dailyChangePct);
                stock.setDailyChangeInTL(dailyChangeInTL);
                stock.setTradingVolumeTL(tradingVolumeTL);
                stock.setTradeVolumeCount(tradeVolumeCount);

                updatedStocksMarketData.put(stockCode,stock);
            }catch (Exception e){
                logger.error("Error processing row: {}", row.toString(), e);
            }
        }
        // Corrupted market data can be retrieved just before opening market.
        // This section check if it is corrupted.
        long possibleCorruptedDataCount = updatedStocksMarketData
                .values()
                .stream()
                .filter(
                        stock -> stock.getDailyChangePct().compareTo(new BigDecimal("-100.00"))  == 0
                )
                .count();
        Boolean isMarketDataInvalid = (((double) possibleCorruptedDataCount / updatedStocksMarketData.keySet().size()) * 100) > 20;

        if (!isMarketDataInvalid) {
            stocksMarketData = updatedStocksMarketData;
        } else {
            logger.warn("Market data source url has invalid stock market data. This update will be skipped.");
        }

    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Value is empty or null for BigDecimal parsing");
        }
        try {
            return new BigDecimal(value.trim().replace(".", "").replace(",", "."));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Failed to parse BigDecimal from value: " + value, e);
        }
    }
}
