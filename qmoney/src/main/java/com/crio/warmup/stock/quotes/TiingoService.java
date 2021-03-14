
package com.crio.warmup.stock.quotes;

import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.client.RestTemplate;

public class TiingoService implements StockQuotesService {


  private RestTemplate restTemplate;

  protected TiingoService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }


  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Implement getStockQuote method below that was also declared in the interface.

  // Note:
  // 1. You can move the code from PortfolioManagerImpl#getStockQuote inside newly created method.
  // 2. Run the tests using command below and make sure it passes.
  //    ./gradlew test --tests TiingoServiceTest


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException {
        if (from.compareTo(to) >= 0) {
          throw new RuntimeException();
        }
    
    String uri = buildUri(symbol, from, to);
    String str = restTemplate.getForObject(uri, String.class);
    ObjectMapper objectMapper = getObjectMapper();
    TiingoCandle[] candleList = objectMapper.readValue(str, TiingoCandle[].class);
    //TiingoCandle[] candleList = restTemplate.getForObject(uri, TiingoCandle[].class);
    
        if (candleList == null) {
          return new ArrayList<Candle>();
        }
        List<Candle> stockList = Arrays.asList(candleList);
        //return stockList;

      return stockList;
  }

  private static ObjectMapper getObjectMapper() {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Write a method to create appropriate url to call the Tiingo API.

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String uriTemplate = "https://api.tiingo.com/tiingo/daily/" + symbol + "/prices?startDate=" 
              + startDate.toString() + "&endDate=" + endDate.toString() 
              + "&token=" + "d431f671467bfe6b952b908e6eea0397bfa1f560";
    return uriTemplate;
  }

}
