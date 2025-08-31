package unit.service;

import com.soap.stocks.exception.InvalidSymbolsException;
import com.soap.stocks.exception.NoMarketDataFoundException;
import com.soap.stocks.service.impl.StockServiceImpl;
import https.soap_stocks_up_railway.Stock;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;


@ExtendWith(MockitoExtension.class)
public class StockServiceImplTest {

    @InjectMocks
    private StockServiceImpl stockService;

    private static final String VALID_STOCK_SYMBOL = "SASA";
    private static final String INVALID_STOCK_SYMBOL = "INVALID";
    private static final String MOCK_URL = "http://mock-url.com";
    private static final String MOCK_HTML_RESOURCE = "mock/sample-web-html.txt";
    private static final String MOCK_HTML_WITH_NO_DATA_ROWS =  "<table></table>";

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
        ReflectionTestUtils.setField(stockService, "stockDataUrl", MOCK_URL);
    }

    @Test
    @DisplayName("updateStockData - Should update data on schedule")
    void shouldUpdateDataOnSchedule() throws IOException, NoSuchMethodException {

        // Given
        String mockHtml;
        Stock expectedStock = createValidStock();
        Map<String, Stock> result;

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(MOCK_HTML_RESOURCE)) {
            assertNotNull(inputStream, "Test resource not found: " + MOCK_HTML_RESOURCE);
            mockHtml = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }

        Document mockDoc = Jsoup.parse(mockHtml);
        //Mock connection
        org.jsoup.Connection mockConnection = mock(org.jsoup.Connection.class, Mockito.withSettings().lenient());
        Mockito.when(mockConnection.get()).thenReturn(mockDoc);

        // When
        try (MockedStatic<Jsoup> jsoupMock = Mockito.mockStatic(Jsoup.class)) {
            jsoupMock.when(() -> Jsoup.connect(Mockito.anyString()))
                    .thenReturn(mockConnection);
            // Call private method
            ReflectionTestUtils.invokeMethod(stockService, "updateStockData");

            result = (Map<String, Stock>) ReflectionTestUtils.getField(stockService, "stocksMarketData");
        }

        // Then
        assertTrue(result.containsKey(VALID_STOCK_SYMBOL), "Result should contain expected stock symbol");
        Stock reponseStock = result.get(VALID_STOCK_SYMBOL);

        assertEquals(expectedStock.getLatestPrice(), reponseStock.getLatestPrice(), "Latest price mismatch");
        assertEquals(expectedStock.getDailyChangePct(), reponseStock.getDailyChangePct(), "Daily Change Pct mismatch");
        assertEquals(expectedStock.getDailyChangeInTL(), reponseStock.getDailyChangeInTL(), "Daily Change TL mismatch");
        assertEquals(expectedStock.getTradingVolumeTL(), reponseStock.getTradingVolumeTL(), "Trading Volume TL mismatch");
        assertEquals(expectedStock.getTradeVolumeCount(), reponseStock.getTradeVolumeCount(), "Trading Volume count mismatch");
    }

    @Test
    @DisplayName("Should get stocks market data when a valid stock symbol given")
    void shouldGetStockMarketDataWhenValidSymbolIsGiven(){
        // Given
        Stock stock = createValidStock();

        Map<String, Stock> stocksMarketData = createStocksMarketDataWith(List.of(stock));
        ReflectionTestUtils.setField(stockService, "stocksMarketData", stocksMarketData);

        List<String> stockSymbols = List.of(VALID_STOCK_SYMBOL);

        // When
        Map<String, Stock>  response = stockService.getStocksMarketData(stockSymbols);

        // Then
        assertTrue(response.containsKey(VALID_STOCK_SYMBOL), "Response should contain stock symbol");
        assertEquals(0, new BigDecimal("3.18").compareTo(response.get(VALID_STOCK_SYMBOL).getLatestPrice()), "Stock symbol should have correct latest price.");
    }

    @Test
    @DisplayName("Should throw InvalidSymbolsException when a invalid stock symbol given")
    void shouldThrowInvalidSymbolsExceptionWhenInvalidSymbolIsGiven(){
        //Given
        Stock stock = createValidStock();
        List<String> invalidStockSymbols = List.of(INVALID_STOCK_SYMBOL);

        Map<String, Stock> stocksMarketData = createStocksMarketDataWith(List.of(stock));
        ReflectionTestUtils.setField(stockService, "stocksMarketData", stocksMarketData);

        // When & Then
        assertThrows(InvalidSymbolsException.class, ()->{
            stockService.getStocksMarketData(invalidStockSymbols);
        }, "Was excepting a InvalidSymbolsException to be thrown");
    }

    @Test
    @DisplayName("Should throw NoMarketDataFoundException when stocksMarketData is empty")
    void shouldThrowNoMarketDataFoundExceptionWhenStocksMarketDataIsEmpty(){
        List<String> invalidStockSymbols = List.of(VALID_STOCK_SYMBOL);

        // When & Then
        assertThrows(NoMarketDataFoundException.class, ()->{
            stockService.getStocksMarketData(invalidStockSymbols);
        }, "Was excepting a NoMarketDataFoundException to be thrown");
    }

    @Test
    @DisplayName("Should Not Update StocksMarketData When StockDataUrl is empty")
    void shouldNotUpdateStocksMarketDataWhenStockDataUrlIsEmpty() throws Exception {
        // Given
        ReflectionTestUtils.setField(stockService, "stockDataUrl", "");

        // When
        ReflectionTestUtils.invokeMethod(stockService, "updateStockData");
        Map<String, Stock> result =  (Map<String, Stock>) ReflectionTestUtils.getField(stockService, "stocksMarketData");

        // Then
        assertTrue( result.isEmpty(),"StocksMarketData Should Not Be Updated When StockDataUrl is empty");

    }

    @Test
    @DisplayName("Should Not Update StocksMarketData When StockDataUrl is null")
    void shouldNotUpdateStocksMarketDataWhenStockDataUrlIsNull() throws Exception {
        // Given
        ReflectionTestUtils.setField(stockService, "stockDataUrl", null);

        // When
        ReflectionTestUtils.invokeMethod(stockService, "updateStockData");
        Map<String, Stock> result =  (Map<String, Stock>) ReflectionTestUtils.getField(stockService, "stocksMarketData");

        // Then
        assertTrue( result.isEmpty(), "StocksMarketData Should Not Be Updated When StockDataUrl is null");
    }

    @Test
    @DisplayName("Should Not Update StocksMarketData When document is null")
    void shouldNotUpdateStocksMarketDataWhenDocIsNull(){
        // Given
        ReflectionTestUtils.setField(stockService, "document", null);

        // When
        ReflectionTestUtils.invokeMethod(stockService, "updateStocksMarketData");
        Map<String, Stock> result =  (Map<String, Stock>) ReflectionTestUtils.getField(stockService, "stocksMarketData");

        // Then
        assertTrue( result.isEmpty(), "StocksMarketData Should Not Be Updated When document is null");

    }

    @Test
    @DisplayName("Should Not Update StocksMarketData When document has no data rows")
    void shouldNotUpdateStocksMarketDataWhenDocumentHasNoDataRows() throws IOException {
        Map<String, Stock> result;
        Document mockDoc = Jsoup.parse(MOCK_HTML_WITH_NO_DATA_ROWS);
        //Mock connection
        org.jsoup.Connection mockConnection = mock(org.jsoup.Connection.class, Mockito.withSettings().lenient());
        Mockito.when(mockConnection.get()).thenReturn(mockDoc);

        // When
        try (MockedStatic<Jsoup> jsoupMock = Mockito.mockStatic(Jsoup.class)) {
            jsoupMock.when(() -> Jsoup.connect(Mockito.anyString()))
                    .thenReturn(mockConnection);

            // Call private method
            ReflectionTestUtils.invokeMethod(stockService, "updateStockData");

            result = (Map<String, Stock>) ReflectionTestUtils.getField(stockService, "stocksMarketData");
        }
        // Then
        assertTrue( result.isEmpty(), "StocksMarketData Should Not Be Updated When document has no data rows");
    }

    @Test
    @DisplayName("Should parse standard decimal string correctly")
    void testShouldParseStandardDecimal() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Given
        String input = "1.234,56";

        // Set private method to accessible
        Method method = StockServiceImpl.class.getDeclaredMethod("parseBigDecimal", String.class);
        method.setAccessible(true);

        // When
        BigDecimal result = (BigDecimal) method.invoke(stockService, input);

        // Then
        assertEquals(new BigDecimal("1234.56"), result, "Parsing error. Not working expected.");
    }

    @Test
    @DisplayName("Should throw InvocationTargetException when null value given to parse")
    void testShouldThrowInvocationTargetExceptionWhenNullValueGiven() throws NoSuchMethodException{
        // Given
        String input = null;

        // Set private method to accessible
        Method method = StockServiceImpl.class.getDeclaredMethod("parseBigDecimal", String.class);
        method.setAccessible(true);

        // When & Then
        // InvocationTargetException wraps the actual IllegalArgumentException
        assertThrows(InvocationTargetException.class, ()->{
            method.invoke(stockService, input);
        }, "Was excepting a InvocationTargetException to be thrown");
    }

    @Test
    @DisplayName("Should throw InvocationTargetException when invalid value given to parse")
    void testShouldThrowInvocationTargetExceptionWhenInvalidValueGiven() throws NoSuchMethodException{
        // Given
        String input = "invalid value";

        // Set private method to accessible
        Method method = StockServiceImpl.class.getDeclaredMethod("parseBigDecimal", String.class);
        method.setAccessible(true);

        // When & Then
        // InvocationTargetException wraps the actual IllegalArgumentException
        assertThrows(InvocationTargetException.class, ()->{
            method.invoke(stockService, input);
        }, "Was excepting a IllegalArgumentException to be thrown");
    }
}