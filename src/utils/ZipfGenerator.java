/*
 *  Copyright (C) 2010 A.A.Jamal
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * ZipfGenerator.java
 */

package utils;

import java.util.Random;
/**
 *
 * @author Hyunsik Choi 
 * http://diveintodata.org/2009/09/zipf-distribution-generator-in-java/ 
 * April 2011
 */

public class ZipfGenerator {
    private Random rand = new Random(System.currentTimeMillis());
    private int size;
    private double skew;
    private double bottom = 0.0;

    public ZipfGenerator(int size, double skew){
        this.size = size;
        this.skew = skew;

        for (int i=1; i<size; i++){
            this.bottom += (1/Math.pow(i, this.skew));
        }
    }

    public int next(){
        int rank;
        double frequency = 0;
        double dice;

        rank = rand.nextInt(size);
        frequency = (1.0d / Math.pow(rank, this.skew)) / this.bottom;
        dice =  rand.nextDouble();

        while(!(dice < frequency)){
            rank = rand.nextInt(size);
            frequency = (1.0d / Math.pow(rank, this.skew)) / this.bottom;
            dice = rand.nextDouble();
        }
        return rank;
    }

    public double getProbability(int rank){
        return(1.0d / Math.pow(rank, this.skew)) / this.bottom;
    }


    public static void main(String[] args) {
//        if(args.length != 2) {
//            System.out.println("usage: ./zipf size skew");
//            System.exit(-1);
//        }

        double total=0.0;
        double zipfRet;

        ZipfGenerator zipf = new ZipfGenerator(1000, 10);

//        Output.openOutput("zipf.csv");
        for(int i=1; i<=100; i++){
            zipfRet = zipf.getProbability(i);
            System.out.println(i+" " + zipfRet);
//            Output.writeZipfOutput(i, zipfRet);

            total = total + zipfRet;
        }
//        Output.closeOutput();

        System.out.println(total);
    }
}
