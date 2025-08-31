# 📈 SOAP Web Service - Stocks Market Data 

This project is a **SOAP-based web service** for retrieving **stocks market data**. The service allows clients to request information about one or more stock symbols and receive detailed responses including price, volume, and daily change metrics.

**Soap Endpoint:** `https://soap-stocks.up.railway.app/soap`

**WSDL Endpoint:** `https://soap-stocks.up.railway.app/soap/stocks.wsdl`

**JUnit - JaCoCo Reports:** `https://junit-soap-stocks.up.railway.app`

### Request Example

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:soap="https://soap-stocks.up.railway.app">
    <soapenv:Header/>
    <soapenv:Body>
        <soap:GetStockRequest>
            <soap:stockSymbol>HEKTS</soap:stockSymbol>
            <soap:stockSymbol>SASA</soap:stockSymbol>
        </soap:GetStockRequest>
    </soapenv:Body>
</soapenv:Envelope>
```
### Important Notice
⚠️ **This API is for demonstration purposes only**  
It is not intended for commercial or personal use. The project was developed to showcase coding abilities and architectural understanding.

