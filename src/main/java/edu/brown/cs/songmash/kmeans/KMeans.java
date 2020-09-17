package edu.brown.cs.songmash.kmeans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KMeans<E extends  KMeanable> {

    private List<E> items;
    private int clusters;
    private int iterations;
    private List<double[]> means;

    private Map<E, Integer> belongsTo;
    private List<Integer> clusterSizes;

    public KMeans(List<E> items, int clusters, int iterations){
        this.items = items;
        this.clusters = clusters;
        this.iterations = iterations;
        this.means = initializeMeans();

        belongsTo = new HashMap<>();
        clusterSizes = new ArrayList<>();

        for(int i = 0; i < clusters; i++){
            clusterSizes.add(0);
        }
    }

    public void calculateMeans(){
        for(int i = 0; i < iterations; i++){
            for(E e : items){
                int index = classify(e);
                clusterSizes.set(index, clusterSizes.get(index) + 1);
                means.set(index, updateMean(means.get(index), clusterSizes.get(index), e));
            }
        }
    }

    public void findClusters(){
        for(E e : items){
            int index = classify(e);
            belongsTo.compute(e, (k,v) -> index);
        }
    }

    public E getRandom(int cluster){
        //TODO for suggestion algorithm
        for(E e : belongsTo.keySet()){

        }
        return null;
    }

    private List<double[]> initializeMeans(){
        List<double[]> means = new ArrayList<>();

        double[] min = findMinMatrix();
        double[] max = findMaxMatrix();

        for(int i = 0; i < clusters; i++){
            double[] mean = new double[items.get(0).getDimensions().length];
            for(int j = 0; j < mean.length; j++){
                double smallest = min[j];
                double range = max[j] - min[j] + 1;
                mean[j] = (Math.random() * range) + smallest;
            }
            means.add(mean);
        }
        return means;
    }

    private double[] updateMean(double[] mean, int n, E e){
        double[] newMean = new double[mean.length];
        for(int i = 0; i < mean.length; i++){
            double m = mean[i];
            m = (m * (n - 1) + e.getDimensions()[i]) / n;
            newMean[i] = Math.round(1000 * m) / 1000.0;
        }
        return newMean;
    }

    public int classify(E e){
        double min = Double.MAX_VALUE;
        int index = -1;

        for(int i = 0; i < means.size(); i++){
            double dist = getEuclideanDistance(e.getDimensions(), means.get(i));

            if(dist < min){
                min = dist;
                index = i;
            }
        }

        return index;
    }

    private double[] findMinMatrix(){
        double[] min = new double[items.get(0).getDimensions().length];
        for(int i = 0; i < min.length; i++){
            min[i] = Double.MAX_VALUE;
        }

        for(E e : items){
            for(int i = 0; i < min.length; i++){
                if(e.getDimensions()[i] < min[i]){
                    min[i] = e.getDimensions()[i];
                }
            }
        }

        return min;
    }

    private double[] findMaxMatrix(){
        double[] max = new double[items.get(0).getDimensions().length];
        for(int i = 0; i < max.length; i++){
            max[i] = Double.MIN_VALUE;
        }

        for(E e : items){
            for(int i = 0; i < max.length; i++){
                if(e.getDimensions()[i] > max[i]){
                    max[i] = e.getDimensions()[i];
                }
            }
        }

        return max;
    }

    private double getEuclideanDistance(double[] p1, double[] p2){
        double sum = 0;
        for(int i = 0; i < p1.length; i++){
            sum += Math.pow(p1[i] - p2[i], 2);
        }
        return Math.sqrt(sum);
    }

    private double getCosineDistance(double[] p1, double[] p2) {
        double dot = 0;
        double magA = 0;
        double magB = 0;

        for (int i = 0; i < p1.length; i++) {
            dot = p1[i] * p2[i];
            magA = p1[i] * p1[i];
            magB = p2[i] * p2[i];
        }

        return dot / (Math.sqrt(magA * magB));
    }

    public Map<E, Integer> getResults(){
        return belongsTo;
    }

}
