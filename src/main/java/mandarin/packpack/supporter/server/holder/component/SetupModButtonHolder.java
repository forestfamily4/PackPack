package mandarin.packpack.supporter.server.holder.component;

import mandarin.packpack.commands.Command;
import mandarin.packpack.supporter.StaticStore;
import mandarin.packpack.supporter.lang.LangID;
import mandarin.packpack.supporter.server.data.IDHolder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;

import java.util.ArrayList;
import java.util.List;

public class SetupModButtonHolder extends ComponentHolder {
    private final Message msg;
    private final String channelID;
    private final String memberID;
    private final int lang;

    private final IDHolder holder;

    private String roleID;

    public SetupModButtonHolder(Message author, Message msg, String channelID, IDHolder holder, int lang) {
        super(author, channelID, msg.getId());

        this.msg = msg;
        this.channelID = channelID;
        this.memberID = author.getAuthor().getId();
        this.lang = lang;

        this.holder = holder;

        StaticStore.executorHandler.postDelayed(FIVE_MIN, () -> {
            if(expired)
                return;

            expired = true;

            StaticStore.removeHolder(author.getAuthor().getId(), SetupModButtonHolder.this);

            expire(userID);
        });
    }

    @Override
    public void onEvent(GenericComponentInteractionCreateEvent event) {
        MessageChannel ch = msg.getChannel();

        switch (event.getComponentId()) {
            case "role" -> {
                EntitySelectInteractionEvent es = (EntitySelectInteractionEvent) event;

                if (es.getValues().size() != 1)
                    return;

                roleID = es.getValues().get(0).getId();

                event.deferEdit()
                        .setContent(LangID.getStringByID("setup_modsele", lang).replace("_RRR_", es.getValues().get(0).getId()))
                        .setComponents(getComponents())
                        .setAllowedMentions(new ArrayList<>())
                        .queue();
            }
            case "confirm" -> Command.replyToMessageSafely(ch, LangID.getStringByID("setup_mem", lang), msg, a -> a.setComponents(
                    ActionRow.of(EntitySelectMenu.create("role", EntitySelectMenu.SelectTarget.ROLE).setPlaceholder(LangID.getStringByID("setup_select", lang)).setRequiredRange(1, 1).build()),
                    ActionRow.of(Button.success("confirm", LangID.getStringByID("button_confirm", lang)).asDisabled(), Button.danger("cancel", LangID.getStringByID("button_cancel", lang)))
            ), m -> {
                expired = true;

                StaticStore.removeHolder(memberID, this);

                StaticStore.putHolder(memberID, new SetupMemberButtonHolder(m, getAuthorMessage(), channelID, holder, roleID, lang));

                event.deferEdit()
                        .setContent(LangID.getStringByID("setup_modsele", lang).replace("_RRR_", roleID))
                        .setComponents()
                        .queue();
            });
            case "cancel" -> {
                expired = true;

                StaticStore.removeHolder(memberID, this);

                event.deferEdit()
                        .setContent(LangID.getStringByID("setup_cancel", lang))
                        .setComponents()
                        .queue();
            }
        }
    }

    @Override
    public void clean() {

    }

    @Override
    public void onExpire(String id) {
        expired = true;

        msg.editMessage(LangID.getStringByID("setup_expire", lang))
                .setComponents()
                .mentionRepliedUser(false)
                .queue();
    }

    private List<ActionRow> getComponents() {
        List<ActionRow> result = new ArrayList<>();

        result.add(ActionRow.of(EntitySelectMenu.create("role", EntitySelectMenu.SelectTarget.ROLE).setRequiredRange(1, 1).build()));

        Button confirm;

        if(roleID != null) {
            confirm = Button.success("confirm", LangID.getStringByID("button_confirm", lang)).asEnabled();
        } else {
            confirm = Button.success("confirm", LangID.getStringByID("button_confirm", lang)).asDisabled();
        }

        result.add(ActionRow.of(confirm, Button.danger("cancel", LangID.getStringByID("button_cancel", lang))));

        return result;
    }
}
