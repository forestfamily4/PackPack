package mandarin.packpack.commands.server;

import mandarin.packpack.commands.ConstraintCommand;
import mandarin.packpack.supporter.StaticStore;
import mandarin.packpack.supporter.lang.LangID;
import mandarin.packpack.supporter.server.CommandLoader;
import mandarin.packpack.supporter.server.data.IDHolder;
import mandarin.packpack.supporter.server.holder.component.ConfirmButtonHolder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class EventMessage extends ConstraintCommand {
    public EventMessage(ROLE role, int lang, @Nullable IDHolder id) {
        super(role, lang, id, true);
    }

    @Override
    public void doSomething(CommandLoader loader) throws Exception {
        MessageChannel ch = loader.getChannel();

        if(holder == null)
            return;

        String[] contents = loader.getContent().split(" ", 3);

        if(contents.length < 2) {
            replyToMessageSafely(ch, LangID.getStringByID("eventmes_noloc", lang), loader.getMessage(), a -> a);

            return;
        }

        String loc = getLocale(contents[1]);

        if(loc == null) {
            replyToMessageSafely(ch, LangID.getStringByID("eventmes_invalidloc", lang), loader.getMessage(), a -> a);

            return;
        }

        String message;

        if(contents.length < 3) {
            message = "";
        } else {
            message = contents[2];
        }

        if(message.length() >= 2000) {
            replyToMessageSafely(ch, LangID.getStringByID("eventmes_toolong", lang), loader.getMessage(), a -> a);

            return;
        }

        if(Pattern.compile("(<@(&)?\\d+>|@everyone|@here)").matcher(message).find()) {
            Member m = loader.getMember();
            replyToMessageSafely(ch, LangID.getStringByID("eventmes_mention", lang), loader.getMessage(), a -> registerConfirmButtons(a, lang), msg ->
                StaticStore.putHolder(m.getId(), new ConfirmButtonHolder(loader.getMessage(), msg, ch.getId(), () -> {
                    if(message.isBlank()) {
                        if(holder.eventMessage.containsKey(loc)) {
                            holder.eventMessage.remove(loc);

                            replyToMessageSafely(ch, LangID.getStringByID("eventmes_removed", lang), loader.getMessage(), a -> a);
                        } else {
                            replyToMessageSafely(ch, LangID.getStringByID("eventmes_noempty", lang), loader.getMessage(), a -> a);
                        }
                    } else {
                        holder.eventMessage.put(loc, message);

                        replyToMessageSafely(ch, LangID.getStringByID("eventmes_added", lang), loader.getMessage(), a -> a);
                    }
            } ,lang)));
        } else {
            if(message.isBlank()) {
                if(holder.eventMessage.containsKey(loc)) {
                    holder.eventMessage.remove(loc);

                    replyToMessageSafely(ch, LangID.getStringByID("eventmes_removed", lang), loader.getMessage(), a -> a);
                } else {
                    replyToMessageSafely(ch, LangID.getStringByID("eventmes_noempty", lang), loader.getMessage(), a -> a);
                }
            } else {
                holder.eventMessage.put(loc, message);

                replyToMessageSafely(ch, LangID.getStringByID("eventmes_added", lang), loader.getMessage(), a -> a);
            }
        }
    }

    private String getLocale(String loc) {
        return switch (loc) {
            case "-en" -> "en";
            case "-tw" -> "tw";
            case "-kr" -> "kr";
            case "-jp" -> "jp";
            default -> null;
        };
    }
}
