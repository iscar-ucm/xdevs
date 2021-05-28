package xdevs.lib.tmp_journal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;

import xdevs.core.modeling.Component;
import xdevs.core.modeling.Coupled;
import xdevs.core.modeling.Coupling;
import xdevs.lib.performance.DevStone;
import xdevs.lib.performance.DevStoneAtomic;
import xdevs.lib.performance.DevStoneCoupledHO;
import xdevs.lib.performance.DevStoneGenerator;

public class XmlStoneGenerator {

  private static final Logger LOGGER = Logger.getLogger(XmlStoneGenerator.class.getName());

  private static final int MAX_EVENTS = 1;
  private static final double PREPARATION_TIME = 0.0;
  private static final double PERIOD = 1;

  protected DevStone.BenchmarkType model = null;
  protected Integer size = null;
  protected String delayDistribution = null;
  protected RealDistribution distribution = null;
  protected Long seed = null;

  protected Coupled framework = null;
  protected DevStoneGenerator generator = null;
  protected DevStone stone = null;

  public static void main(String[] args) {
    if (args.length == 0) {
      args = new String[] { "--model=HO", "--size=15", "--delay-distribution=Constant-2", "--seed=1234" };
    }
    XmlStoneGenerator test = new XmlStoneGenerator();
    for (String arg : args) {
      if (arg.startsWith("--model=")) {
        String[] parts = arg.split("=");
        test.model = DevStone.BenchmarkType.valueOf(parts[1]);
      } else if (arg.startsWith("--size=")) {
        String[] parts = arg.split("=");
        test.size = Integer.parseInt(parts[1]);
      } else if (arg.startsWith("--delay-distribution")) {
        String[] parts = arg.split("=");
        test.delayDistribution = parts[1];
      } else if (arg.startsWith("--seed=")) {
        String[] parts = arg.split("=");
        test.seed = Long.parseLong(parts[1]);
      }
    }
    test.init();
    test.buildFramework();
    try {
      test.toXML();
    } catch (IOException e) {
      LOGGER.severe(e.getLocalizedMessage());
    }
  }

  public void init() {
    String[] parts = delayDistribution.split("-");
    if (parts[0].equals("UniformRealDistribution")) {
      distribution = new UniformRealDistribution(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
    } else if (parts[0].equals("ChiSquaredDistribution")) {
      distribution = new ChiSquaredDistribution(Integer.parseInt(parts[1]));
    } else if (parts[0].equals("Constant")) {
      distribution = null;
      delayDistribution = parts[1];
    }
    if (distribution != null)
      distribution.reseedRandomGenerator(seed);
  }

  public void buildFramework() {
    framework = new Coupled("DevStone" + model.toString());
    generator = new DevStoneGenerator("Generator", PREPARATION_TIME, PERIOD, MAX_EVENTS);
    framework.addComponent(generator);
    switch (model) {
      case HO:
        stone = (distribution != null) ? new DevStoneCoupledHO("C", size, size, PREPARATION_TIME, distribution)
            : new DevStoneCoupledHO("C", size, size, PREPARATION_TIME, Double.parseDouble(delayDistribution),
                Double.parseDouble(delayDistribution));
        break;
      default:
        LOGGER.severe("Right now, only HO model is supported.");
        return;
    }
    framework.addComponent(stone);
    framework.addCoupling(generator.oOut, stone.iIn);
    switch (model) {
      case HO:
        framework.addCoupling(generator.oOut, ((DevStoneCoupledHO) stone).iInAux);
        break;
      default:
        LOGGER.severe("Right now, only HO model is supported.");
        return;
    }
  }

  public void toXML() throws IOException {
    Coupled frameworkFlattened = framework.flatten();
    String fileContent = getXmlModel(frameworkFlattened, false);
    BufferedWriter writer = new BufferedWriter(
        new FileWriter(new File(model + "_s-" + size + "_t-" + delayDistribution + ".xml")));
    writer.write(fileContent);
    writer.flush();
    writer.close();
  }

  public String getXmlModel(Coupled coupled, boolean homogeneous) {
    StringBuilder builder = new StringBuilder();
    builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
    builder.append("<coupled name=\"" + coupled.getName() + "\"");
    builder.append(" class=\"").append(this.getClass().getCanonicalName()).append("\"");
    if (homogeneous) {
      builder.append(" host=\"host-service\"");
    } else {
      builder.append(" host=\"" + coupled.getName() + "-service\"");
    }
    builder.append(" mainPort=\"5000\"");
    builder.append(" auxPort=\"6000\"");
    builder.append(">\n");
    int counter = 1;
    Collection<Component> components = coupled.getComponents();
    for (Component component : components) {
      if (component instanceof Coupled) {
        LOGGER.severe(
            "ERROR: This models should not have coupled models (" + component.getName() + " is a Coupled model).");
      } else {
        builder.append("\t<atomic name=\"").append(component.getName()).append("\"");
        builder.append(" class=\"").append(component.getClass().getCanonicalName()).append("\"");
        if (homogeneous) {
          builder.append(" host=\"host-service\"");
        } else {
          builder.append(" host=\"" + component.getName() + "-service\"");
        }
        builder.append(" mainPort=\"").append(5000 + counter).append("\"");
        builder.append(" auxPort=\"").append(6000 + counter).append("\"");
        builder.append(">\n");
      }
      if (component instanceof DevStoneGenerator) {
        DevStoneGenerator atomic = (DevStoneGenerator) component;
        builder.append("\t\t<constructor-arg value=\"" + atomic.getPreparationTime() + "\"/>\n");
        builder.append("\t\t<constructor-arg value=\"" + atomic.getPeriod() + "\"/>\n");
        builder.append("\t\t<constructor-arg value=\"" + atomic.getMaxEvents() + "\"/>\n");
      } else if (component instanceof DevStoneAtomic) {
        DevStoneAtomic atomic = (DevStoneAtomic) component;
        builder.append("\t\t<constructor-arg value=\"" + atomic.getPreparationTime() + "\"/>\n");
        builder.append("\t\t<constructor-arg value=\"" + atomic.getIntDelayTime() + "\"/>\n");
        builder.append("\t\t<constructor-arg value=\"" + atomic.getExtDelayTime() + "\"/>\n");
      }
      builder.append("\t</atomic>\n");
      counter = (homogeneous) ? counter : counter + 1;
    }
    // Couplings
    LinkedList<Coupling<?>> couplings = coupled.getIC();
    couplings.forEach((coupling) -> {
      builder.append("\t<connection");
      builder.append(" componentFrom=\"").append(coupling.getPortFrom().getParent().getName()).append("\"");
      builder.append(" classFrom=\"").append(coupling.getPortFrom().getParent().getClass().getCanonicalName())
          .append("\"");
      builder.append(" portFrom=\"").append(coupling.getPortFrom().getName()).append("\"");
      builder.append(" componentTo=\"").append(coupling.getPortTo().getParent().getName()).append("\"");
      builder.append(" classTo=\"").append(coupling.getPortTo().getParent().getClass().getCanonicalName()).append("\"");
      builder.append(" portTo=\"").append(coupling.getPortTo().getName()).append("\"");
      builder.append("/>\n");
    });
    builder.append("</coupled>\n");
    return builder.toString();
  }
}
