package com.idm.seventh;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.bson.Document;

import java.util.*;

public class KMeans {

    public static String mongoURL;
    public static String mongoDatabase;
    public static String mongoCollection;
    public static String kValue;
    public static String maxEpochsValue;

    // None test C 3 10

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        if (args.length > 0) {
            mongoURL = args[0];
            mongoDatabase = args[1];
            mongoCollection = args[2];
            kValue = args[3];
            maxEpochsValue = args[4];
        }

        int k = Integer.parseInt(kValue);
        int maxEpochs = Integer.parseInt(maxEpochsValue);

        MongoClient mongoClient = getClient(mongoURL);
        MongoDatabase db = mongoClient.getDatabase(mongoDatabase);
        MongoCollection<Document> collection = db.getCollection(mongoCollection);

        List<Double> firstPointDims = (List<Double>) collection.find().first().get("point");
        int n = firstPointDims.size();
        double[] maxDim = new double[n];
        for(int i=0; i<n; i++) {
            Document dimOfI = collection.aggregate(Collections.singletonList(
                    new Document("$sort", new Document("point." + i, -1))
            )).first();
            List<Double> iDims = (List<Double>) dimOfI.get("point");
            double maxValue = iDims.get(i);
            maxDim[i] = maxValue;
        }
        double[] minDim = new double[n];
        for(int i=0; i<n; i++) {
            Document dimOfI = collection.aggregate(Collections.singletonList(
                    new Document("$sort", new Document("point." + i, 1))
            )).first();
            List<Double> iDims = (List<Double>) dimOfI.get("point");
            double minValue = iDims.get(i);
            minDim[i] = minValue;
        }

        List<double[]> centroids = new ArrayList<>();
        for(int i=0; i<k; i++) {
            double[] ci = generateRandomCentroid(n, maxDim, minDim);
            centroids.add(ci);
        }
//        System.out.println("Random initial Centroids");
//        for(double[] c : centroids){
//            System.out.println(Arrays.toString(c));
//        }
        while(maxEpochs>0){
            // System.out.println("New iteration "+maxEpochs);

            // For each point find Euclidean distance to each centroid
            // Add label in document of the nearest centroid
            // Check if value matches to any centroid
            FindIterable<Document> points= collection.find().batchSize(1000);
            for (Document poi:points){
                List<Double> pointDims = (List<Double>) poi.get("point");
                List<Double> centroidDistance = new ArrayList<>();
                for(double[] ci : centroids){
                    centroidDistance.add(euclideanDistance(pointDims, ci));
                }
                int label = centroidDistance.indexOf(Collections.min(centroidDistance));
                //boolean isCentroid = checkIfCentroid(poiDims, centroids.get(label));
                Document labelAttr = new Document().append("$set", new Document().append("label", label));
                Document isCentroidAttr = new Document().append("$set", new Document().append("isCentroid", false));
                collection.updateOne(poi, labelAttr);
                collection.updateOne(poi, isCentroidAttr);
            }

            // Geometric mean of centroids
            // Reassign centroids
            List<double[]> oldCentroids = List.copyOf(centroids);
            for(int i=0; i<k; i++) {
                FindIterable<Document> iLabelPoints= collection.find(Filters.eq("label", i)).batchSize(1000);
                if(iLabelPoints.first() != null) {
                    for (int j = 0; j < n; j++) {
                        DescriptiveStatistics ds = new DescriptiveStatistics();
                        for (Document p : iLabelPoints) {
                            ds.addValue(((List<Double>) p.get("point")).get(j));
                        }
                        double newCentroidDimJ = ds.getGeometricMean();
                        double[] newCentroid = centroids.get(i);
                        newCentroid[j] = newCentroidDimJ;
                        centroids.set(i, newCentroid);
                    }
                }else{
                    centroids.set(i, generateRandomCentroid(n, maxDim, minDim));
                }
            }
            if(centroids.equals(oldCentroids)){
                break;
            }
            maxEpochs--;
        }
        //System.out.println("New Centroids"+maxEpochs);
        double sse = getSSE(collection, centroids);
        for(int c=0; c<centroids.size(); c++){
            //System.out.println(Arrays.toString(centroids.get(c)));
            Document newDoc = new Document();
            List<Double> l = new ArrayList<>();
            for(double cenDim : centroids.get(c)){
                l.add(cenDim);
            }
            newDoc.append("point", l);
            newDoc.append("label", c);
            newDoc.append("isCentroid", true);
            if(c==0){
                newDoc.append("sse", sse);
            }
            collection.insertOne(newDoc);
        }
        System.out.println("Entire process took : "+(System.currentTimeMillis()-start)/60000);
    }

    private static double getSSE(MongoCollection<Document> collection, List<double[]> centroids) {
        double sse = 0;
        for(int c=0; c<centroids.size(); c++){
            FindIterable<Document> iLabelPoints= collection.find(Filters.eq("label", c)).batchSize(1000);
            if(iLabelPoints.first() != null) {
                for (Document poi : iLabelPoints) {
                    List<Double> pointDims = (List<Double>) poi.get("point");
                    double d = euclideanDistance(pointDims, centroids.get(c));
                    sse = sse + Math.pow(d, 2);
                }
            }
        }
        return sse;
    }

    private static double euclideanDistance(List<Double> poiDims, double[] ci) {
        double sumOfSquares = 0;
        for (int i=0; i<poiDims.size(); i++){
            sumOfSquares = sumOfSquares + Math.pow(ci[i]-poiDims.get(i), 2);
        }
        return Math.sqrt(sumOfSquares);
    }

    private static double[] generateRandomCentroid(int n, double[] maxDim, double[] minDim) {
        double[] ci = new double[n];
        for(int j=0; j<n; j++) {
            ci[j] = minDim[j] + (maxDim[j]-minDim[j]) * Math.random();
        }
        return ci;
    }

    private static MongoClient getClient(String u) {
        MongoClient client = null;
        if (u.equals("None"))
            client = new MongoClient();
        else
            client = new MongoClient(new MongoClientURI(u));
        return client;
    }
}
