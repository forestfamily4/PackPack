package mandarin.packpack.supporter.server.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import mandarin.packpack.supporter.StaticStore;
import mandarin.packpack.supporter.lang.LangID;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;

import java.util.*;

public class IDHolder implements Cloneable {
    public static IDHolder jsonToIDHolder(JsonObject obj) {
        IDHolder id = new IDHolder();

        if(obj.has("server")) {
            id.serverPrefix = id.setOr(obj.get("server").getAsString());
        }

        if (obj.has("publish")) {
            id.publish = obj.get("publish").getAsBoolean();
        }

        if(obj.has("mod")) {
            id.MOD = id.setOrNull(obj.get("mod").getAsString());
        }

        if(obj.has("mem")) {
            id.MEMBER = id.setOrNull(obj.get("mem").getAsString());
        }

        if(obj.has("ann")) {
            id.ANNOUNCE = id.setOrNull(obj.get("ann").getAsString());
        }

        if(obj.has("status")) {
            JsonElement elem = obj.get("status");

            if(!elem.isJsonPrimitive()) {
                id.status = StaticStore.jsonToListString(elem.getAsJsonArray());
            }
        }

        if(obj.has("bo")) {
            id.BOOSTER = id.setOrNull(obj.get("bo").getAsString());
        }

        if(obj.has("channel")) {
            id.channel = id.toMap(obj.getAsJsonObject("channel"));
        }

        if(obj.has("id")) {
            id.ID = id.toIDMap(obj.getAsJsonObject("id"));

            while(id.ID.size() > SelectMenu.OPTIONS_MAX_AMOUNT) {
                String[] keys = id.ID.keySet().toArray(new String[0]);

                id.ID.remove(keys[keys.length - 1]);
            }
        }

        if(obj.has("logDM")) {
            id.logDM = id.setOrNull(obj.get("logDM").getAsString());
        }

        if(obj.has("config")) {
            id.config = ConfigHolder.parseJson(obj.getAsJsonObject("config"));
        }

        if(obj.has("locale")) {
            id.config.lang = obj.get("locale").getAsInt();
        }

        if(obj.has("banned")) {
            id.banned = id.jsonObjectToListString(obj.getAsJsonArray("banned"));
        }

        if(obj.has("channelException")) {
            id.channelException = id.toMap(obj.getAsJsonObject("channelException"));
        }

        if(obj.has("forceCompact")) {
            id.forceCompact = obj.get("forceCompact").getAsBoolean();
        }

        if(obj.has("forceFullTreasure")) {
            id.forceFullTreasure = obj.get("forceFullTreasure").getAsBoolean();
        }

        if(obj.has("announceMessage")) {
            id.announceMessage = obj.get("announceMessage").getAsString();
        }

        if(obj.has("eventMessage")) {
            id.eventMessage = StaticStore.jsonToMapString(obj.getAsJsonArray("eventMessage").getAsJsonArray());
        }

        if(obj.has("event") && obj.has("eventLocale")) {
            List<Integer> locales = id.jsonObjectToListInteger(obj.getAsJsonArray("eventLocale"));
            String channel = id.setOrNull(obj.get("event").getAsString());

            if(locales != null && channel != null && !channel.isBlank()) {
                for(int l : locales) {
                    id.eventMap.put(l, channel);
                }
            }
        }

        if(obj.has("eventMap")) {
            id.eventMap = id.jsonObjectToMapIntegerString(obj.get("eventMap"));
        }

        if(obj.has("boosterPin")) {
            id.boosterPin = obj.get("boosterPin").getAsBoolean();
        }

        if(obj.has("boosterPinChannel")) {
            id.boosterPinChannel = id.jsonObjectToListString(obj.getAsJsonArray("boosterPinChannel"));
        }

        if(id.config.lang < 0)
            id.config.lang = 0;

        return id;
    }

    public String serverPrefix = "p!";

    public String MOD;
    public String MEMBER;
    public String BOOSTER;
    public String ANNOUNCE;
    public String logDM = null;

    public boolean publish = false, eventRaw = false, forceCompact = false, forceFullTreasure = false, boosterPin = false;

    public ConfigHolder config = new ConfigHolder();

    public List<String> status = new ArrayList<>();
    public List<String> banned = new ArrayList<>();
    public List<String> boosterPinChannel = new ArrayList<>();

