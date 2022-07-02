package mandarin.packpack.commands.server;

import mandarin.packpack.commands.ConstraintCommand;
import mandarin.packpack.supporter.StaticStore;
import mandarin.packpack.supporter.server.data.IDHolder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;

import java.io.File;

public class ServerJson extends ConstraintCommand {
    public ServerJson(ROLE role, int lang, IDHolder id) {
        super(role, lang, id);
    }

    @Override
    public void doSomething(GenericMessageEvent event) throws Exception {
        MessageChannel ch = getChannel(event);

        if(ch == null)
            return;

        StaticStore.saveServerInfo();

        File f = new File("./data/serverinfo.json");

        if(f.exists()) {
            Message msg = getMessage(event);

            if(msg != null) {
                msg.getAuthor().openPrivateChannel()
                        .flatMap(pc -> pc.sendMessage("Sent serverinfo.json via DM").addFile(f, "serverinfo.json"))
                        .queue();
            }
        }
    }
}
