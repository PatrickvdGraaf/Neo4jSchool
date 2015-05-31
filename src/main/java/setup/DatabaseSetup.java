package setup;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

/**
 * This class is used to...
 * Created by Patrick van de Graaf
 */
public class DatabaseSetup {

    public static void setup(GraphDatabaseService graphDb){
        try(Transaction tx = graphDb.beginTx()){
            tx.success();
        }
    }
}
