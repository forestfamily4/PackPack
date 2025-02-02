package mandarin.packpack.commands.bot;

import mandarin.packpack.commands.ConstraintCommand;
import mandarin.packpack.supporter.StaticStore;
import mandarin.packpack.supporter.server.CommandLoader;
import mandarin.packpack.supporter.server.data.IDHolder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;

public class ServerJson extends ConstraintCommand {
    public ServerJson(ROLE role, int lang, IDHolder id) {
        super(role, lang, id, false);
    }

    @Override
    public void doSomething(CommandLoader loader) throws Exception {
        StaticStore.saveServerInfo();

        File f = new File("./data/serverinfo.json");

        if(f.exists()) {
            Message msg = loader.getMessage();

            msg.getAuthor().openPrivateChannel()
                    .flatMap(pc -> pc.sendMessage("Sent serverinfo.json via DM").addFiles(FileUpload.fromData(f, "serverinfo.json")))
                    .queue();
        }
    }
}
