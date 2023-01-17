package mandarin.packpack.supporter.server.holder;

import common.util.unit.Form;
import mandarin.packpack.supporter.StaticStore;
import mandarin.packpack.supporter.bc.EntityHandler;
import mandarin.packpack.supporter.server.data.ConfigHolder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class FormReactionSlashMessageHolder extends MessageHolder<MessageReactionAddEvent> {
    private final Form f;
    private final String memberID;
    private final String channelID;
    private final String embID;
    private final ConfigHolder config;
    private final int lang;
    private final Message m;

    private final boolean isFrame;
    private final boolean talent;
    private final boolean extra;
    private final ArrayList<Integer> lv;

    public FormReactionSlashMessageHolder(Message m, Form f, String memberID, String channelID, String embID, ConfigHolder config, boolean isFrame, boolean talent, boolean extra, ArrayList<Integer> lv, int lang) {
        super(MessageReactionAddEvent.class, null);

        this.f = f;
        this.memberID = memberID;
        this.channelID = channelID;
        this.embID = embID;
        this.config = config;
        this.lang = lang;

        this.isFrame = isFrame;
        this.talent = talent;
        this.extra = extra;
        this.lv = lv;

        this.m = m;

        Timer autoFinish = new Timer();

        autoFinish.schedule(new TimerTask() {
            @Override
            public void run() {
                if(expired)
                    return;

                expired = true;

                StaticStore.removeHolder(memberID, FormReactionSlashMessageHolder.this);

                if(!(m.getChannel() instanceof GuildChannel) || m.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)) {
                    m.clearReactions().queue();
                }
            }
        }, TimeUnit.MINUTES.toMillis(5));
    }

    @Override
    public int handleEvent(MessageReactionAddEvent event) {
        if(expired) {
            System.out.println("Expired at FormReactionSlashHolder!");
            return RESULT_FAIL;
        }

        MessageChannel ch = event.getChannel();

        if(!ch.getId().equals(channelID))
            return RESULT_STILL;

        if(!event.getMessageId().equals(embID))
            return RESULT_STILL;

        if(event.getUser() == null)
            return RESULT_STILL;

        User u = event.getUser();

        if(!u.getId().equals(memberID))
            return RESULT_STILL;

        Emoji emoji = event.getEmoji();

        if(!(emoji instanceof CustomEmoji))
            return RESULT_STILL;

        boolean emojiClicked = false;

        switch (emoji.getName()) {
            case "TwoPrevious":
                emojiClicked = true;

                if(f.fid - 2 < 0)
                    break;

                if(f.unit == null)
                    break;

                Form newForm = f.unit.forms[f.fid - 2];

                try {
                    EntityHandler.showUnitEmb(newForm, ch, getAuthorMessage(), config, isFrame, talent, extra, false, false, lv, lang, false, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            case "Previous":
                emojiClicked = true;

                if(f.fid - 1 < 0)
                    break;

                if(f.unit == null)
                    break;

                newForm = f.unit.forms[f.fid - 1];

                try {
                    EntityHandler.showUnitEmb(newForm, ch, getAuthorMessage(), config, isFrame, talent, extra, false, false, lv, lang, false, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            case "Next":
                emojiClicked = true;

                if(f.unit == null)
                    break;

                if(f.fid + 1 >= f.unit.forms.length)
                    break;

                newForm = f.unit.forms[f.fid + 1];

                try {
                    EntityHandler.showUnitEmb(newForm, ch, getAuthorMessage(), config, isFrame, talent, extra, false, false, lv, lang, false, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            case "TwoNext":
                emojiClicked = true;

                if(f.unit == null)
                    break;

                if(f.fid + 2 >= f.unit.forms.length)
                    break;

                newForm = f.unit.forms[f.fid + 2];

                try {
                    EntityHandler.showUnitEmb(newForm, ch, getAuthorMessage(), config, isFrame, talent, extra, false, false, lv, lang, false, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
        }

        if(emojiClicked) {
            if(!(ch instanceof GuildChannel) || m.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)) {
                m.clearReactions().queue();
            }

            expired = true;
        }

        return emojiClicked ? RESULT_FINISH : RESULT_STILL;
    }

    @Override
    public void clean() {

    }

    @Override
    public void expire(String id) {
        if(expired)
            return;

        expired = true;

        StaticStore.removeHolder(id, this);

        MessageChannel ch = m.getChannel();

        if(!(ch instanceof GuildChannel) || m.getGuild().getSelfMember().hasPermission((GuildChannel) ch, Permission.MESSAGE_MANAGE)) {
            m.clearReactions().queue();
        }
    }
}
