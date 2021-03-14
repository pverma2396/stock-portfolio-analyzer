
package com.crio.warmup.stock.quotes;

// import static java.time.temporal.ChronoUnit.DAYS;
// import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.dto.AlphavantageCandle;
import com.crio.warmup.stock.dto.AlphavantageDailyResponse;
import com.crio.warmup.stock.dto.Candle;
// import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
// import java.util.Comparator;
// import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
// import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class AlphavantageService implements StockQuotesService {

  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Implement the StockQuoteService interface as per the contracts. Call Alphavantage service
  //  to fetch daily adjusted data for last 20 years.
  //  Refer to documentation here: https://www.alphavantage.co/documentation/
  //  --
  //  The implementation of this functions will be doing following tasks:
  //    1. Build the appropriate url to communicate with third-party.
  //       The url should consider startDate and endDate if it is supported by the provider.
  //    2. Perform third-party communication with the url prepared in step#1
  //    3. Map the response and convert the same to List<Candle>
  //    4. If the provider does not support startDate and endDate, then the implementation
  //       should also filter the dates based on startDate and endDate. Make sure that
  //       result contains the records for for startDate and endDate after filtering.
  //    5. Return a sorted List<Candle> sorted ascending based on Candle#getDate
  // Note:
  // 1. Make sure you use {RestTemplate#getForObject(URI, String)} else the test will fail.
  // 2. Run the tests using command below and make sure it passes:
  //    ./gradlew test --tests AlphavantageServiceTest
  //CHECKSTYLE:OFF

  private RestTemplate restTemplate;

  protected AlphavantageService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }


  @Override
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to) throws JsonProcessingException {
    // TODO Auto-generated method stub
    if (from.compareTo(to) >= 0) {
      throw new RuntimeException();
    }

    String uri = buildUri(symbol, from, to);

    ObjectMapper objectMapper = getObjectMapper();

    String str = restTemplate.getForObject(uri, String.class);

    if (str == null) {
      return new ArrayList<Candle>();
    }

    AlphavantageDailyResponse alphavantageDailyResponse = new AlphavantageDailyResponse();
    
    alphavantageDailyResponse = objectMapper.readValue(str, AlphavantageDailyResponse.class);

    Map<LocalDate, AlphavantageCandle> mapbeforeSort = alphavantageDailyResponse.getCandles();

    Map<LocalDate, AlphavantageCandle> mapafterSort = new TreeMap<>(mapbeforeSort);

    List<AlphavantageCandle> alphavantageCandlesList = new ArrayList<AlphavantageCandle>();
    for(Map.Entry<LocalDate, AlphavantageCandle> entry : mapafterSort.entrySet()) {
        if((entry.getKey()).compareTo(from) >=0 && (entry.getKey()).compareTo(to) <= 0) {
          AlphavantageCandle temp = entry.getValue();
          temp.setDate(entry.getKey());
          alphavantageCandlesList.add(temp);
        }
    }

    AlphavantageCandle[] list2 = new AlphavantageCandle[alphavantageCandlesList.size()];

    for (int i = 0;i < alphavantageCandlesList.size();i++) {
      list2[i] = alphavantageCandlesList.get(i);
    }

    // return aalphavantageCandlesList;
    return Arrays.asList(list2);
  }

  //CHECKSTYLE:ON
  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  1. Write a method to create appropriate url to call Alphavantage service. The method should
  //     be using configurations provided in the {@link @application.properties}.
  //  2. Use this method in #getStockQuote.

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String uriTemplate = "https://www.alphavantage.co/query?" 
              + "function=TIME_SERIES_DAILY_ADJUSTED&symbol=" + symbol
              + "&outputsize=full" 
              + "&apikey=NLHNSS4BR6TWYQS6";
    return uriTemplate;
  }

}

