package e2e.controller;

import com.soap.stocks.StocksApplication;
import com.soap.stocks.controller.StockController;
import com.soap.stocks.exception.InvalidSymbolsException;
import https.soap_stocks_up_railway.GetStockRequest;
import https.soap_stocks_up_railway.GetStockResponse;
import https.soap_stocks_up_railway.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest(classes = StocksApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class UserApiE2ETest {

    @Autowired
    private StockController stockController;

    private static final String VALID_STOCK_SYMBOL = "SASA";
    private static final String INVALID_STOCK_SYMBOL = "INVALID";
    private GetStockRequest request;
    private GetStockResponse response;

    @BeforeEach
    void setUp() {
        request = new GetStockRequest();
    }

    @Test
    @DisplayName("getStockResponse - Should response stocks market data when a valid stock symbol is given")
    void shouldResponseStockMarketDataWhenValidStockSymbolIsGive(){
        // Given
        List<String> stockSymbols = new ArrayList<>();
        stockSymbols.add(VALID_STOCK_SYMBOL);
        request.getStockSymbol().addAll(stockSymbols);
        // When
        response = stockController.getStockResponse(request);
        //Then
        assertEquals(response.getStock().size(), stockSymbols.size(), "Given stock symbol count and response size should equal");
        assertEquals(response.getStock().get(0).getStockSymbol(), VALID_STOCK_SYMBOL, "Should return given stock symbol");
    }

    @Test
    @DisplayName("Should Return Invalid Symbols When a Valid and Invalid Stock Symbol Is Given")
    void shouldReturnInvalidSymbolsWhenValidAndInvalidStockSymbolIsGiven(){
        // Given
        List<String> stockSymbols = new ArrayList<>();
        stockSymbols.add(VALID_STOCK_SYMBOL);
        stockSymbols.add(INVALID_STOCK_SYMBOL);
        request.getStockSymbol().addAll(stockSymbols);
        // When
        response = stockController.getStockResponse(request);
        //Then
        assertTrue(response.getInvalidSymbols().contains(INVALID_STOCK_SYMBOL), "Invalid symbol should be written in response");
        assertEquals(response.getStock().get(0).getStockSymbol(), VALID_STOCK_SYMBOL, "Should return given valid stock symbol");
    }

    @Test
    @DisplayName("Should throw InvalidSymbolsException when a invalid stock symbol given")
    void shouldThrowInvalidSymbolsExceptionWhenInvalidSymbolIsGiven(){
        // Given
        List<String> stockSymbols = new ArrayList<>();
        stockSymbols.add(INVALID_STOCK_SYMBOL);
        request.getStockSymbol().addAll(stockSymbols);
        // When & Then
        assertThrows(InvalidSymbolsException.class, ()->{
            stockController.getStockResponse(request);
        }, "Was excepting a InvalidSymbolsException to be thrown");

    }
}
