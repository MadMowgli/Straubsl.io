package SandBox;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

public class SimpleSVD {

    public static void main(String[] args) {

        // Create a simple matrix
        double[][] values = new double[5][5];
        for(int i = 0; i < 5; i++) {
            for(int j = 0; j < 5; j++) {
                values[i][j] = Math.floor(Math.random() * 10);
            }
        }

        Matrix simpleMatrix = new Matrix(values);
        SingularValueDecomposition simpleSVD = simpleMatrix.svd();
        System.out.println();
    }




}