    public Map<String, String> ID = new TreeMap<>();
    public Map<String, String> eventMessage = new HashMap<>();
    public Map<Integer, String> eventMap = new TreeMap<>();
    public Map<String, List<String>> channel = new TreeMap<>();
    public Map<String, List<String>> channelException = new HashMap<>();

    public String announceMessage = "";

    public IDHolder(String m, String me, String bo) {
        this.MOD = m;
        this.MEMBER = me;
        this.BOOSTER = bo;

        config.lang = LangID.EN;
    }

    public IDHolder() {
        config.lang = LangID.EN;
    }

    public JsonObject jsonfy() {
        JsonObject obj = new JsonObject();

        obj.addProperty("server", getOrNull(serverPrefix));
        obj.addProperty("publish", publish);
        obj.addProperty("mod", getOrNull(MOD));
        obj.addProperty("mem", getOrNull(MEMBER));
        obj.addProperty("ann", getOrNull(ANNOUNCE));
        obj.add("status", StaticStore.listToJsonString(status));
        obj.addProperty("bo", getOrNull(BOOSTER));
        obj.add("channel", jsonfyMap(channel));
        obj.add("id", jsonfyIDs());
        obj.addProperty("logDM", getOrNull(logDM));
        obj.add("eventMap", mapIntegerStringToJsonArray(eventMap));
        obj.add("config", config.jsonfy());
        obj.add("banned", listStringToJsonObject(banned));
        obj.add("channelException", jsonfyMap(channelException));
        obj.addProperty("forceCompact", forceCompact);
        obj.addProperty("forceFullTreasure", forceFullTreasure);
        obj.addProperty("announceMessage", announceMessage);
        obj.add("eventMessage", StaticStore.mapToJsonString(eventMessage));
        obj.addProperty("boosterPin", boosterPin);
        obj.add("boosterPinChannel", listStringToJsonObject(boosterPinChannel));

        return obj;
    }

    public ArrayList<String> getAllAllowedChannels(Member member) {
        List<Role> ids = member.getRoles();
        List<String> exceptions = channelException.get(member.getId());

        ArrayList<String> result = new ArrayList<>();

        if(MEMBER == null) {
            List<String> channels = channel.get("Member");

            if(channels == null)
                return null;

            result.addAll(channels);
        }

        for(Role role : ids) {
            if(isSetAsRole(role.getId()) && (exceptions == null || !exceptions.contains(role.getId()))) {
                List<String> channels = channel.get(role.getId());

                if(channels == null)
                    return null;

                result.addAll(channels);
            }
        }

        return result;
    }

    private boolean hasIDasRole(String id) {
        for(String i : ID.values()) {
            if(id.equals(i))
                return true;
        }

        return false;
    }

    private boolean isSetAsRole(String id) {
        return id.equals(MOD) || id.equals(MEMBER) || id.equals(BOOSTER) || hasIDasRole(id);
    }

    private String getOrNull(String id) {
        return id == null ? "null" : id;
    }

    private String setOrNull(String id) {
        return id.equals("null") ? null : id;
    }

    private String setOr(String id) {
        return id.equals("null") ? "p!" : id;
    }

    private JsonElement listStringToJsonObject(List<String> arr) {
        if(arr == null) {
            return JsonNull.INSTANCE;
        }

        JsonArray array = new JsonArray();

        for (String s : arr) {
            array.add(s);
        }

        return array;
    }

    private JsonElement mapIntegerStringToJsonArray(Map<Integer, String> map) {
        JsonArray array = new JsonArray();

        for(int key : map.keySet()) {
            if(map.get(key) == null || map.get(key).isBlank())
                continue;

            JsonObject obj = new JsonObject();

            obj.addProperty("key", key);
            obj.addProperty("val", map.get(key));

            array.add(obj);
        }

        return array;
    }

    private List<String> jsonObjectToListString(JsonElement obj) {
        if(obj.isJsonArray()) {
            JsonArray ele = obj.getAsJsonArray();

            ArrayList<String> arr = new ArrayList<>();

            for(int i = 0; i < ele.size(); i++) {
                arr.add(ele.get(i).getAsString());
            }

            return arr;
        }

        return null;
    }

