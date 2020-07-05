package com.idm.sixth;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenerateLK {

    public static String mongoURL = "None";
    public static String mongoDatabase = "six";
    public static String mongoTransactions = "T_1";
    public static String collectionCK = "C3_1";
    public static String collectionLK = "L3_1";
    public static String minsupArg = "5";

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        if (args.length == 6) {
            mongoURL = args[0];
            mongoDatabase = args[1];
            mongoTransactions = args[2];
            collectionCK = args[3];
            collectionLK = args[4];
            minsupArg = args[5];
        }

        MongoClient mongoClient = getClient(mongoURL);
        MongoDatabase db = mongoClient.getDatabase(mongoDatabase);
        MongoCollection<Document> transactions = db.getCollection(mongoTransactions);
        MongoCollection<Document> cKCollection = db.getCollection(collectionCK);
        MongoCollection<Document> lKCollection = db.getCollection(collectionLK);
        int minsup = Integer.parseInt(minsupArg);
        int kValue = getKValue(cKCollection);

        FindIterable<Document> cKs= cKCollection.find().batchSize(1000);
        for (Document ck:cKs){
            List<Document> iidList = new ArrayList<>();
            Document pDoc = (Document) ck.get("_id");
            for (int i = 0; i < kValue; i++) {
                iidList.add(new Document("items", pDoc.get("pos_" + i)));
            }
            //db.N.aggregate([
            //{$match: { $and: [{ items: 1845} , {items: 598}]}},
            //{$group: { _id: null, "count" : { $sum : 1 } }},
            //{$project: {_id : 0} },
            //{$match: { "count" : { $gte:1 } }}])
            Document lkTransactionCount = transactions.aggregate(Arrays.asList(
                    new Document("$match", new Document("$and", iidList)),
                    new Document("$group", new Document("_id", null).append("count", new Document("$sum", 1))),
                    new Document("$project", new Document("_id", 0)),
                    new Document("$match", new Document("count", new Document("$gte", minsup)))
            )).first();
            if (lkTransactionCount != null) {
                ck.put("count", lkTransactionCount.get("count"));
                lKCollection.insertOne(ck);
            }
        }

        System.out.println("Entire process took : "+(System.currentTimeMillis()-start)/60000);
    }

    private static int getKValue(MongoCollection<Document> prevL) {
        Document firstDoc = prevL.find().first();
        Document ids = null;
        int kMinusOne = 0;
        if(firstDoc != null) {
            ids = (Document) firstDoc.get("_id");
            kMinusOne = ids.keySet().size();
        }
        return kMinusOne;
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
