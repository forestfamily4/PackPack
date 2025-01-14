package mandarin.packpack.commands.server;

import mandarin.packpack.commands.ConstraintCommand;
import mandarin.packpack.supporter.StaticStore;
import mandarin.packpack.supporter.lang.LangID;
import mandarin.packpack.supporter.server.CommandLoader;
import mandarin.packpack.supporter.server.data.IDHolder;
import mandarin.packpack.supporter.server.holder.component.ConfirmButtonHolder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.util.ArrayList;

public class CommandUnban extends ConstraintCommand {
    public CommandUnban(ROLE role, int lang, IDHolder id) {
        super(role, lang, id, true);
    }

    @Override
    public void doSomething(CommandLoader loader) throws Exception {
        if(holder == null)
            return;

        MessageChannel ch = loader.getChannel();
        Guild g = loader.getGuild();

        String[] contents = loader.getContent().split(" ");

        if(contents.length < 2) {
            createMessageWithNoPings(ch, LangID.getStringByID("comban_noid", lang));

            return;
        }

        String id = contents[1].replaceAll("(<@(!)?|>)", "");

        if(!StaticStore.isNumeric(id)) {
            createMessageWithNoPings(ch, LangID.getStringByID("comban_nonumber", lang));

            return;
        }

        g.retrieveMemberById(id).queue(m -> {
            if(m == null) {
                createMessageWithNoPings(ch, LangID.getStringByID("comban_nomember", lang));

                return;
            }

            if(!holder.banned.contains(m.getId())) {
                createMessageWithNoPings(ch, LangID.getStringByID("comunban_notban", lang));

                return;
            }

            Member me = loader.getMember();


            registerConfirmButtons(
                    ch.sendMessage(LangID.getStringByID("comunban_confirm", lang).replace("_", m.getId()))
                            .setAllowedMentions(new ArrayList<>())
                    , lang
            ).queue(msg -> StaticStore.putHolder(me.getId(), new ConfirmButtonHolder(loader.getMessage(), msg, ch.getId(), () -> {
                holder.banned.remove(m.getId());

                createMessageWithNoPings(ch, LangID.getStringByID("comunban_success", lang).replace("_", m.getId()));
            }, lang)), e -> StaticStore.logger.uploadErrorLog(e, "E/CommandUnban::doSomething - Failed to perform message with button"));
        }, e -> createMessageWithNoPings(ch, LangID.getStringByID("comban_nomember", lang)));
    }
}
