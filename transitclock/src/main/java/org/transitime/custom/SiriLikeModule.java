package org.transitime.custom;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.avl.AvlClient;
import org.transitime.custom.aws.AvlSqsClientModule;
import org.transitime.db.structs.AvlReport;
import org.transitime.db.structs.AvlReport.AssignmentType;
import org.transitime.modules.Module;
import org.transitime.utils.threading.BoundedExecutor;
import org.transitime.utils.threading.NamedThreadFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SiriLikeModule extends Module {

  private static final Logger logger = 
      LoggerFactory.getLogger(AvlSqsClientModule.class);
  
  private static SimpleDateFormat ISO_DATE_SHORT_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
  private static SimpleDateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSXXX");

  public List<String> _routeList = new ArrayList<String>();
  private final BoundedExecutor _avlClientExecutor;
  private XPathExpression mvjExpression;
  private XPathExpression tripIdExpression;
  private XPathExpression recordedAtExpression;
  private XPathExpression vehicleIdExpression;
  private XPathExpression latExpression;
  private XPathExpression lonExpression;
  private XPathExpression speedExpression;
  private XPathExpression bearingExpression;
  private DocumentBuilderFactory factory;
  private DocumentBuilder builder;
  private String _apiKey;
  private String _baseUrl;
  
  public SiriLikeModule(String agencyId) {
    super(agencyId);
    // todo move this to config
    addRoutes();
    
    _baseUrl = "http://api.rideuta.com/SIRI/SIRI.svc/VehicleMonitor/ByRoute";
    _apiKey = "UPBML0P0ZO0";
    
    ISO_DATE_FORMAT.setLenient(false);
    ISO_DATE_SHORT_FORMAT.setLenient(false);
    factory = DocumentBuilderFactory.newInstance();
    try {
      builder = factory.newDocumentBuilder();
    
    XPathFactory xPathfactory = XPathFactory.newInstance();
    XPath xpath = xPathfactory.newXPath();
    mvjExpression = xpath.compile("/Siri/VehicleMonitoringDelivery/VehicleActivity/MonitoredVehicleJourney");
    recordedAtExpression = xpath.compile("/Siri/VehicleMonitoringDelivery/VehicleActivity/RecordedAtTime/text()");
    tripIdExpression = xpath.compile("FramedVehicleJourneyRef/DatedVehicleJourneyRef/text()");
    vehicleIdExpression = xpath.compile("VehicleRef/text()");
    latExpression = xpath.compile("VehicleLocation/Latitude/text()");
    lonExpression = xpath.compile("VehicleLocation/Longitude/text()");
    speedExpression = xpath.compile("Extensions/Speed/text()");
    bearingExpression = xpath.compile("Extensions/Bearing/text()");
    } catch (ParserConfigurationException | XPathExpressionException e) {
      logger.error("invalid config:", e);
    }
    
    // Create the executor that actually processes the AVL data
    NamedThreadFactory avlClientThreadFactory = new NamedThreadFactory(
        "avlClient");
    Executor executor = Executors.newFixedThreadPool(1,
        avlClientThreadFactory);
    _avlClientExecutor = new BoundedExecutor(executor, 100);
  }



  public String getApiKey() {
    return _apiKey;
  }

  public String getUrl() {
    return _baseUrl;
  }

  public void setRoutes(List<String> routes) {
    _routeList = routes;
  }

  public List<String> getRoutes() {
    return _routeList;
  }

  @Override
  public void run() {
    while (!Thread.interrupted()) {
      try {
        refresh();
        Thread.sleep(1000);
      } catch (Exception e) {
        logger.error("issue processing data", e);
      }
    }
  }

  private void refresh() throws Exception {
    for (String route : getRoutes()) {
      NodesAndTimestamp nodesAndTimestamp = parseVehicles(new URL(constructUrl(route, getApiKey(), getUrl())));
      for (Node n : nodesAndTimestamp.getNodes()) {
        AvlReport avl = parse(n, nodesAndTimestamp.getTimestamp());
        if (avl != null) {
          Runnable avlClient = new AvlClient(avl);
          _avlClientExecutor.execute(avlClient);
        }
      }
    }
  }

  
  private AvlReport parse(Node node, long timestamp) throws Exception {
    String tripId = (String) tripIdExpression.evaluate(node, XPathConstants.STRING);
    if (tripId == null) {
      logger.error("no trip for node=" + node);
      return null;
    }
    String vehicleId = (String)vehicleIdExpression.evaluate(node, XPathConstants.STRING);
    double lat = asDouble(latExpression.evaluate(node, XPathConstants.STRING));
    double lon = asDouble(lonExpression.evaluate(node, XPathConstants.STRING));
    float speed = asFloat(speedExpression.evaluate(node, XPathConstants.STRING));
    float bearing = asFloat(bearingExpression.evaluate(node, XPathConstants.STRING));

    return toAvlReport(vehicleId, timestamp, lat, lon, speed, bearing, tripId);
  }

  private NodesAndTimestamp parseVehicles(URL url) throws Exception {
    List<Node> vehicles = new ArrayList<Node>();
    Document doc = builder.parse(url.toString());
    String recordedAtStr = (String)recordedAtExpression.evaluate(doc, XPathConstants.STRING);
    long timestamp = parseDate(recordedAtStr).getTime();
    NodeList nl = (NodeList) this.mvjExpression.evaluate(doc, XPathConstants.NODESET);
    if (nl ==null || nl.getLength() == 0) {
      logger.error("no nodes found");
      return new NodesAndTimestamp(vehicles, timestamp);
    }
    
    for (int i = 0; i < nl.getLength(); i++) {
      vehicles.add(nl.item(i));
    }
    return new NodesAndTimestamp(vehicles, timestamp);
  }

  private AvlReport toAvlReport(String vehicleId,
                                long timestamp,
                                double lat, double lon,
                                float speed,
                                float bearing,
                                String tripId) {
    AvlReport a = new AvlReport(vehicleId, timestamp, lat, lon, speed, bearing, "UTASIRI");
    a.setAssignment(tripId, AssignmentType.TRIP_ID);
    return a;
  }
  private String constructUrl(String route, String apiKey, String url) {
    return url + "?route=" + route + "&usertoken=" + apiKey;
  }

  public Date parseShortDate(String s) throws Exception {
    return ISO_DATE_SHORT_FORMAT.parse(s);
  }

  public Date parseDate(String s) throws Exception {
    int endPos = "yyyy-MM-ddTHH:mm:ss.SSS".length();
    // we can't convince Java's Simple Date to parse millisecond to 7 digit precision
    s = s.substring(0, endPos-1) + s.substring((s.length() - 7), s.length());
    
    return ISO_DATE_FORMAT.parse(s);
  }

  private double asDouble(Object obj) {
    String s = (String) obj;
    return Double.parseDouble(s);
  }

  private float asFloat(Object obj) {
    String s = (String) obj;
    return Float.parseFloat(s);
  }

  public static class NodesAndTimestamp {
    private List<Node> _nodes;
    private long _timestamp;
    public NodesAndTimestamp(List<Node> nodes, long timestamp) {
      this._nodes = nodes;
      this._timestamp = timestamp;
    }
    public List<Node> getNodes() {
      return _nodes;
    }
    public long getTimestamp() {
      return _timestamp;
    }
  }
  private void addRoutes() {
    _routeList.add("2X");
    _routeList.add("2");
    _routeList.add("320");
    _routeList.add("33");
    _routeList.add("240");
    _routeList.add("248");
    _routeList.add("228");
    _routeList.add("232");
    _routeList.add("307");
    _routeList.add("313");

    _routeList.add("3");
    _routeList.add("920");
    _routeList.add("919");
    _routeList.add("9");
    _routeList.add("863");
    _routeList.add("880");
    _routeList.add("850");
    _routeList.add("862");
    _routeList.add("35M");
    _routeList.add("39");
    _routeList.add("35");
    _routeList.add("354");
    _routeList.add("320");
    _routeList.add("33");
    _routeList.add("307");
    _routeList.add("313");
    _routeList.add("41");
    _routeList.add("45");
    _routeList.add("960");
    _routeList.add("920");
    _routeList.add("F522");
    _routeList.add("990");
    _routeList.add("F546");
    _routeList.add("F534");
    _routeList.add("F556");
    _routeList.add("F547");
    _routeList.add("F618");
    _routeList.add("F638");
    _routeList.add("451");
    _routeList.add("205");
    _routeList.add("455");
    _routeList.add("454");
    _routeList.add("453");
    _routeList.add("201");
    _routeList.add("462");
    _routeList.add("461");
    _routeList.add("460");
    _routeList.add("456");
    _routeList.add("47");
    _routeList.add("463");
    _routeList.add("248");
    _routeList.add("841");
    _routeList.add("842");
    _routeList.add("838");
    _routeList.add("840");
    _routeList.add("863");
    _routeList.add("9");
    _routeList.add("850");
    _routeList.add("862");
    _routeList.add("902");
    _routeList.add("919");
    _routeList.add("F590");
    _routeList.add("F638");
    _routeList.add("F618");
    _routeList.add("516");
    _routeList.add("519");
    _routeList.add("477");
    _routeList.add("500");
    _routeList.add("509");
    _routeList.add("513");
    _routeList.add("470");
    _routeList.add("471");
    _routeList.add("472");
    _routeList.add("473");
    _routeList.add("F94");
    _routeList.add("836");
    _routeList.add("835");
    _routeList.add("834");
    _routeList.add("833");
    _routeList.add("832");
    _routeList.add("831");
    _routeList.add("830");
    _routeList.add("822");
    _routeList.add("821");
    _routeList.add("811");
    _routeList.add("990");
    _routeList.add("667");
    _routeList.add("665");
    _routeList.add("F400");
    _routeList.add("645");
    _routeList.add("640");
    _routeList.add("664");
    _routeList.add("650");
    _routeList.add("626");
    _routeList.add("625");
    _routeList.add("630");
    _routeList.add("627");
    _routeList.add("F94");
    _routeList.add("608");
    _routeList.add("606");
    _routeList.add("525");
    _routeList.add("520");
    _routeList.add("54");
    _routeList.add("526");
    _routeList.add("6");
    _routeList.add("551");
    _routeList.add("604");
    _routeList.add("603");
    _routeList.add("806");
    _routeList.add("807");
    _routeList.add("701");
    _routeList.add("703");
    _routeList.add("704");
    _routeList.add("72");
    _routeList.add("720");
    _routeList.add("750");
    _routeList.add("805");
    _routeList.add("616");
    _routeList.add("62");
    _routeList.add("612");
    _routeList.add("613");
    _routeList.add("606");
    _routeList.add("608");
    _routeList.add("603");
    _routeList.add("604");
    _routeList.add("551");
    _routeList.add("6");
    _routeList.add("217");
    _routeList.add("213");
    _routeList.add("200");
    
    _routeList.add("17");
    _routeList.add("11");
    _routeList.add("21");
    _routeList.add("209");
    _routeList.add("205");
    _routeList.add("201");
    _routeList.add("F514");
    _routeList.add("640");
    _routeList.add("645");
    _routeList.add("F504");
    _routeList.add("616");
    _routeList.add("62");
    _routeList.add("612");
    _routeList.add("613");
    _routeList.add("627");
    _routeList.add("630");
    _routeList.add("625");
    _routeList.add("626");
    _routeList.add("F518");
    _routeList.add("902");
    _routeList.add("54");
    _routeList.add("526");
    _routeList.add("880");
    _routeList.add("901");
    _routeList.add("513");
    _routeList.add("509");
    _routeList.add("500");
    _routeList.add("477");
    _routeList.add("525");
    _routeList.add("520");
    _routeList.add("519");
    _routeList.add("516");
    _routeList.add("703");
    _routeList.add("F578");
    _routeList.add("675");
    _routeList.add("674");
    _routeList.add("667");
    _routeList.add("665");
    _routeList.add("664");
    _routeList.add("F570");
    _routeList.add("F402");
    _routeList.add("F401");
    _routeList.add("704");
    _routeList.add("675");
    _routeList.add("674");
    _routeList.add("223");
    _routeList.add("F400");
    _routeList.add("F402");
    _routeList.add("F401");
    _routeList.add("3");
    _routeList.add("2X");
    _routeList.add("72");
    _routeList.add("720");
    _routeList.add("750");
    _routeList.add("805");
    _routeList.add("806");
    _routeList.add("807");
    _routeList.add("811");
    _routeList.add("821");
    _routeList.add("822");
    _routeList.add("830");
    _routeList.add("962");
    _routeList.add("992");
    _routeList.add("240");
    _routeList.add("951");
    _routeList.add("952");
    _routeList.add("953");
    _routeList.add("954");
    _routeList.add("227");
    _routeList.add("223");
    _routeList.add("F504");
    _routeList.add("F514");
    _routeList.add("21");
    _routeList.add("209");
    _routeList.add("F518");
    _routeList.add("F522");
    _routeList.add("220");
    _routeList.add("218");
    _routeList.add("217");
    _routeList.add("213");
    _routeList.add("455");
    _routeList.add("454");
    _routeList.add("650");
    _routeList.add("354");
    _routeList.add("35");
    _routeList.add("39");
    _routeList.add("35M");
    _routeList.add("45");
    _routeList.add("41");
    _routeList.add("453");
    _routeList.add("451");
    _routeList.add("841");
    _routeList.add("840");
    _routeList.add("838");
    _routeList.add("832");
    _routeList.add("831");
    _routeList.add("834");
    _routeList.add("833");
    _routeList.add("232");
    _routeList.add("228");
    _routeList.add("11");
    _routeList.add("17");
    _routeList.add("220");
    _routeList.add("218");
    _routeList.add("227");
    _routeList.add("701");
    _routeList.add("472");
    _routeList.add("2");
    _routeList.add("200");
    _routeList.add("473");
    _routeList.add("463");
    _routeList.add("47");
    _routeList.add("470");
    _routeList.add("471");
    _routeList.add("456");
    _routeList.add("460");
    _routeList.add("461");
    _routeList.add("462");
    _routeList.add("F590");
    _routeList.add("F578");
    _routeList.add("F570");
    _routeList.add("F556");
    _routeList.add("F547");
    _routeList.add("F546");
    _routeList.add("F534");

  }
}
