package xdevs.lib.tmp_journal;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import xdevs.core.modeling.Coupled;
import xdevs.core.simulation.Coordinator;
import xdevs.core.util.DevsLogger;
import xdevs.lib.performance.DevStoneAtomic;

public class DevStoneXml extends Coupled {

  private static final Logger LOGGER = Logger.getLogger(DevStoneXml.class.getName());

  public DevStoneXml(Element xmlCoupled) {
    super(xmlCoupled);
  }

  public static void main(String args[]) {
    Element xmlCoupled = null;
    try {
      DevsLogger.setup(Level.INFO);
      File file = new File("HO_s-5_t-1.xml");
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setValidating(false);
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document docApplication = builder.parse(file.toURI().toString());
      xmlCoupled = (Element) docApplication.getElementsByTagName("coupled").item(0);

    } catch (IOException | ParserConfigurationException | SAXException ex) {
      LOGGER.severe(ex.getLocalizedMessage());
    }
    DevStoneAtomic.NUM_DELT_INTS = 0;
    DevStoneAtomic.NUM_DELT_EXTS = 0;
    DevStoneAtomic.NUM_OF_EVENTS = 0;

    DevStoneXml model = new DevStoneXml(xmlCoupled);
    Coordinator coordinator = new Coordinator(model);
    coordinator.initialize();
    long simBegin = System.currentTimeMillis();
    coordinator.simulate(Long.MAX_VALUE);
    long simEnds = System.currentTimeMillis();
    double simTime = (simEnds - simBegin) / 1e3;
    coordinator.exit();
    model.logReport(simTime);
  }

  public void logReport(double simTime) {
    StringBuilder stats = new StringBuilder();
    stats.append("\n");
    stats.append("-------------------------------------------------------------\n");
    stats.append("MODEL=HO\n");
    stats.append("NUM_ATOMICS=").append(this.countAtomicComponents()).append("\n");
    stats.append("NUM_DELT_INTS=").append(DevStoneAtomic.NUM_DELT_INTS).append("\n");
    stats.append("NUM_DELT_EXTS=").append(DevStoneAtomic.NUM_DELT_EXTS).append("\n");
    stats.append("NUM_OF_EVENTS=").append(DevStoneAtomic.NUM_OF_EVENTS).append("\n");
    stats.append("SIM_TIME (s)=").append(simTime).append("\n");
    stats.append("-------------------------------------------------------------\n");

    LOGGER.info(stats.toString());
}

}
