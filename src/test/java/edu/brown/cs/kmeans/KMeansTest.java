package edu.brown.cs.kmeans;

import org.junit.Test;

import edu.brown.cs.songmash.kmeans.KMeanable;
import edu.brown.cs.songmash.kmeans.KMeans;

import java.util.*;

import static org.junit.Assert.*;

public class KMeansTest {

    @Test
    public void baseTest(){
        List<Point> points = new ArrayList<>();

        double[] p1 = {1.0, 2.0, 3.0};
        double[] p2 = {3.0, 4.0, 5.0};

        points.add(new Point(p1));
        points.add(new Point(p2));

        KMeans<Point> kMeans = new KMeans<>(points, 1, 10);
        kMeans.calculateMeans();
        kMeans.findClusters();

        for(int i = 0; i < points.size(); i++){
            assertEquals(kMeans.getResults().get(points.get(i)), 0, 0);
            assertNotEquals(kMeans.getResults().get(points.get(i)), 1, 0);
        }
    }

    @Test
    public void twoClusterTest(){
        List<Point> points = new ArrayList<>();

        double[] p1 = {-10, -10, -10};
        double[] p2 = {20, 20, 20};

        points.add(new Point(p1));
        points.add(new Point(p2));

        KMeans<Point> kMeans = new KMeans<>(points, 2, 1000);
        kMeans.calculateMeans();
        kMeans.findClusters();

        int result1 = kMeans.getResults().get(points.get(0));
        int result2 = kMeans.getResults().get(points.get(1));

        if(result1 == 0){
            assertEquals(result2, 1, 0);
            assertNotEquals(result2, 0, 0);
        }else{
            assertEquals(result2, 0, 0);
            assertNotEquals(result2, 1, 0);
        }

        assertNotEquals(result1, 2, 0);
        assertNotEquals(result2, 2, 0);
    }

    @Test
    public void twoCluster3PointTest(){
        List<Point> points = new ArrayList<>();

        //p1 and p2 are close to eachother
        double[] p1 = {1, 2, 3};
        double[] p2 = {1.1, 2.1, 3.1};
        double[] p3 = {19, 29, 39};

        points.add(new Point(p1));
        points.add(new Point(p3));
        points.add(new Point(p2));

        KMeans<Point> kMeans = new KMeans<>(points, 2, 1000);
        kMeans.calculateMeans();
        kMeans.findClusters();

        int result1 = kMeans.getResults().get(points.get(0));
        int result3 = kMeans.getResults().get(points.get(1));
        int result2 = kMeans.getResults().get(points.get(2));

        assertEquals(result1, result2);
        assertNotEquals(result1, result3);
        assertNotEquals(result2, result3);
    }

    @Test
    public void multipleClustersUsedTest(){
        List<Point> points = new ArrayList<>();
        for(int i = 0; i < 100; i++){
            double[] p = new double[3];
            p[0] = Math.random() * 10;
            p[1] = Math.random() * 10;
            p[2] = Math.random() * 10;
            points.add(new Point(p));
        }

        KMeans<Point> kMeans = new KMeans<>(points, 10, 3000);
        kMeans.calculateMeans();
        kMeans.findClusters();

        Set<Integer> clusterNumber = new HashSet<>();
        Set<Integer> ans = new HashSet<>();
        for(int i = 0; i < 10; i++){
            ans.add(i);
        }

        assertEquals(kMeans.getResults().size(), 100);
        for(Point p : kMeans.getResults().keySet()){
            int c = kMeans.getResults().get(p);
            clusterNumber.add(c);
            assertTrue(c < 10);
        }
        assertEquals(clusterNumber, ans);
    }
}

class Point implements KMeanable {

    private double[] dim;

    public Point(double[] dim){
        this.dim = dim;
    }

    @Override
    public double[] getDimensions() {
        return dim;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return Arrays.equals(dim, point.dim);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(dim);
    }
}
