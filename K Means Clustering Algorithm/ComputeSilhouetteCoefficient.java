package com.idm.seventh;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.Arrays;
import java.util.List;

public class ComputeSilhouetteCoefficient {

    public static String mongoURL;
    public static String mongoDatabase;
    public static String mongoCollection;

    // None test C

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        if (args.length > 0) {
            mongoURL = args[0];
            mongoDatabase = args[1];
            mongoCollection = args[2];
        }

        MongoClient mongoClient = getClient(mongoURL);
        MongoDatabase db = mongoClient.getDatabase(mongoDatabase);
        MongoCollection<Document> collection = db.getCollection(mongoCollection);

        //db.C.aggregate([
        //{$match: {isCentroid : false}},
        //{$group: { _id : "$label", members : { $addToSet : "$point"}}}
        //])
        AggregateIterable<Document> allClusters = collection.aggregate(Arrays.asList(
                new Document("$match", new Document("isCentroid", false)),
                new Document("$group", new Document("_id", "$label")
                                           .append("members", new Document("$addToSet", "$point")))
        )).batchSize(1000);

        FindIterable<Document> allPoints= collection.find(Filters.eq("isCentroid", false)).batchSize(1000);
        for(Document point:allPoints){
            int label = (int) point.get("label");
            double bi = Double.MAX_VALUE;
            double ai = 0.0;
            for(Document cluster:allClusters){
                if((int) cluster.get("_id") != label){
                    double di = getAverageDistanceFromClusterPoints(cluster, point);
                    if(di<bi)
                       bi = di;
                }else{
                    ai = getAverageDistanceFromClusterPoints(cluster, point);
                }
            }
            double si = (bi - ai) / Math.max(ai, bi);
            Document sAttr = new Document().append("$set", new Document().append("S", si));
            collection.updateOne(point, sAttr);
        }
        System.out.println("Entire process took : "+(System.currentTimeMillis()-start)/60000);
    }

    private static double getAverageDistanceFromClusterPoints(Document cluster, Document point) {
        List<Double> pointDims = (List<Double>) point.get("point");
        double sumOfDistances = 0;
        double numberOfPoints = 0;
        List<List<Double>> clusterPoints = (List<List<Double>>) cluster.get("members");
        for(List<Double> cPoint:clusterPoints){
            sumOfDistances = sumOfDistances + euclideanDistance(pointDims, cPoint);
            numberOfPoints++;
        }
        return sumOfDistances/numberOfPoints;
    }

    private static double euclideanDistance(List<Double> poiDims, List<Double> ci) {
        double sumOfSquares = 0;
        for (int i=0; i<poiDims.size(); i++){
            sumOfSquares = sumOfSquares + Math.pow(ci.get(i)-poiDims.get(i), 2);
        }
        return Math.sqrt(sumOfSquares);
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
