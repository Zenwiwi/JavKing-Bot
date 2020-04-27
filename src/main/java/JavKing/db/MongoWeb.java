package JavKing.db;

import JavKing.main.BotContainer;
import JavKing.main.DiscordBot;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

public class MongoWeb {
    private MongoClient client;

    public MongoWeb() {
        client = createConnection();
    }

    private MongoClient createConnection() {
        try {
            return MongoClients.create(BotContainer.getDotenv("MONGO_DB"));
        } catch (Exception e) {
            e.printStackTrace();
            DiscordBot.LOGGER.error("Can't connect to the database!");
        }
        return null;
    }

    public MongoClient getConnection() {
        if (client == null) {
            client = createConnection();
        }
        return client;
    }
}