    private List<Integer> jsonObjectToListInteger(JsonElement obj) {
        if(obj.isJsonArray()) {
            JsonArray ele = obj.getAsJsonArray();

            List<Integer> arr = new ArrayList<>();

            for(int i = 0; i < ele.size(); i++) {
                arr.add(ele.get(i).getAsInt());
            }

            return arr;
        }

        return null;
    }

    private Map<Integer, String> jsonObjectToMapIntegerString(JsonElement obj) {
        if(obj.isJsonArray()) {
            JsonArray arr = obj.getAsJsonArray();

            Map<Integer, String> result = new TreeMap<>();

            for(int i = 0; i < arr.size(); i++) {
                JsonObject o = arr.get(i).getAsJsonObject();

                if(o.has("key") && o.has("val")) {
                    result.put(o.get("key").getAsInt(), o.get("val").getAsString());
                }
            }

            return result;
        }

        return new TreeMap<>();
    }

    private JsonObject jsonfyMap(Map<String, List<String>> map) {
        JsonObject obj = new JsonObject();

        Set<String> keys = map.keySet();

        int i = 0;

        for(String key : keys) {
            List<String> arr = map.get(key);

            if(arr == null)
                continue;

            JsonObject container = new JsonObject();

            container.addProperty("key", key);
            container.add("val" , listStringToJsonObject(arr));

            obj.add(Integer.toString(i), container);

            i++;
        }

        return obj;
    }

    private JsonObject jsonfyIDs() {
        JsonObject obj = new JsonObject();

        Set<String> keys = ID.keySet();

        int i = 0;

        for(String key : keys) {
            String id = ID.get(key);

            if(id == null)
                continue;

            JsonObject container = new JsonObject();

            container.addProperty("key", key);
            container.addProperty("val", id);

            obj.add(Integer.toString(i), container);

            i++;
        }

        return obj;
    }

    private TreeMap<String, List<String>> toMap(JsonObject obj) {
        TreeMap<String, List<String>> map = new TreeMap<>();

        int i = 0;

        while(true) {
            if(obj.has(Integer.toString(i))) {
                JsonObject container = obj.getAsJsonObject(Integer.toString(i));

                String key = container.get("key").getAsString();
                List<String> arr = jsonObjectToListString(container.get("val"));

                map.put(key, arr);

                i++;
            } else {
                break;
            }
        }

        return map;
    }

    private TreeMap<String, String> toIDMap(JsonObject obj) {
        TreeMap<String, String> map = new TreeMap<>();

        int i = 0;

        while(true) {
            if (obj.has(Integer.toString(i))) {
                JsonObject container = obj.getAsJsonObject(Integer.toString(i));

                String key = container.get("key").getAsString();
                String val = container.get("val").getAsString();

                map.put(key, val);

                i++;
            } else {
                break;
            }
        }

        return map;
    }



    @Override
    public String toString() {
        return "IDHolder{" +
                "serverPrefix='" + serverPrefix + '\'' +
                ", publish=" + publish +
                ", MOD='" + MOD + '\'' +
                ", MEMBER='" + MEMBER + '\'' +
                ", BOOSTER='" + BOOSTER + '\'' +
                ", ANNOUNCE='" + ANNOUNCE + '\'' +
                ", ID=" + ID +
                ", channel=" + channel +
                '}';
    }

    @Override
    public IDHolder clone() {
        try {
            IDHolder id = (IDHolder) super.clone();

            id.serverPrefix = serverPrefix;

            id.MOD = MOD;
            id.MEMBER = MEMBER;
            id.BOOSTER = BOOSTER;

            id.ANNOUNCE = ANNOUNCE;
            id.logDM = logDM;

            id.publish = publish;
            id.eventRaw = eventRaw;
            id.forceCompact = forceCompact;
            id.forceFullTreasure = forceFullTreasure;
            id.boosterPin = boosterPin;

            id.config = config.clone();

            id.status = new ArrayList<>(status);
            id.banned = new ArrayList<>(banned);
            id.boosterPinChannel = new ArrayList<>(boosterPinChannel);

            id.ID = new HashMap<>(ID);
            id.eventMessage = new HashMap<>(eventMessage);
            id.eventMap = new HashMap<>(eventMap);

            for(String key : channel.keySet()) {
                id.channel.put(key, new ArrayList<>(channel.get(key)));
            }

            for(String key : channelException.keySet()) {
                id.channelException.put(key, new ArrayList<>(channelException.get(key)));
            }

            return id;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}