package mandarin.packpack.commands.server;

import mandarin.packpack.commands.ConstraintCommand;
import mandarin.packpack.supporter.StaticStore;
import mandarin.packpack.supporter.lang.LangID;
import mandarin.packpack.supporter.server.data.IDHolder;
import mandarin.packpack.supporter.server.holder.ConfirmButtonHolder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;

import java.util.ArrayList;

public class CommandUnban extends ConstraintCommand {
    public CommandUnban(ROLE role, int lang, IDHolder id) {
        super(role, lang, id);
    }

    @Override
    public void doSomething(GenericMessageEvent event) throws Exception {
        MessageChannel ch = getChannel(event);
        Guild g = getGuild(event);

        if(ch == null || g == null)
            return;

        String[] contents = getContent(event).split(" ");

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

            Member me = getMember(event);

            if(me == null) {
                createMessageWithNoPings(ch, LangID.getStringByID("comban_fail", lang));

                return;
            }


            registerConfirmButtons(
                    ch.sendMessage(LangID.getStringByID("comunban_confirm", lang).replace("_", m.getId()))
                            .setAllowedMentions(new ArrayList<>())
                    , lang
            ).queue(msg -> StaticStore.putHolder(me.getId(), new ConfirmButtonHolder(msg, getMessage(event), ch.getId(), () -> {
                holder.banned.remove(m.getId());

                createMessageWithNoPings(ch, LangID.getStringByID("comunban_success", lang).replace("_", m.getId()));
            }, lang)), e -> StaticStore.logger.uploadErrorLog(e, "E/CommandUnban::doSomething - Failed to perform message with button"));
        }, e -> createMessageWithNoPings(ch, LangID.getStringByID("comban_nomember", lang)));
    }
}