package mandarin.packpack.commands.server;

import mandarin.packpack.commands.ConstraintCommand;
import mandarin.packpack.supporter.StaticStore;
import mandarin.packpack.supporter.lang.LangID;
import mandarin.packpack.supporter.server.data.IDHolder;
import mandarin.packpack.supporter.server.holder.component.ConfirmButtonHolder;
import mandarin.packpack.supporter.server.holder.component.SetupModButtonHolder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ForLoopReplaceableByForEach")
public class Setup extends ConstraintCommand {
    public Setup(ROLE role, int lang, IDHolder id) {
        super(role, lang, id, true);
    }

    @Override
    public void doSomething(GenericMessageEvent event) throws Exception {
        Guild g = getGuild(event);
        MessageChannel ch = getChannel(event);

        if(g == null || ch == null) {
            return;
        }

        if(alreadySet(g)) {
            List<ActionComponent> components = new ArrayList<>();

            components.add(Button.success("confirm", LangID.getStringByID("button_confirm", lang)));
            components.add(Button.danger("cancel", LangID.getStringByID("button_cancel", lang)));

            Message m = ch.sendMessage(LangID.getStringByID("setup_confirm", lang))
                    .setComponents(ActionRow.of(components))
                    .complete();

            if(m == null)
                return;

            Message author = getMessage(event);
            Member member = getMember(event);

            if(author == null || member == null)
                return;

            StaticStore.putHolder(member.getId(), new ConfirmButtonHolder(author, m, ch.getId(), () -> initializeSetup(ch, author), lang));
        } else {
            Message author = getMessage(event);

            if(author == null)
                return;

            initializeSetup(ch, author);
        }
    }

    private void initializeSetup(MessageChannel ch, Message author) {
        MessageCreateAction action = ch.sendMessage(LangID.getStringByID("setup_mod", lang));

        action = action.setComponents(
                ActionRow.of(EntitySelectMenu.create("role", EntitySelectMenu.SelectTarget.ROLE).setPlaceholder(LangID.getStringByID("setup_select", lang)).setRequiredRange(1, 1).build()),
                ActionRow.of(Button.success("confirm", LangID.getStringByID("button_confirm", lang)).asDisabled(), Button.danger("cancel", LangID.getStringByID("button_cancel", lang)))
        ).setAllowedMentions(new ArrayList<>());

        Message m = action.complete();

        if(m == null)
            return;

        StaticStore.putHolder(author.getAuthor().getId(), new SetupModButtonHolder(author, m, ch.getId(), holder, lang));
    }

    private boolean alreadySet(Guild g) {
        if(holder == null)
            throw new IllegalStateException("E/Setup::alreadySet - IDHolder must not be null");

        if(holder.MOD != null) {
            Role r = g.getRoleById(holder.MOD);

            if(r == null) {
                StaticStore.logger.uploadLog("W/Setup::alreadySet | Role was null");

                return false;
            }

            return !r.getName().equals("PackPackMod") || holder.MEMBER != null;
        } else {
            StaticStore.logger.uploadLog("Invalid ID holder data found, moderator role ID was null\nServer ID : "+g.getId()+" | "+g.getName()+"\n-----ID Holder-----\n\n"+holder);
        }

        return false;
    }
}
