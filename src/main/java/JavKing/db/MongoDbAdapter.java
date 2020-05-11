package JavKing.db;

import JavKing.command.model.OGuild;
import JavKing.command.model.OMusic;
import JavKing.main.BotContainer;
import JavKing.util.Util;
import JavKing.util.YTSearch;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.bson.Document;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;

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

    public MongoCollection<Document> getCollection(String collection) {
        return getDatabase().getCollection(collection);
    }

    public int dropCollection(String collection) {
        getCollection(collection).drop();
        return 1;
    }

    public int createCollection(String collection) {
        getDatabase().createCollection(collection);
        return 1;
    }

    /**
     * Update server related stats
     **/

    public int update(String collection, TextChannel channel, String key, Object value) {
        return update(collection, channel, new String[]{key}, new Object[]{value});
    }

    public int update(String collection, TextChannel channel, String[] keys, Object[] values) {
        return update(collection, channel.getGuild(), keys, values);
    }

    public int update(String collection, Guild guild, String[] keys, Object[] values) {
        return update(collection, guild.getId(), keys, values);
    }

    public int update(String collection, String guildId, String[] keys, Object[] values) {
        return resolveParameters(collection, guildId, null, keys, values);
    }

    /**
     * Update music related stats
     **/

    public int updateMusic(String collection, String key, Object value) {
        return updateMusic(collection, new String[]{key}, new Object[]{value});
    }

    public int updateMusic(String collection, String[] keys, Object[] values) {
        return resolveParameters(collection, null, null, keys, values);
    }

    private int resolveParameters(String collection, @Nullable String guildId, @Nullable Document doc, String[] keys, Object[] values) {
        if (keys.length == values.length) {
            if (doc == null) doc = new Document();
            ArrayList<Object> unusable = new ArrayList<>();
            for (int i = 0; i < keys.length; i++) {
                if (values[i].getClass().getName().startsWith("java.lang")) {
                    doc.append(keys[i], values[i]);
                } else unusable.add(values[i]);
            }
            String select, id;
            if (guildId == null) {
                select = Util.idOrURI(doc.getString("uri")) ? "uri" : "id";
                id = select.equals("uri") ? doc.getString("uri") : doc.getString("id");
            } else {
                doc.append("guildId", guildId);
                select = "guildId";
                id = guildId;
            }
            MongoCollection<Document> dbDoc = getCollection(collection);
            if (dbDoc.find(Filters.eq(select, id)).first() != null) {
                dbDoc.updateOne(Filters.eq(select, id), new Document("$set", doc));
            } else {
                dbDoc.insertOne(doc);
            }
            BotContainer.LOGGER.info("Successfully updated music db category with " + unusable.size() + " errors");
            return 1;
        } else {
            BotContainer.LOGGER.error("Keys array not equal to values array");
            return -1;
        }
    }

    public OMusic loadMusic(String uri, User requester) {
        return loadMusic(uri, requester, (String) null);
    }

    public OMusic loadMusic(@Nullable String uri, @Nullable User requester, TextChannel channel) {
        return loadMusic(uri, requester, channel.getGuild());
    }

    public OMusic loadMusic(@Nullable String uri, @Nullable User requester, Guild guild) {
        return loadMusic(uri, requester, guild.getId());
    }

    public OMusic loadMusic(@Nullable String uri, @Nullable User requester, @Nullable String guildId) {
        Document doc = uri == null
                ? getCollection("lastPlayed").find(Filters.eq("guildId", guildId)).first()
                : (Util.idOrURI(uri) ? getCollection("SCvideoId") : getCollection("videoId"))
                .find(Filters.eq(Util.idOrURI(uri) ? "uri" : "id", uri)).first();

        if (doc != null) {
            OMusic music = new OMusic();
            music.id = doc.getString("id");
            music.title = doc.getString("title");
            try {
                music.duration = doc.getLong("duration");
                music.author = doc.getString("channel");
            } catch (Exception ignored) {

            }
            music.uri = doc.getString("uri");
            music.thumbnail = doc.getString("thumbnail");
            if (requester != null) music.requestedBy = requester.getAsTag();
            return music;
        }
        return null;
    }

    public OGuild loadRes(FindIterable<Document> findIterable) {
        return new OGuild();
    }
}
