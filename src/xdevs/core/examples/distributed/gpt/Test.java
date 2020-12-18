/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.core.examples.distributed.gpt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import xdevs.core.examples.efp.Efp;
import xdevs.core.modeling.Coupled;
//import org.w3c.dom.Node;
/**
 *
 * @author Almendras
 */
public class Test {
    public static void main(String[] args) throws Exception {
        Coupled efp = new Efp("GPT", 1, 3, 100);
        Coupled newEfp = efp.flatten();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(File.separator + "tmp" + File.separator + "example.xml")))) {
            writer.write(newEfp.getDistributedModel());
        }
    } 
}
