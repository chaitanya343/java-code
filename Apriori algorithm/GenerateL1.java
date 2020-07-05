package com.idm.sixth;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.Arrays;

public class GenerateL1 {

    public static String mongoURL = "None";
    public static String mongoDatabase = "test";
    public static String mongoTransactions = "N";
    public static String mongoCollection = "L1";
    public static String minsup = "3";

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        if (args.length == 5) {
            mongoURL = args[0];
            mongoDatabase = args[1];
            mongoTransactions = args[2];
            mongoCollection = args[3];
            minsup = args[4];
        }

        MongoClient mongoClient = getClient(mongoURL);
        MongoDatabase db = mongoClient.getDatabase(mongoDatabase);
        MongoCollection<Document> transactions = db.getCollection(mongoTransactions);
        MongoCollection<Document> l1s = db.getCollection(mongoCollection);

//        AggregateIterable<Document> l1 = transactions.aggregate(Arrays.asList(Aggregates.unwind("$items"),
//                Aggregates.group("$items", Accumulators.sum("count", 1)),
//                Aggregates.match(Filters.gte("count", Integer.parseInt(minsup)))
//        ));
//        --
        AggregateIterable<Document> l1 = transactions.aggregate(Arrays.asList(
                new Document("$unwind", "$items"),
                new Document("$group", new Document("_id", new Document("pos_0", "$items")).append("count", new Document("$sum", 1))),
                new Document("$match", new Document("count", new Document("$gte", Integer.parseInt(minsup))))
        ));
//        --Mongo Query
//        db.N.aggregate([{$unwind : "$items" },
//        {$group: { _id: {"pos_0":"$items"}, "count" : { $sum : 1 } }}]),
//        {$match: { "count" : { $gte:3 } }}

        for (Document doc : l1) {
            l1s.insertOne(doc);
        }
        System.out.println("Entire process took : "+(System.currentTimeMillis()-start)/60000);
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
