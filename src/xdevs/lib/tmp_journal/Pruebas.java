package xdevs.lib.tmp_journal;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;

public class Pruebas {
  public static void main(String[] args) {
    ChiSquaredDistribution distribution = new ChiSquaredDistribution(2);
    distribution.reseedRandomGenerator(1234);
    double min = 1e6, max = -1e6, sum = 0;
    for(int i=0; i<10000; ++i) {
      double x = distribution.sample();
      min = (x<min) ? min = x : min;
      max = (x>max) ? max = x : max;
      sum += x;
      if(i%100==0) {
        System.out.println("x=" + x);
      }
    }
    System.out.println("[MIN, MAX]=[" + min + "," + max + "]");
    System.out.println("Total time (h) = " + sum/3600.0);
  }
}
