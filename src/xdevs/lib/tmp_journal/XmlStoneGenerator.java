package xdevs.lib.tmp_journal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;

import org.apache.commons.math3.util.Pair;
import xdevs.core.modeling.Component;
import xdevs.core.modeling.Coupled;
import xdevs.core.modeling.Coupling;
import xdevs.lib.performance.*;

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

  protected Integer numFastPods = null;
  protected Integer numSlowPods = null;
  protected Float fastPodsAtomicProportion = null;

  public static void main(String[] args) {
    if (args.length == 0) {
      //args = new String[] { "--model=HO", "--size=15", "--delay-distribution=Constant-2", "--seed=1234" };
      args = new String[] { "--model=HO", "--size=4", "--delay-distribution=UniformRealDistribution-0-1", "--seed=1234", "--pods=2-2"};
      //args = new String[] { "--model=HO", "--size=15", "--delay-distribution=ChiSquaredDistribution-2", "--seed=1234", "--pods=3-1"}; //
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
      } else if (arg.startsWith("--pods=")) {
        String[] parts = arg.split("=");
        parts = parts[1].split("-");
        test.numFastPods = Integer.parseInt(parts[0]);
        test.numSlowPods = Integer.parseInt(parts[1]);
        if(parts.length > 2) {
          test.fastPodsAtomicProportion = Float.parseFloat(parts[2]);
        } else {
          test.fastPodsAtomicProportion = (float) test.numFastPods / (test.numSlowPods + test.numFastPods);
        }
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
    switch (parts[0]) {
      case "UniformRealDistribution":
        distribution = new UniformRealDistribution(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        break;
      case "ChiSquaredDistribution":
        distribution = new ChiSquaredDistribution(Integer.parseInt(parts[1]));
        break;
      case "Constant":
        distribution = null;
        delayDistribution = parts[1];
        break;
    }
    if (distribution != null)
      distribution.reseedRandomGenerator(seed);
  }

  public void buildFramework() {
    framework = new Coupled("devstone-" + model.toString().toLowerCase());
    generator = new DevStoneGenerator("generator", PREPARATION_TIME, PERIOD, MAX_EVENTS);
    framework.addComponent(generator);
    switch (model) {
      case LI:
        stone = (distribution != null) ? new DevStoneCoupledLI("C", size, size, PREPARATION_TIME, distribution)
                : new DevStoneCoupledLI("C", size, size, PREPARATION_TIME, Double.parseDouble(delayDistribution),
                Double.parseDouble(delayDistribution));
        break;
      case HI:
        stone = (distribution != null) ? new DevStoneCoupledHI("C", size, size, PREPARATION_TIME, distribution)
                : new DevStoneCoupledHI("C", size, size, PREPARATION_TIME, Double.parseDouble(delayDistribution),
                Double.parseDouble(delayDistribution));
        break;
      case HO:
        stone = (distribution != null) ? new DevStoneCoupledHO("C", size, size, PREPARATION_TIME, distribution)
            : new DevStoneCoupledHO("C", size, size, PREPARATION_TIME, Double.parseDouble(delayDistribution),
                Double.parseDouble(delayDistribution));
        break;
      case HOmod:
        stone = (distribution != null) ? new DevStoneCoupledHOmod("C", size, size, PREPARATION_TIME, distribution)
            : new DevStoneCoupledHOmod("C", size, size, PREPARATION_TIME, Double.parseDouble(delayDistribution),
                Double.parseDouble(delayDistribution));
        break;
      default:
        LOGGER.severe("Unsupported model.");
        return;
    }
    framework.addComponent(stone);
    framework.addCoupling(generator.oOut, stone.iIn);
    if (model == DevStone.BenchmarkType.HO) {
      framework.addCoupling(generator.oOut, ((DevStoneCoupledHO) stone).iInAux);
    } else if (model == DevStone.BenchmarkType.HOmod) {
      framework.addCoupling(generator.oOut, ((DevStoneCoupledHOmod) stone).iInAux);
    }
  }

  public void toXML() throws IOException {
    Coupled frameworkFlattened = framework.flatten();
    String fileContent = getXmlModel(frameworkFlattened, false);
    String fn = model + "_s-" + size + "_t-" + delayDistribution;
    if (numFastPods != null && numSlowPods != null) {
      fn += "_p" + numFastPods + "-" + numSlowPods;
    }
    fn += ".xml";
    // System.out.println(fn);
    BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fn)));
    writer.write(fileContent);
    writer.flush();
    writer.close();
  }

  public String getXmlModel(Coupled coupled, boolean homogeneous) {
    Map<String, String> atomicToPod = null;
    StringBuilder builder = new StringBuilder();
    String firstPod = "";
    boolean usingPods = numFastPods != null && numSlowPods != null;

    if (usingPods) {
      atomicToPod = classifyAtomics(coupled);
      firstPod = (numFastPods > 0)?"fast0":"slow0";
    }

    builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
    builder.append("<coupled name=\"").append(coupled.getName()).append("\"");
    builder.append(" class=\"").append(this.getClass().getCanonicalName()).append("\"");
    if (homogeneous) {
      builder.append(" host=\"host-service\"");
    } else if (usingPods) {
      builder.append(" host=\"").append(firstPod).append("\"");
    } else {
      builder.append(" host=\"").append(coupled.getName().replace("_", "-").toLowerCase()).append("\"");
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
        } else if (atomicToPod != null && atomicToPod.containsKey(component.getName())) {
          builder.append(" host=\"").append(atomicToPod.get(component.getName())).append("\"");
        } else if (usingPods) {
          builder.append(" host=\"").append(firstPod).append("\"");
        } else {
          builder.append(" host=\"").append(component.getName().replace("_", "-").toLowerCase()).append("\"");
        }
        builder.append(" mainPort=\"").append(5000 + counter).append("\"");
        builder.append(" auxPort=\"").append(6000 + counter).append("\"");
        builder.append(">\n");
      }
      if (component instanceof DevStoneGenerator) {
        DevStoneGenerator atomic = (DevStoneGenerator) component;
        builder.append("\t\t<constructor-arg value=\"").append(atomic.getPreparationTime()).append("\"/>\n");
        builder.append("\t\t<constructor-arg value=\"").append(atomic.getPeriod()).append("\"/>\n");
        builder.append("\t\t<constructor-arg value=\"").append(atomic.getMaxEvents()).append("\"/>\n");
      } else if (component instanceof DevStoneAtomic) {
        DevStoneAtomic atomic = (DevStoneAtomic) component;
        builder.append("\t\t<constructor-arg value=\"").append(atomic.getPreparationTime()).append("\"/>\n");
        builder.append("\t\t<constructor-arg value=\"").append(atomic.getIntDelayTime()).append("\"/>\n");
        builder.append("\t\t<constructor-arg value=\"").append(atomic.getExtDelayTime()).append("\"/>\n");
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

  private Map<String, String> classifyAtomics(Coupled framework) {
    List<Pair<Double, String>> delays = new LinkedList<>();
    double currDelay;
    DevStoneAtomic atomic;
    int repetitions;

    for(Component c: framework.getComponents()) {
      if(c instanceof DevStoneAtomic) {
        atomic = (DevStoneAtomic) c;
        if (model == DevStone.BenchmarkType.HI || model == DevStone.BenchmarkType.HO) {
          repetitions = Integer.parseInt(atomic.getName().substring(1, atomic.getName().indexOf("_")));
        } else if (model == DevStone.BenchmarkType.HOmod) {
          // System.out.println(atomic.getName());
          if (atomic.getName().equals("A1_C0")) {
            repetitions = 1;
          } else {
            int aux = atomic.getName().indexOf("_");
            int row = Integer.parseInt(atomic.getName().substring(2, aux));
            int col = Integer.parseInt(atomic.getName().substring(aux + 1, atomic.getName().indexOf("_", aux + 1)));
            if (row == 1) {
              repetitions = size + col - 1;
            } else {
              repetitions = (col == 1)?1:(col - 1);
            }
          }
        } else {
          repetitions = 1;
        }
        currDelay = repetitions * (atomic.getIntDelayTime() + atomic.getExtDelayTime());
        delays.add(new Pair<>(currDelay, c.getName()));
      } else if (!(c instanceof DevStoneGenerator)) {
        throw new RuntimeException("Invalid component found in framework on XML generation.");
      }
    }

    delays.sort((p1, p2) -> {
      if(p1.getFirst().equals(p2.getFirst())) return 0;
      else if(p1.getFirst() > p2.getFirst()) return 1;
      else return -1;
    });

    Map<String, String> atomicToPod = new HashMap<>();
    int slowPodIdx = 0;
    int fastPodIdx = 0;
    int numFastAtomics = (int)(delays.size() * fastPodsAtomicProportion);

    for(int i=0; i<delays.size(); i++) {
      Pair<Double, String> pair = delays.get(i);

      if(i < numFastAtomics) {  // Fast pod
        atomicToPod.put(pair.getSecond(), "fast" + fastPodIdx);
//        System.out.println(pair.getSecond() + " -> Fast" + fastPodIdx + "-service");
        fastPodIdx += 1;
        if(fastPodIdx >= numFastPods)
          fastPodIdx = 0;
      } else {  // Slow pod
        atomicToPod.put(pair.getSecond(), "slow" + slowPodIdx);
//        System.out.println(pair.getSecond() + " -> Slow" + slowPodIdx + "-service");
        slowPodIdx += 1;
        if(slowPodIdx >= numSlowPods)
          slowPodIdx = 0;
      }
    }

    return atomicToPod;
  }
}
