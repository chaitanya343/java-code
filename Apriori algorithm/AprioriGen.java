package com.idm.sixth;

import com.google.common.collect.Sets;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.*;

public class AprioriGen {

    public static String mongoURL = "None";
    public static String mongoDatabase = "six";
    public static String mongoPrevL = "L3_1";
    public static String mongoNextL = "C4_1";

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        if (args.length == 4) {
            mongoURL = args[0];
            mongoDatabase = args[1];
            mongoPrevL = args[2];
            mongoNextL = args[3];
        }

        MongoClient mongoClient = getClient(mongoURL);
        MongoDatabase db = mongoClient.getDatabase(mongoDatabase);
        MongoCollection<Document> prevL = db.getCollection(mongoPrevL);
        MongoCollection<Document> nextL = db.getCollection(mongoNextL);

        int kMinusOne = getKValue(prevL);

        FindIterable<Document> ps = prevL.find().batchSize(1000);
        FindIterable<Document> qs = prevL.find().batchSize(1000);

        for (Document p:ps){
            for (Document q:qs) {
                List<Integer> pIds = new ArrayList<>();
                List<Integer> qIds = new ArrayList<>();
                List<Integer> cK = new ArrayList<>();

                Document pDoc = (Document) p.get("_id");
                Document qDoc = (Document) q.get("_id");

                for (int i = 0; i < kMinusOne; i++) {
                    pIds.add((Integer) pDoc.get("pos_" + i));
                    qIds.add((Integer) qDoc.get("pos_" + i));
                }

                Collections.sort(pIds);
                Collections.sort(qIds);
//              If p.item1=q.item1 AND p.item2=q.item2 AND ... AND p.itemk-1 < q.itemk-1:
//                 Add [p.item1, p.item2, ..., p.itemk-2, p.itemk-1, q.itemk-1] to Ck
                for (int i = 0; i < kMinusOne; i++) {
                    if (i != kMinusOne - 1 && !pIds.get(i).equals(qIds.get(i))) {
                        break;
                    }
                    if (i == kMinusOne - 1 && pIds.get(i) < qIds.get(i)) {
                        cK.addAll(pIds);
                        cK.add(qIds.get(i));
                        //TODO: Optimization perform prune check here or is it already done?
                    }
                }
                //All combinations of cK size cK.size()-1 present in prevL
                if(cK.size() > 2){
                    boolean allPresent = false;
                    Set<Set<Integer>> cdPrevCombinations = Sets.combinations(Sets.newHashSet(cK), cK.size() - 1);
                    for (Set<Integer> comb : cdPrevCombinations) {
                        //TODO: Sort comb or not?
                        List<Integer> combList = new ArrayList<>(comb);
                        Collections.sort(combList);
                        List<Document> combDocs = new ArrayList<>();
                        for (int i = 0; i < combList.size(); i++) {
                            combDocs.add(new Document("_id.pos_" + i, combList.get(i)));
                        }
                        AggregateIterable<Document> combMatch = prevL.aggregate(Arrays.asList(
                                new Document("$match", new Document("$and", combDocs))
                        ));
                        //db.L2.aggregate([
                        //{$match: { "_id.pos_0":705356, "_id.pos_1":719637 }}
                        //])
                        if (combMatch.first() == null) {
                            allPresent = false;
                            break;
                        } else {
                            allPresent = true;
                        }
                    }
                    if (!allPresent) {
                        continue;
                    }
                }
                if(!cK.isEmpty()){
                    Document candidate = new Document();
                    Document ckItems = new Document();
                    for(int i=0; i<cK.size(); i++) {
                        ckItems.append("pos_"+i, cK.get(i));
                    }
                    candidate.put("_id", ckItems);
                    candidate.put("count", 0);
                    nextL.insertOne(candidate);
                }
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

// O/P structure
//  {
//    _id : {
//      pos_0 : iid_0,
//      pos_1 : iid_1,
//      ... ,
//      pos_(k-1) : iid_(k-1)
//      },
//    count : 0
//    }
}
