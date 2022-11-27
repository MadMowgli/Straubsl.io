package SearchEngine;

import Jama.Matrix;
import PreProcessor.Configuration.ConfigurationManager;
import PreProcessor.Models.TermSet;
import Util.LogFormatter;
import Util.MatrixManager;
import Util.PerformanceTimer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

@SpringBootApplication
public class SpringSearchApplication {



    public static void main(String[] args) {
        SpringApplication.run(SpringSearchApplication.class, args);
    }



}
