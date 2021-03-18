
package com.crio.warmup.stock.portfolio;

// import static java.time.temporal.ChronoUnit.DAYS;
// import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
// import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.crio.warmup.stock.quotes.StockQuotesService;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
// import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
// import java.util.concurrent.ExecutionException;
// import java.util.concurrent.ExecutorService;
// import java.util.concurrent.Executors;
// import java.util.concurrent.Future;
// import java.util.concurrent.TimeUnit;
// import java.util.stream.Collectors;
// import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
// import com.crio.warmup.stock.quotes.StockQuotesService;
// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {


  private StockQuotesService stockQuotesService;

  private RestTemplate restTemplate;

  // Caution: Do not delete or modify the constructor, or else your build will
  // break!
  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility

  @Deprecated
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  protected PortfolioManagerImpl(StockQuotesService stockQuotesService){
    this.stockQuotesService = stockQuotesService;
  }




  //TODO: CRIO_TASK_MODULE_REFACTOR
  // 1. Now we want to convert our code into a module, so we will not call it from main anymore.
  //    Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  //    into #calculateAnnualizedReturn function here and ensure it follows the method signature.
  // 2. Logic to read Json file and convert them into Objects will not be required further as our
  //    clients will take care of it, going forward.

  // Note:
  // Make sure to exercise the tests inside PortfolioManagerTest using command below:
  // ./gradlew test --tests PortfolioManagerTest

  //CHECKSTYLE:OFF

  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
       LocalDate endDate) {

    AnnualizedReturn annualizedReturn;
    List<AnnualizedReturn> annualizedReturnslist = new ArrayList<AnnualizedReturn>();

    for (PortfolioTrade trade: portfolioTrades) {
      
      annualizedReturn = getAnnualizedReturn(trade, endDate);

      annualizedReturnslist.add(annualizedReturn);

    }

    Comparator<AnnualizedReturn> comparator = getComparator();
    Collections.sort(annualizedReturnslist, comparator);

    return annualizedReturnslist;
    //return Collections.emptyList();
  }


  public AnnualizedReturn getAnnualizedReturn(PortfolioTrade trade, LocalDate endDate) {
    AnnualizedReturn annualizedReturn;
    String symbol = trade.getSymbol();
    LocalDate startDate = trade.getPurchaseDate();

    try {
      Double annualizedReturns;
      Double totalReturns;
      Double totalnumyears;
      List<Candle> stockList = getStockQuote(symbol, startDate, endDate);

      Double buyPrice = stockList.get(0).getOpen();
      Double sellPrice = stockList.get(stockList.size() - 1).getClose();

      totalReturns = (sellPrice - buyPrice) / buyPrice;

      totalnumyears = (double) (startDate.until(endDate, ChronoUnit.DAYS) / 365.00);

      annualizedReturns = (Math.pow((1 + totalReturns), (1 / totalnumyears))) - 1;

      annualizedReturn = new AnnualizedReturn(symbol, annualizedReturns, totalReturns);

    } catch (JsonProcessingException e) {
      annualizedReturn = new AnnualizedReturn(symbol, Double.NaN, Double.NaN);
    }

    return annualizedReturn;

  }

  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo third-party APIs to a separate function.
  //  Remember to fill out the buildUri function and use that.


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException {
      
    try {
      return stockQuotesService.getStockQuote(symbol, from, to);
    } catch (StockQuoteServiceException e) {
      System.out.println(e.getMessage());
    }
      
    // if (from.compareTo(to) >= 0) {
    //   throw new RuntimeException();
    // }

    // String uri = buildUri(symbol, from, to);

    // TiingoCandle[] candleList = restTemplate.getForObject(uri, TiingoCandle[].class);

    // if (candleList == null) {
    //   return new ArrayList<Candle>();
    // }
    // List<Candle> stockList = Arrays.asList(candleList);
    // //return stockList;
    

    // return stockList;
    return Collections.emptyList();
  }


  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String uriTemplate = "https://api.tiingo.com/tiingo/daily/" + symbol + "/prices?startDate=" 
              + startDate.toString() + "&endDate=" + endDate.toString() 
              + "&token=" + "d431f671467bfe6b952b908e6eea0397bfa1f560";
    // String uriTemplate = "https://api.tiingo.com/tiingo/daily/" + symbol 
    // + "/prices?startDate=" + startDate.toString() + "&endDate=" + endDate.toString() 
    // + "&token=" + "d431f671467bfe6b952b908e6eea0397bfa1f560";
    // String uriTemplate = "https://api.tiingo.com/tiingo/daily/" + symbol + "/prices?startDate=" + startDate.toString() + "&endDate=" + endDate.toString() + "&token=" + "d431f671467bfe6b952b908e6eea0397bfa1f560";
    return uriTemplate;
  }




  // ¶TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Modify the function #getStockQuote and start delegating to calls to
  //  stockQuoteService provided via newly added constructor of the class.
  //  You also have a liberty to completely get rid of that function itself, however, make sure
  //  that you do not delete the #getStockQuote function.



}
