package unit.controller;

import com.soap.stocks.controller.StockController;
import com.soap.stocks.exception.InvalidSymbolsException;
import com.soap.stocks.service.impl.StockServiceImpl;
import https.soap_stocks_up_railway.GetStockRequest;
import https.soap_stocks_up_railway.GetStockResponse;
import https.soap_stocks_up_railway.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StockControllerTest {

    @InjectMocks
    private StockController stockController;

    @Mock
    private StockServiceImpl stockService;

    private GetStockRequest request;
    private GetStockResponse response;
    private Stock stock;
    private Map<String, Stock> mockStockData;
    private static final String VALID_STOCK_SYMBOL = "SASA";
    private static final String INVALID_STOCK_SYMBOL = "INVALID";
    private static final String MOCK_URL = "http://mock-url.com";

    private Stock createValidStock() {
        Stock stock = new Stock();
        stock.setStockSymbol(VALID_STOCK_SYMBOL);
        stock.setLatestPrice(new BigDecimal("3.18"));
        stock.setDailyChangePct(new BigDecimal("1.92"));
        stock.setDailyChangeInTL(new BigDecimal("0.06"));
        stock.setTradingVolumeTL(new BigDecimal("1320096217"));
        stock.setTradeVolumeCount(new BigDecimal("416662442"));
        return stock;
    }

    private Map<String, Stock> createStocksMarketDataWith(List<Stock> stocks) {
        Map<String, Stock> stocksMarketData = new ConcurrentHashMap<>();
        for( Stock stock : stocks){
            stocksMarketData.put(stock.getStockSymbol(), stock);
        }
        return stocksMarketData;
    }

    @BeforeEach
    void setUp() {
        request = new GetStockRequest();
        response = new GetStockResponse();
        stock = createValidStock();

        // Initialize mock stock data
        mockStockData = new HashMap<>();
        //mockStockData.put(stock.getStockSymbol(), stock);

        // Initialize service data
        ReflectionTestUtils.setField(stockService, "stockDataUrl", MOCK_URL);
        Map<String, Stock> stocksMarketData = createStocksMarketDataWith(List.of(stock));
        ReflectionTestUtils.setField(stockService, "stocksMarketData", stocksMarketData);

    }

    @Test
    @DisplayName("Should Return Market Data When Valid Stock Symbol Is Given")
    void shouldReturnStockMarketDataWhenValidStockSymbolIsGiven(){
        // Given
        request.getStockSymbol().addAll(List.of(VALID_STOCK_SYMBOL));
        mockStockData.put(stock.getStockSymbol(), stock);
        when(stockService.getStocksMarketData(anyList())).thenReturn(mockStockData);

        // When
        GetStockResponse response = stockController.getStockResponse(request);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(response.getStock().get(0).getStockSymbol(), VALID_STOCK_SYMBOL, "Stock symbol mismatch");
        assertEquals(response.getStock().get(0).getLatestPrice(), stock.getLatestPrice(), "Latest price mismatch");
        assertEquals(response.getStock().get(0).getDailyChangePct(), stock.getDailyChangePct(), "Daily Change Pct mismatch");
        assertEquals(response.getStock().get(0).getDailyChangeInTL(), stock.getDailyChangeInTL(), "Daily Change TL mismatch");
        assertEquals(response.getStock().get(0).getTradingVolumeTL(), stock.getTradingVolumeTL(), "Trading Volume TL mismatch");
        assertEquals(response.getStock().get(0).getTradeVolumeCount(), stock.getTradeVolumeCount(), "Trading Volume count mismatch");
        assertFalse(response.isIsPartialResponse(), "Should not be partial response");
        assertNull(response.getInvalidSymbols(), "Invalid symbols should be null");

    }

    @Test
    @DisplayName("Should Return Invalid Symbols When a Valid and Invalid Stock Symbol Is Given")
    void shouldReturnInvalidSymbolsWhenValidAndInvalidStockSymbolIsGiven(){
        // Given
        request.getStockSymbol().addAll(List.of(VALID_STOCK_SYMBOL, INVALID_STOCK_SYMBOL));
        mockStockData.put(stock.getStockSymbol(), stock);
        when(stockService.getStocksMarketData(anyList())).thenReturn(mockStockData);

        // When
        GetStockResponse response = stockController.getStockResponse(request);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(response.getStock().get(0).getStockSymbol(), VALID_STOCK_SYMBOL, "Stock symbol mismatch");
        assertEquals(response.getStock().get(0).getLatestPrice(), stock.getLatestPrice(), "Latest price mismatch");
        assertEquals(response.getStock().get(0).getDailyChangePct(), stock.getDailyChangePct(), "Daily Change Pct mismatch");
        assertEquals(response.getStock().get(0).getDailyChangeInTL(), stock.getDailyChangeInTL(), "Daily Change TL mismatch");
        assertEquals(response.getStock().get(0).getTradingVolumeTL(), stock.getTradingVolumeTL(), "Trading Volume TL mismatch");
        assertEquals(response.getStock().get(0).getTradeVolumeCount(), stock.getTradeVolumeCount(), "Trading Volume count mismatch");
        assertTrue(response.isIsPartialResponse(), "Should be partial response");
        assertEquals(response.getInvalidSymbols(), INVALID_STOCK_SYMBOL);
    }

    @Test
    @DisplayName("Should Throw InvalidSymbolsException When Invalid Symbols Is Given")
    void shouldThrowInvalidSymbolsExceptionWhenInvalidSymbolIsGiven(){
        // Given
        request.getStockSymbol().addAll(List.of(INVALID_STOCK_SYMBOL));
        when(stockService.getStocksMarketData(anyList()))
                .thenThrow(new InvalidSymbolsException(INVALID_STOCK_SYMBOL));

        // When & Then
        InvalidSymbolsException thrown = assertThrows(InvalidSymbolsException.class, () -> {
            stockController.getStockResponse(request);
        });

        assertEquals("Symbols are not valid. Given stock symbols are 'INVALID'", thrown.getMessage());

    }
}
