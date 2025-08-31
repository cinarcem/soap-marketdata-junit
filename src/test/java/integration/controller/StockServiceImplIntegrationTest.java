package integration.controller;

import com.soap.stocks.StocksApplication;
import com.soap.stocks.exception.InvalidSymbolsException;
import com.soap.stocks.service.impl.StockServiceImpl;
import https.soap_stocks_up_railway.Stock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootTest(classes = StocksApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class StockServiceImplIntegrationTest {

    @Autowired
    private StockServiceImpl stockService;

    private static final String VALID_STOCK_SYMBOL = "SASA";
    private static final String INVALID_STOCK_SYMBOL = "INVALID";

    @Test
    @DisplayName("Should get stocks market data when a valid stock symbol given")
    void shouldGetStockMarketDataWhenValidSymbolIsGiven(){
        // Given
        List<String> stockSymbols = new ArrayList<>();
        stockSymbols.add(VALID_STOCK_SYMBOL);

        // When
        Map<String, Stock> serviceResponse = stockService.getStocksMarketData(stockSymbols);

        // Then
        assertEquals(serviceResponse.get(VALID_STOCK_SYMBOL).getStockSymbol(), VALID_STOCK_SYMBOL, "Service should response stock market data");

    }

    @Test
    @DisplayName("Should throw InvalidSymbolsException when a invalid stock symbol given")
    void shouldThrowInvalidSymbolsExceptionWhenInvalidSymbolIsGiven(){
        // Given
        List<String> stockSymbols = new ArrayList<>();
        stockSymbols.add(INVALID_STOCK_SYMBOL);

        // When & Then
        assertThrows(InvalidSymbolsException.class, ()->{
            stockService.getStocksMarketData(stockSymbols);
        }, "Was excepting a InvalidSymbolsException to be thrown");

    }
}
