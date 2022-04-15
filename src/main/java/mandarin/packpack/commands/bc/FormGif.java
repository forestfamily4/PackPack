package mandarin.packpack.commands.bc;

import common.util.Data;
import common.util.unit.Form;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import mandarin.packpack.commands.ConstraintCommand;
import mandarin.packpack.commands.GlobalTimedConstraintCommand;
import mandarin.packpack.supporter.StaticStore;
import mandarin.packpack.supporter.bc.EntityFilter;
import mandarin.packpack.supporter.bc.EntityHandler;
import mandarin.packpack.supporter.lang.LangID;
import mandarin.packpack.supporter.server.holder.FormAnimMessageHolder;
import mandarin.packpack.supporter.server.data.IDHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class FormGif extends GlobalTimedConstraintCommand {
    private final int PARAM_DEBUG = 2;
    private final int PARAM_RAW = 4;
    private final int PARAM_GIF = 8;

    private final GatewayDiscordClient client;

    public FormGif(ConstraintCommand.ROLE role, int lang, IDHolder id, String mainID, GatewayDiscordClient client) {
        super(role, lang, id, mainID, TimeUnit.SECONDS.toMillis(30));

        this.client = client;
    }

    @Override
    protected void doThing(MessageEvent event) throws Exception {
        MessageChannel ch = getChannel(event);

        if(ch == null)
            return;

        Guild g = getGuild(event).block();

        if(g == null)
            return;

        AtomicReference<Boolean> isTrusted = new AtomicReference<>(false);

        getMember(event).ifPresentOrElse(m -> {
            if(StaticStore.contributors.contains(m.getId().asString())) {
                isTrusted.set(true);
            } else if(m.getId().asString().equals(StaticStore.MANDARIN_SMELL)) {
                isTrusted.set(true);
            } else {
                isTrusted.set(false);
            }
        }, () -> isTrusted.set(false));

        String[] list = getContent(event).split(" ");

        if(list.length >= 2) {
            File temp = new File("./temp");

            if(!temp.exists()) {
                boolean res = temp.mkdirs();

                if(!res) {
                    System.out.println("Can't create folder : "+temp.getAbsolutePath());
                    disableTimer();
                    return;
                }
            }

            String search = filterCommand(getContent(event));

            if(search.isBlank()) {
                ch.createMessage(LangID.getStringByID("fimg_more", lang)).subscribe();
                disableTimer();
                return;
            }

            ArrayList<Form> forms = EntityFilter.findUnitWithName(search, lang);

            if(forms.isEmpty()) {
                createMessageWithNoPings(ch, LangID.getStringByID("formst_nounit", lang).replace("_", filterCommand(getContent(event))));
                disableTimer();
            } else if(forms.size() == 1) {
                int param = checkParameters(getContent(event));
                int mode = getMode(getContent(event));
                boolean debug = (param & PARAM_DEBUG) > 0;
                boolean raw = (param & PARAM_RAW) > 0;
                boolean gif = (param & PARAM_GIF) > 0;
                int frame = getFrame(getContent(event));

                if(raw && !isTrusted.get()) {
                    ch.createMessage(LangID.getStringByID("gif_ignore", lang)).subscribe();
                }

                Form f = forms.get(0);

                boolean result = EntityHandler.generateFormAnim(f, ch, client, g.getPremiumTier().getValue(), mode, debug, frame, lang, raw && isTrusted.get(), gif);

                if(raw && isTrusted.get()) {
                    changeTime(TimeUnit.MINUTES.toMillis(1));
                }

                if(!result) {
                    disableTimer();
                }
            } else {
                StringBuilder sb = new StringBuilder(LangID.getStringByID("formst_several", lang).replace("_", filterCommand(getContent(event))));

                String check;

                if(forms.size() <= 20)
                    check = "";
                else
                    check = LangID.getStringByID("formst_next", lang);

                sb.append("```md\n").append(LangID.getStringByID("formst_pick", lang)).append(check);

                for(int i = 0; i < 20; i++) {
                    if(i >= forms.size())
                        break;

                    Form f = forms.get(i);

                    String fname = Data.trio(f.uid.id)+"-"+Data.trio(f.fid)+" ";

                    String name = StaticStore.safeMultiLangGet(f, lang);

                    if(name != null)
                        fname += name;

                    sb.append(i+1).append(". ").append(fname).append("\n");
                }

                if(forms.size() > 20)
                    sb.append(LangID.getStringByID("formst_page", lang).replace("_", "1").replace("-", String.valueOf(forms.size()/20 + 1)));

                sb.append(LangID.getStringByID("formst_can", lang));
                sb.append("```");

                int param = checkParameters(getContent(event));
                int mode = getMode(getContent(event));
                int frame = getFrame(getContent(event));

                boolean raw = (param & PARAM_RAW) > 0;
                boolean gif = (param &  PARAM_GIF) > 0;

                if(raw && !isTrusted.get()) {
                    ch.createMessage(LangID.getStringByID("gif_ignore", lang)).subscribe();
                }

                Message res = getMessageWithNoPings(ch, sb.toString());

                if(res != null) {
                    getMember(event).ifPresent(member -> {
                        Message msg = getMessage(event);

                        if(msg != null)
                            StaticStore.putHolder(member.getId().asString(), new FormAnimMessageHolder(forms, msg, res, client, ch.getId().asString(), mode, frame, false, ((param & PARAM_DEBUG) > 0), lang, true, raw && isTrusted.get(), gif));
                    });
                }

                disableTimer();
            }
        } else {
            ch.createMessage(LangID.getStringByID("fimg_more", lang)).subscribe();
            disableTimer();
        }
    }

    @Override
    protected void setOptionalID(MessageEvent event) {
        optionalID = "";
    }

    @Override
    protected void prepareAborts() {

    }

    private int getMode(String message) {
        String [] msg = message.split(" ");

        for(int i = 0; i < msg.length; i++) {
            if(msg[i].equals("-m") || msg[i].equals("-mode")) {
                if(i < msg.length - 1) {
                    if(LangID.getStringByID("fimg_walk", lang).toLowerCase(java.util.Locale.ENGLISH).contains(msg[i+1].toLowerCase(java.util.Locale.ENGLISH)))
                        return 0;
                    else if(LangID.getStringByID("fimg_idle", lang).toLowerCase(java.util.Locale.ENGLISH).contains(msg[i+1].toLowerCase(java.util.Locale.ENGLISH)))
                        return 1;
                    else if(LangID.getStringByID("fimg_atk", lang).toLowerCase(java.util.Locale.ENGLISH).contains(msg[i+1].toLowerCase(java.util.Locale.ENGLISH)))
                        return 2;
                    else if(LangID.getStringByID("fimg_hb", lang).toLowerCase(java.util.Locale.ENGLISH).contains(msg[i+1].toLowerCase(java.util.Locale.ENGLISH)))
                        return 3;
                    else if(LangID.getStringByID("fimg_enter", lang).toLowerCase(java.util.Locale.ENGLISH).contains(msg[i+1].toLowerCase(java.util.Locale.ENGLISH)))
                        return 4;
                    else if(LangID.getStringByID("fimg_burrdown", lang).toLowerCase(java.util.Locale.ENGLISH).contains(msg[i+1].toLowerCase(java.util.Locale.ENGLISH)))
                        return 4;
                    else if(LangID.getStringByID("fimg_burrmove", lang).toLowerCase(java.util.Locale.ENGLISH).contains(msg[i+1].toLowerCase(java.util.Locale.ENGLISH)))
                        return 5;
                    else if(LangID.getStringByID("fimg_burrup", lang).toLowerCase(java.util.Locale.ENGLISH).contains(msg[i+1].toLowerCase(java.util.Locale.ENGLISH)))
                        return 6;
                } else {
                    return 0;
                }
            }
        }

        return 0;
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

    private int checkParameters(String message) {
        String[] msg = message.split(" ");

        int result = 1;

        if(msg.length >= 2) {
            String[] pureMessage = message.split(" ", 2)[1].toLowerCase(Locale.ENGLISH).split(" ");

            label:
            for(int i = 0; i < pureMessage.length; i++) {
                switch (pureMessage[i]) {
                    case "-d":
                    case "-debug":
                        if((result & PARAM_DEBUG) == 0) {
                            result |= PARAM_DEBUG;
                        } else {
                            break label;
                        }
                        break;
                    case "-r":
                    case "-raw":
                        if((result & PARAM_RAW) == 0) {
                            result |= PARAM_RAW;
                        } else {
                            break label;
                        }
                        break;
                    case "-f":
                    case "-fr":
                        if(i < pureMessage.length - 1 && StaticStore.isNumeric(pureMessage[i+1])) {
                            i++;
                        } else {
                            break label;
                        }
                        break;
                    case "-m":
                    case "-mode":
                        if(i < pureMessage.length -1) {
                            i++;
                        } else {
                            break label;
                        }
                        break;
                    case "-g":
                    case "-gif":
                        if((result & PARAM_GIF) == 0) {
                            result |= PARAM_GIF;
                        } else {
                            break label;
                        }
                        break;
                }
            }
        }

        return result;
    }

    String filterCommand(String message) {
        String[] contents = message.split(" ");

        if(contents.length == 1)
            return "";

        StringBuilder result = new StringBuilder();

        boolean debug = false;
        boolean raw = false;

        boolean mode = false;
        boolean frame = false;
        boolean gif = false;

        for(int i = 1; i < contents.length; i++) {
            boolean written = false;

            switch (contents[i]) {
                case "-debug":
                case "-d":
                    if(!debug) {
                        debug = true;
                    } else {
                        result.append(contents[i]);
                        written = true;
                    }
                    break;
                case "-r":
                case "-raw":
                    if(!raw) {
                        raw = true;
                    } else {
                        result.append(contents[i]);
                        written = true;
                    }
                    break;
                case "-mode":
                case "-m":
                    if(!mode && i < contents.length - 1) {
                        mode = true;
                        i++;
                    } else {
                        result.append(contents[i]);
                        written = true;
                    }
                    break;
                case "-fr":
                case "-f":
                    if(!frame && i < contents.length - 1 && StaticStore.isNumeric(contents[i + 1])) {
                        frame = true;
                        i++;
                    } else {
                        result.append(contents[i]);
                        written = true;
                    }
                    break;
                case "-g":
                case "-gif":
                    if(!gif) {
                        gif = true;
                    } else {
                        result.append(contents[i]);
                        written = true;
                    }
                    break;
                default:
                    result.append(contents[i]);
                    written = true;
            }

            if(written && i < contents.length - 1)
                result.append(" ");
        }

        return result.toString().trim();
    }
}
