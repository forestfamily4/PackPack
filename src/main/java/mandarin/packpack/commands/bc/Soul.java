package mandarin.packpack.commands.bc;

import common.pack.UserProfile;
import common.util.Data;
import mandarin.packpack.commands.ConstraintCommand;
import mandarin.packpack.commands.GlobalTimedConstraintCommand;
import mandarin.packpack.supporter.StaticStore;
import mandarin.packpack.supporter.bc.EntityHandler;
import mandarin.packpack.supporter.lang.LangID;
import mandarin.packpack.supporter.server.CommandLoader;
import mandarin.packpack.supporter.server.data.IDHolder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Soul extends GlobalTimedConstraintCommand {
    private static final String NO_ID = "NO_ID";
    private static final String INVALID_RANGE = "INVALID_RANGE";

    private final int PARAM_DEBUG = 2;
    private final int PARAM_RAW = 4;
    private final int PARAM_GIF = 8;

    public Soul(ConstraintCommand.ROLE role, int lang, IDHolder id, String mainID) {
        super(role, lang, id, mainID, TimeUnit.SECONDS.toMillis(30), false);
    }

    @Override
    public void prepare() throws Exception {
        registerRequiredPermission(Permission.MESSAGE_ATTACH_FILES);
    }

    @Override
    protected void doThing(CommandLoader loader) {
        MessageChannel ch = loader.getChannel();
        User u = loader.getUser();

        int id = findSoulID(loader.getContent());

        common.util.pack.Soul s = UserProfile.getBCData().souls.get(id);

        if(s == null) {
            replyToMessageSafely(ch, LangID.getStringByID("soul_nosoul", lang), loader.getMessage(), a -> a);

            return;
        }

        boolean isTrusted = StaticStore.contributors.contains(u.getId()) || u.getId().equals(StaticStore.MANDARIN_SMELL);

        int param = checkParameters(loader.getContent());
        boolean debug = (param & PARAM_DEBUG) > 0;
        boolean raw = (param & PARAM_RAW) > 0;
        boolean gif = (param & PARAM_GIF) > 0;
        int frame = getFrame(loader.getContent());

        if(raw && !isTrusted) {
            ch.sendMessage(LangID.getStringByID("gif_ignore", lang)).queue();
        }

        int boostLevel = 0;

        if (ch instanceof GuildChannel) {
            boostLevel = loader.getGuild().getBoostTier().getKey();
        }

        EntityHandler.generateSoulAnim(s, ch, loader.getMessage(), boostLevel, debug, frame, lang, raw && isTrusted, gif, () -> {
            if(raw && isTrusted) {
                StaticStore.logger.uploadLog("Generated mp4 by user " + u.getName() + " for soul ID " + Data.trio(s.getID().id));
            }

            if(raw && isTrusted) {
                changeTime(TimeUnit.MINUTES.toMillis(1));
            }
        }, this::disableTimer);
    }

    @Override
    protected void setOptionalID(CommandLoader loader) {
        int id = findSoulID(loader.getContent());

        if(id == -1) {
            optionalID = NO_ID;
        } else {
            int max = UserProfile.getBCData().souls.size();

            if(id >= max) {
                optionalID = INVALID_RANGE;
            }
        }
    }

    @Override
    protected void prepareAborts() {
        aborts.add(NO_ID);
        aborts.add(INVALID_RANGE);
    }

    @Override
    protected void onAbort(CommandLoader loader) {
        MessageChannel ch = loader.getChannel();

        switch (optionalID) {
            case NO_ID -> replyToMessageSafely(ch, LangID.getStringByID("soul_argu", lang), loader.getMessage(), a -> a);
            case INVALID_RANGE -> {
                int soulLen = UserProfile.getBCData().souls.size() - 1;
                replyToMessageSafely(ch, LangID.getStringByID("soul_range", lang).replace("_", String.valueOf(soulLen)), loader.getMessage(), a -> a);
            }
        }
    }

    private int checkParameters(String message) {
        String[] msg = message.split(" ");

        int result = 1;

        if(msg.length >= 2) {
            String[] pureMessage = message.split(" ", 2)[1].toLowerCase(Locale.ENGLISH).split(" ");

            label:
            for(int i = 0; i < pureMessage.length; i++) {
                switch (pureMessage[i]) {
                    case "-d", "-debug" -> {
                        if ((result & PARAM_DEBUG) == 0) {
                            result |= PARAM_DEBUG;
                        } else {
                            break label;
                        }
                    }
                    case "-r", "-raw" -> {
                        if ((result & PARAM_RAW) == 0) {
                            result |= PARAM_RAW;
                        } else {
                            break label;
                        }
                    }
                    case "-f", "-fr" -> {
                        if (i < pureMessage.length - 1 && StaticStore.isNumeric(pureMessage[i + 1])) {
                            i++;
                        } else {
                            break label;
                        }
                    }
                    case "-g", "-gif" -> {
                        if ((result & PARAM_GIF) == 0) {
                            result |= PARAM_GIF;
                        } else {
                            break label;
                        }
                    }
                }
            }
        }

        return result;
    }

    private int getFrame(String message) {
        String [] msg = message.split(" ");

        for(int i = 0; i < msg.length; i++) {
            if(msg[i].equals("-f") || msg[i].equals("-fr")) {
                if(i < msg.length - 1 && StaticStore.isNumeric(msg[i+1])) {
                    return StaticStore.safeParseInt(msg[i+1]);
                }
            }
        }

        return -1;
    }

    private int findSoulID(String content) {
        String[] contents = content.split(" ");

        boolean frame = false;

        for(int i = 0; i < contents.length; i++) {
            if((contents[i].equals("-f") || contents[i].equals("-fr")) && i < contents.length - 1 && StaticStore.isNumeric(contents[i + 1]) && !frame) {
                frame = true;

                i++;
            } else if(StaticStore.isNumeric(contents[i]))
                return StaticStore.safeParseInt(contents[i]);
        }

        return -1;
    }
}
