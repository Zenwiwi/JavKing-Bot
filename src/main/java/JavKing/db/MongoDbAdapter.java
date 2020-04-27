package JavKing.db;

import JavKing.command.model.OGuild;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import org.bson.Document;

public class MongoDbAdapter {
    private MongoClient client;

    public MongoDbAdapter() {
        client = new MongoWeb().getConnection();
    }

    public MongoDatabase getDatabase() {
        return client.getDatabase("JavKing");
    }

    public OGuild getGuild(MessageChannel channel) {
        return getGuild(((TextChannel) channel).getGuild().getIdLong());
    }

    public OGuild getGuild(long guildId) {
        return loadRes(getCollection("guildSettings").find(Filters.eq("guildId", String.valueOf(guildId))));
    }

    public MongoCollection getCollection(String collection) {
        MongoDatabase JavDB = getDatabase();
        try {
            return JavDB.getCollection(collection);
        } catch (Exception e) {
            JavDB.createCollection(collection);
            return JavDB.getCollection(collection);
        }
    }

    public int dropCollection(String collection) {
        getCollection(collection).drop();
        return 1;
    }

    public int createCollection(String collection) {
        getDatabase().createCollection(collection);
        return 1;
    }

    public int update(String collection, TextChannel channel, String[] keys, Object[] values) {
        return update(collection, channel.getGuild(), keys, values);
    }

    public int update(String collection, Guild guild, String[] keys, Object[] values) {
        return update(collection, guild.getIdLong(), keys, values);
    }

    public int update(String collection, long guildId, String[] keys, Object[] values) {
        Document doc = (Document) getCollection(collection).find(Filters.eq("guildId", guildId)).first();
        if (keys.length == values.length) {
            for (int i = 0; i < keys.length; i++) {
                resolveParameters(collection, String.valueOf(guildId), keys[i], values[i], doc);
            }
        }
        return 1;
    }

    public void resolveParameters(String collection, String guildId, String key, Object value, Document doc) {
        if (doc != null && doc.getString("guildId").equals(guildId)) {
            getCollection(collection).updateOne(Filters.eq("guildId", guildId), new Document("$set", new Document(key, value)));
        } else {
            getCollection(collection).insertOne(new Document("guildId", guildId).append(key, value));
        }
    }

    public OGuild loadRes(FindIterable<Object> findIterable) {
        return new OGuild();
    }
}
