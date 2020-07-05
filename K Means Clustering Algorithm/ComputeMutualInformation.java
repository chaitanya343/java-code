package com.idm.seventh;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ComputeMutualInformation {

    public static String mongoURL;
    public static String mongoDatabase;
    public static String mongoCollection;
    public static String outputFile;

    // None test C outputFile

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        if (args.length > 0) {
            mongoURL = args[0];
            mongoDatabase = args[1];
            mongoCollection = args[2];
            outputFile = args[3];
        }

        MongoClient mongoClient = getClient(mongoURL);
        MongoDatabase db = mongoClient.getDatabase(mongoDatabase);
        MongoCollection<Document> collection = db.getCollection(mongoCollection);
        
        //db.C.aggregate([
        //{$match: {isCentroid : false}},
        //{$group: { _id : "$label", memberIds : { $addToSet : "$_id"}, count : {$sum : 1}}},
        //{$sort : { _id : 1 } }
        //])
        List<Integer> clusterMemberCounts = new ArrayList<>();
        List<List<Integer>> clusterMemberIds = new ArrayList<>();
        AggregateIterable<Document> allClusters = collection.aggregate(Arrays.asList(
                new Document("$match", new Document("isCentroid", false)),
                new Document("$group", new Document("_id", "$label")
                        .append("memberIds", new Document("$addToSet", "$_id"))
                        .append("count", new Document("$sum", 1))),
                new Document("$sort", new Document("_id", 1))
        )).batchSize(100);
        for(Document cluster:allClusters){
            clusterMemberCounts.add(cluster.getInteger("count"));
            clusterMemberIds.add((List<Integer>) cluster.get("memberIds"));
        }

        int totalPoints = 0;
        for (int count : clusterMemberCounts) {
            totalPoints += count;
        }
        double entropyU = getEntropy(clusterMemberCounts, totalPoints);

        AggregateIterable<Document> allExpectedClusters = collection.aggregate(Arrays.asList(
                new Document("$match", new Document("isCentroid", false)),
                new Document("$group", new Document("_id", "$expected")
                        .append("memberIds", new Document("$addToSet", "$_id"))
                        .append("count", new Document("$sum", 1))),
                new Document("$sort", new Document("_id", 1))
        )).batchSize(100);

        double miUV = 0;
        List<Integer> expectedClusterMemberCounts = new ArrayList<>();
        //List<List<Integer>> expectedClusterMemberIds = new ArrayList<>();
        for(Document expectedCluster:allExpectedClusters){
            int expectedClusterMemberCount = expectedCluster.getInteger("count");
            expectedClusterMemberCounts.add(expectedClusterMemberCount);
            //expectedClusterMemberIds.add((List<Integer>) expectedCluster.get("memberIds"));
            for (int i=0; i<clusterMemberIds.size(); i++){
                List<Integer> uMembers = clusterMemberIds.get(i);
                uMembers.retainAll((List<Integer>) expectedCluster.get("memberIds"));
                if(uMembers.size() > 0)
                    miUV = miUV + ((double) uMembers.size()/totalPoints) * Math.log((double) (uMembers.size() * totalPoints) / (clusterMemberCounts.get(i) * expectedClusterMemberCount));
            }
        }

        double entropyV = getEntropy(expectedClusterMemberCounts, totalPoints);

        //Possible option
        //int[][] miTable = new int[clusterMemberIds.size()][expectedClusterMemberIds.size()];

//  Optimized code above
//        for (int i=0; i<clusterMemberIds.size(); i++){
//            for (int k=0; k<expectedClusterMemberIds.size(); k++){
//                List<Integer> u = clusterMemberIds.get(i);
//                u.retainAll(expectedClusterMemberIds.get(k));
//                //miTable[i][j] = u.size();
//                if(u.size() > 0)
//                    miUV += ((double) u.size()/totalPoints) * Math.log((double) u.size() * totalPoints / clusterMemberCounts.get(i) * expectedClusterMemberCounts.get(k));
//            }
//        }
        try {
            FileWriter fileWriter = new FileWriter(outputFile);
            System.out.println(entropyU);
            fileWriter.write(String.valueOf(entropyU)+"\n");
            System.out.println(entropyV);
            fileWriter.write(String.valueOf(entropyV)+"\n");
            System.out.println(miUV);;
            fileWriter.write(String.valueOf(miUV)+"\n");
            fileWriter.close();
        }catch(IOException ioe){
            System.err.println(ioe);
        }

        System.out.println("Entire process took : "+(System.currentTimeMillis()-start)/60000);
    }

    private static double getEntropy(List<Integer> counts, int totalPoints) {
        double entropy = 0;
        for (int count : counts) {
            entropy += -((double) count / totalPoints) * Math.log((double) count / totalPoints);
        }
        return entropy;
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
