package mandarin.packpack.commands.data;

import mandarin.packpack.commands.ConstraintCommand;
import mandarin.packpack.supporter.StaticStore;
import mandarin.packpack.supporter.lang.LangID;
import mandarin.packpack.supporter.server.CommandLoader;
import mandarin.packpack.supporter.server.data.IDHolder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.util.ArrayList;
import java.util.List;

public class PrintItemEvent extends ConstraintCommand {
    public PrintItemEvent(ROLE role, int lang, IDHolder id) {
        super(role, lang, id, false);
    }

    @Override
    public void doSomething(CommandLoader loader) throws Exception {
        MessageChannel ch = loader.getChannel();

        boolean now = isNow(loader.getContent());
         int t = 0;

        if(now) {
            User u = loader.getUser();

            t = StaticStore.timeZones.getOrDefault(u.getId(), 0);

            String content;

            if(t >= 0)
                content = "+" + t;
            else
                content = String.valueOf(t);

            ch.sendMessage(LangID.getStringByID("printevent_time", lang).replace("_", content)).queue();
        }

        boolean full = isFull(loader.getContent());

        User u = loader.getUser();

        if(full && !StaticStore.contributors.contains(u.getId())) {
            full = false;

            createMessageWithNoPings(ch, LangID.getStringByID("event_ignorefull", lang));
        }

        List<String> result = StaticStore.event.printItemEvent(getLocale(loader.getContent()), lang, full, isRaw(loader.getContent()), now, t);

        if(result.isEmpty()) {
            ch.sendMessage(LangID.getStringByID("chevent_noup", lang)).queue();

            return;
        }

        boolean started = false;

        while(!result.isEmpty()) {
            StringBuilder builder = new StringBuilder();

            if(!started) {
                started = true;

                builder.append(LangID.getStringByID("event_item", lang)).append("\n\n");
            }

            builder.append("```ansi\n");

            while(builder.length() < 1950 && !result.isEmpty()) {
                String line = result.get(0);

                if(line.length() > 1950) {
                    result.remove(0);

                    continue;
                }

                if(builder.length() + line.length() > 1950)
                    break;

                builder.append(line).append("\n");

                result.remove(0);
            }

            builder.append("```");

            ch.sendMessage(builder.toString())
                    .setAllowedMentions(new ArrayList<>())
                    .queue();
        }
    }

    private int getLang() {
        if(lang >= 1 && lang < 4) {
            return lang;
        } else {
            return 0;
        }
    }

    private int getLocale(String content) {
        String[] contents = content.split(" ");

        for(int i = 0; i < contents.length; i++) {
            switch (contents[i]) {
                case "-en" -> {
                    return LangID.EN;
                }
                case "-tw" -> {
                    return LangID.ZH;
                }
                case "-kr" -> {
                    return LangID.KR;
                }
                case "-jp" -> {
                    return LangID.JP;
                }
            }
        }

        return getLang();
    }

    private boolean isFull(String content) {
        String[] contents = content.split(" ");

        for(int i = 0; i < contents.length; i++) {
            if(contents[i].equals("-f") || contents[i].equals("-full"))
                return true;
        }

        return false;
    }

    private boolean isRaw(String content) {
        String[] contents = content.split(" ");

        for(int i = 0; i < contents.length; i++) {
            if(contents[i].equals("-r") || contents[i].equals("-raw"))
                return true;
        }

        return false;
    }

    private boolean isNow(String content) {
        String[] contents = content.split(" ");

        for(int i = 0; i < contents.length; i++) {
            if (contents[i].equals("-n") || contents[i].equals("-now")) {
                return true;
            }
        }

        return false;
    }
}
