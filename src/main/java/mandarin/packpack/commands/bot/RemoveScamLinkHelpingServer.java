package mandarin.packpack.commands.bot;

import mandarin.packpack.commands.ConstraintCommand;
import mandarin.packpack.supporter.StaticStore;
import mandarin.packpack.supporter.server.CommandLoader;
import mandarin.packpack.supporter.server.data.IDHolder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

public class RemoveScamLinkHelpingServer extends ConstraintCommand {
    public RemoveScamLinkHelpingServer(ROLE role, int lang, IDHolder id) {
        super(role, lang, id, false);
    }

    @Override
    public void doSomething(CommandLoader loader) throws Exception {
        MessageChannel ch = loader.getChannel();

        String[] contents = loader.getContent().split(" ");

        if(contents.length < 2) {
            ch.sendMessage("Usage : p!ashs [Server ID]").queue();

            return;
        }

        if(!StaticStore.isNumeric(contents[1])) {
            ch.sendMessage("Server ID must be numeric!").queue();

            return;
        }


        if(!StaticStore.scamLink.servers.contains(contents[1])) {
            ch.sendMessage("This server is already unregistered as helping server").queue();

            return;
        }

        StaticStore.scamLink.servers.remove(contents[1]);
        ch.sendMessage("Removed server "+contents[1]).queue();
    }
}
