package mandarin.packpack.commands.bot;

import mandarin.packpack.commands.ConstraintCommand;
import mandarin.packpack.supporter.StaticStore;
import mandarin.packpack.supporter.server.CommandLoader;
import mandarin.packpack.supporter.server.data.IDHolder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

public class SuggestUnban extends ConstraintCommand {

    public SuggestUnban(ROLE role, int lang, IDHolder id) {
        super(role, lang, id, false);
    }

    @Override
    public void doSomething(CommandLoader loader) throws Exception {
        MessageChannel ch = loader.getChannel();

        String[] contents = loader.getContent().split(" ");

        if(contents.length < 2) {
            ch.sendMessage("This command requires user ID!").queue();
        } else {
            if(StaticStore.suggestBanned.containsKey(contents[1])) {
                StaticStore.suggestBanned.remove(contents[1]);

                ch.sendMessage("Unbanned "+contents[1]).queue();
            } else {
                ch.sendMessage("That user seems not suggest-banned yet").queue();
            }
        }
    }
}
