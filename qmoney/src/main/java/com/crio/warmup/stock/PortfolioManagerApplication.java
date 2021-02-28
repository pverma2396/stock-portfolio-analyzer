
package com.crio.warmup.stock;

//import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
//import com.crio.warmup.stock.dto.AnnualizedReturn
//import com.crio.warmup.stock.dto.AnnualizedReturn;
//import com.crio.warmup.stock.dto.PortfolioTrade;
//import com.crio.warmup.stock.dto.TotalReturnsDto;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.fasterxml.jackson.core.type.TypeReference;


//import com.crio.warmup.stock.dto.TotalReturnsDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
//import java.net.URI;
import java.net.URISyntaxException;
//import java.net.URLEncoder;
//import java.nio.file.Files;
import java.nio.file.Paths;
//import java.text.SimpleDateFormat;
//import java.time.LocalDate;
//import java.time.temporal.ChronoUnit;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
//import java.util.Collections;
//import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
//import java.util.logging.Level;
import java.util.logging.Logger;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
import org.apache.logging.log4j.ThreadContext;
//import java.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.MediaType;
//import org.springframework.web.client.RestTemplate;
//import java.nio.file.Files;
//import java.time.format.DateTimeFormatter;
//import java.time.format.FormatStyle;
//import java.util.logging.Level;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {

  
  //  Read the json file provided in the argument[0]. The file will be available in the classpath.
  //    1. Use #resolveFileFromResources to get actual file from classpath.
  //    2. Extract stock symbols from the json file with ObjectMapper provided by #getObjectMapper.
  //    3. Return the list of all symbols in the same order as provided in json.

  //  Note:
  //  1. There can be few unused imports, you will need to fix them to make the build pass.
  //  2. You can use "./gradlew build" to check if your code builds successfully.

  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {

    //resolveFileFromResources(args[0]);

    Object objectMapper = getObjectMapper();
    
    PortfolioTrade[] ob = 
    ((ObjectMapper)objectMapper).readValue(resolveFileFromResources(args[0]),
    PortfolioTrade[].class);
    List<String> ls = new ArrayList<>();
    for (PortfolioTrade pf:ob) {
      ls.add(pf.getSymbol());
    }
    //return Collections.emptyList();
    return ls;
  }





  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Now that you have the list of PortfolioTrade and their data, calculate annualized returns
  //  for the stocks provided in the Json.
  //  Use the function you just wrote #calculateAnnualizedReturns.
  //  Return the list of AnnualizedReturns sorted by annualizedReturns in descending order.

  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.


  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws IOException, URISyntaxException {

    Object objectMapper = getObjectMapper();
    
    //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    //LocalDate ed = LocalDate.parse(args[1], formatter);
    
    List<AnnualizedReturn> tempList = new ArrayList<>();
    
    PortfolioTrade[] ob = 
    ((ObjectMapper)objectMapper).readValue(resolveFileFromResources(args[0]),
    PortfolioTrade[].class);
    List<String> symbols = new ArrayList<>();
    List<LocalDate> startDate = new ArrayList<>();
    List<Integer> quantity = new ArrayList<>();  
    for (PortfolioTrade pf:ob) {
      symbols.add(pf.getSymbol());
      startDate.add(pf.getPurchaseDate());
      quantity.add(pf.getQuantity());
    }


    RestTemplate restTemplate = new RestTemplate();

    for (int i = 0;i < symbols.size();i++) {
      List<TiingoCandle> candlelist = new ArrayList<TiingoCandle>();
      String str = restTemplate.getForObject("https://api.tiingo.com/tiingo/daily/{symbol}/prices?startDate={startDate}&endDate={endDate}&token=d431f671467bfe6b952b908e6eea0397bfa1f560",
              String.class, symbols.get(i), startDate.get(i).toString(), args[1]);
      
      candlelist = ((ObjectMapper) objectMapper).readValue(str,
       new TypeReference<List<TiingoCandle>>(){});

      PortfolioTrade tf = new PortfolioTrade(symbols.get(i), quantity.get(i), startDate.get(i));
      //System.out.println(tf.getSymbol() + " " + tf.getQuantity() + " " + tf.getPurchaseDate());


      //System.out.println(calculateAnnualizedReturns(LocalDate.parse(args[1]),
      // tf, candlelist.get(0).getOpen(), candlelist.get(candlelist.size()-1).getClose()));
      //System.out.println(candlelist.get(0).getOpen() + " "
      // + candlelist.get(candlelist.size()-1).getClose());
      tempList.add(calculateAnnualizedReturns(LocalDate.parse(args[1]), 
          tf, candlelist.get(0).getOpen(), candlelist.get(candlelist.size() - 1).getClose()));

      //System.out.println(candlelist.get(candlelist.size()-1).getClose());
    }

    Collections.sort(tempList, new Comparator<AnnualizedReturn>() {
      public int compare(AnnualizedReturn a1, AnnualizedReturn a2) {
        return a2.getAnnualizedReturn().compareTo(a1.getAnnualizedReturn());
      }
    });

    return tempList;
    //return Collections.emptyList();
  }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Return the populated list of AnnualizedReturn for all stocks.
  //  Annualized returns should be calculated in two steps:
  //   1. Calculate totalReturn = (sell_value - buy_value) / buy_value.
  //      1.1 Store the same as totalReturns
  //   2. Calculate extrapolated annualized returns by scaling the same in years span.
  //      The formula is:
  //      annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
  //      2.1 Store the same as annualized_returns
  //  Test the same using below specified command. The build should be successful.
  //     ./gradlew test --tests PortfolioManagerApplicationTest.testCalculateAnnualizedReturn

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {

    //String symbol = "";
    Double annualizedReturn;
    Double totalReturns;
    Double totalnumyears;
    LocalDate startDate = trade.getPurchaseDate();
    totalReturns = (sellPrice - buyPrice) / buyPrice;

    //Period period = Period.between(startDate, endDate);
    //startDate.until(endDate, ChronoUnit.DAYS)

    totalnumyears = (double) (startDate.until(endDate, ChronoUnit.DAYS) / 365.00);

    annualizedReturn = (Math.pow((1 + totalReturns), (1 / totalnumyears))) - 1;

    // System.out.println(startDate + " " + endDate + " " + Period.between(startDate, endDate) 
    //   + " " + totalReturns + " " + totalnumyears + " " + annualizedReturn);

    AnnualizedReturn ar = new AnnualizedReturn(trade.getSymbol(), annualizedReturn, totalReturns);

    return ar;
    //return new AnnualizedReturn("", 0.0, 0.0);
  }








  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(
        Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }


  
  //  Follow the instructions provided in the task documentation and fill up the correct values for
  //  the variables provided. First value is provided for your reference.
  //  A. Put a breakpoint on the first line inside mainReadFile() which says
  //    return Collections.emptyList();
  //  B. Then Debug the test #mainReadFile provided in PortfoliomanagerApplicationTest.java
  //  following the instructions to run the test.
  //  Once you are able to run the test, perform following tasks and record the output as a
  //  String in the function below.
  //  Use this link to see how to evaluate expressions -
  //  https://code.visualstudio.com/docs/editor/debugging#_data-inspection
  //  1. evaluate the value of "args[0]" and set the value
  //     to the variable named valueOfArgument0 (This is implemented for your reference.)
  //  2. In the same window, evaluate the value of expression below and set it
  //  to resultOfResolveFilePathArgs0
  //     expression ==> resolveFileFromResources(args[0])
  //  3. In the same window, evaluate the value of expression below and set it
  //  to toStringOfObjectMapper.
  //  You might see some garbage numbers in the output. Dont worry, its expected.
  //    expression ==> getObjectMapper().toString()
  //  4. Now Go to the debug window and open stack trace. Put the name of the function you see at
  //  second place from top to variable functionNameFromTestFileInStackTrace
  //  5. In the same window, you will see the line number of the function in the stack trace window.
  //  assign the same to lineNumberFromTestFileInStackTrace
  //  Once you are done with above, just run the corresponding test and
  //  make sure its working as expected. use below command to do the same.
  //  ./gradlew test --tests PortfolioManagerApplicationTest.testDebugValues

  public static List<String> debugOutputs() {

    String valueOfArgument0 = "trades.json";
    String resultOfResolveFilePathArgs0 =
            "/home/crio-user/workspace/praveenverma2396-ME_QMONEY/qmoney/bin/main/trades.json";
    String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@2f9f7dcf";
    String functionNameFromTestFileInStackTrace = "mainReadFile";
    String lineNumberFromTestFileInStackTrace = "22";


    return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
        toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
        lineNumberFromTestFileInStackTrace});
  }

  @Autowired
  static RestTemplate restTemplate;

  // Note:
  // Remember to confirm that you are getting same results for annualized returns
  // as in Module 3.

  // TODO: CRIO_TASK_MODULE_REST_API
  //  Find out the closing price of each stock on the end_date and return the list
  //  of all symbols in ascending order by its close value on end date.

  // Note:
  // 1. You may have to register on Tiingo to get the api_token.
  // 2. Look at args parameter and the module instructions carefully.
  // 2. You can copy relevant code from #mainReadFile to parse the Json.
  // 3. Use RestTemplate#getForObject in order to call the API,
  //    and deserialize the results in List<Candle>


  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {

    Object objectMapper = getObjectMapper();
    
    PortfolioTrade[] ob = 
    ((ObjectMapper)objectMapper).readValue(resolveFileFromResources(args[0]),
    PortfolioTrade[].class);
    List<String> symbols = new ArrayList<>();
    List<String> startDate = new ArrayList<>();
    for (PortfolioTrade pf:ob) {
      symbols.add(pf.getSymbol());
      startDate.add((pf.getPurchaseDate()).toString());
    }

    HashMap<String, Double> mapclosing = new HashMap<String, Double>();
    List<String> finalresult = new ArrayList<String>();

    
    Object objectmapper = getObjectMapper();
    RestTemplate restTemplate = new RestTemplate();

    for (int i = 0;i < symbols.size();i++) {
      List<TiingoCandle> candlelist = new ArrayList<TiingoCandle>();
      String str = restTemplate.getForObject("https://api.tiingo.com/tiingo/daily/{symbol}/prices?startDate={startDate}&endDate={endDate}&token=d431f671467bfe6b952b908e6eea0397bfa1f560",
              String.class, symbols.get(i), startDate.get(i), args[1]);
      
      candlelist = ((ObjectMapper) objectmapper).readValue(str,
       new TypeReference<List<TiingoCandle>>(){});
      mapclosing.put(symbols.get(i), candlelist.get(candlelist.size() - 1).getClose());


      //System.out.println(candlelist.get(candlelist.size()-1).getClose());
    }

    List<Map.Entry<String, Double>> list = new 
          LinkedList<Map.Entry<String, Double>>(mapclosing.entrySet());
    
    Collections.sort(list, new Comparator<Map.Entry<String, Double>>() { 
      public int compare(Map.Entry<String, Double> o1,  
                         Map.Entry<String, Double> o2) { 
          return (o1.getValue()).compareTo(o2.getValue()); 
        } 
    });

    for (Map.Entry<String, Double> a : list) {
      finalresult.add(a.getKey());
    }

    //System.out.println(str);
    return finalresult;
    //return Collections.emptyList();
  }


  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());

    //printJsonObject(mainReadFile(args));


    //printJsonObject(mainReadQuotes(args));



    printJsonObject(mainCalculateSingleReturn(args));

  }
}

