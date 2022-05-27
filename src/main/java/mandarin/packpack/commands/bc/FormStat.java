package mandarin.packpack.commands.bc;

import common.CommonStatic;
import common.util.Data;
import common.util.unit.Form;
import discord4j.core.event.domain.interaction.InteractionCreateEvent;
import discord4j.core.event.domain.message.MessageEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.ApplicationCommandInteractionData;
import discord4j.discordjson.json.ApplicationCommandInteractionOptionData;
import discord4j.discordjson.json.InteractionData;
import discord4j.discordjson.json.MemberData;
import mandarin.packpack.commands.ConstraintCommand;
import mandarin.packpack.supporter.StaticStore;
import mandarin.packpack.supporter.bc.EntityFilter;
import mandarin.packpack.supporter.bc.EntityHandler;
import mandarin.packpack.supporter.lang.LangID;
import mandarin.packpack.supporter.server.data.ConfigHolder;
import mandarin.packpack.supporter.server.data.IDHolder;
import mandarin.packpack.supporter.server.holder.FormButtonHolder;
import mandarin.packpack.supporter.server.holder.FormReactionSlashMessageHolder;
import mandarin.packpack.supporter.server.holder.FormStatMessageHolder;
import mandarin.packpack.supporter.server.slash.SlashBuilder;
import mandarin.packpack.supporter.server.slash.SlashOption;
import mandarin.packpack.supporter.server.slash.WebhookBuilder;

import java.util.ArrayList;
import java.util.List;

public class FormStat extends ConstraintCommand {
    public static WebhookBuilder getInteractionWebhook(InteractionCreateEvent event) {
        InteractionData interaction = event.getInteraction().getData();

        if(interaction.data().isAbsent()) {
            System.out.println("Data is absent!");
            return null;
        }

        ApplicationCommandInteractionData data = interaction.data().get();

        if(data.options().isAbsent()) {
            System.out.println("Options are absent!");
            return null;
        }

        int lang = LangID.EN;

        if(!interaction.guildId().isAbsent()) {
            String gId = interaction.guildId().get();

            if(gId.equals(StaticStore.BCU_KR_SERVER))
                lang = LangID.KR;
        }

        if(!interaction.member().isAbsent()) {
            MemberData m = interaction.member().get();

            if(StaticStore.config.containsKey(m.user().id().asString())) {
                lang = StaticStore.config.get(m.user().id().asString()).lang;
            }
        }

        List<ApplicationCommandInteractionOptionData> options = data.options().get();

        String name = SlashOption.getStringOption(options, "name", "");
        boolean frame = SlashOption.getBooleanOption(options, "frame", true);
        boolean talent = SlashOption.getBooleanOption(options, "talent", false);
        boolean extra = SlashOption.getBooleanOption(options, "extra", false);
        ArrayList<Integer> lvs = prepareLevels(options);

        Form f = EntityFilter.pickOneForm(name, lang);

        int finalLang = lang;

        if(f == null) {
            return SlashBuilder.getWebhookRequest(w -> w.setContent(LangID.getStringByID("formst_specific", finalLang)));
        }

        return SlashBuilder.getWebhookRequest(w -> {
            try {
                EntityHandler.showUnitEmb(f, w, frame, talent, extra, lvs, finalLang);

                w.addPostHandler((g, m) -> {
                            if (!interaction.member().isAbsent()) {
                                MemberData member = interaction.member().get();

                                StaticStore.putHolder(
                                        member.user().id().asString(),
                                        new FormReactionSlashMessageHolder(g, f, member.user().id().asString(), m.channelId().asLong(), m.id().asLong(), frame, talent, extra, lvs, finalLang)
                                );
                            }
                        }
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static ArrayList<Integer> prepareLevels(List<ApplicationCommandInteractionOptionData> options) {
        ArrayList<Integer> levels = new ArrayList<>();

        for(int i = 0; i < 7; i++) {
            if(i == 0)
                levels.add(SlashOption.getIntOption(options, "level", -1));
            else
                levels.add(SlashOption.getIntOption(options, "talent_level_"+i, -1));
        }

        return levels;
    }

    private static final int PARAM_TALENT = 2;
    private static final int PARAM_SECOND = 4;
    private static final int PARAM_EXTRA = 8;

    private final ConfigHolder config;

    public FormStat(ROLE role, int lang, IDHolder holder, ConfigHolder config) {
        super(role, lang, holder);

        this.config = config;
    }

    @Override
    public void doSomething(MessageEvent event) throws Exception {
        MessageChannel ch = getChannel(event);

        String[] list = getContent(event).split(" ",2);

        if(list.length == 1 || filterCommand(getContent(event)).isBlank()) {
            ch.createMessage(LangID.getStringByID("formst_noname", lang)).subscribe();
        } else {
            ArrayList<Form> forms = EntityFilter.findUnitWithName(filterCommand(getContent(event)), lang);

            if (forms.size() == 1) {
                CommonStatic.getConfig().lang = lang;

                int param = checkParameters(getContent(event));

                ArrayList<Integer> lv = handleLevel(getContent(event));

                if(lv.get(0) == -1) {
                    if(forms.get(0).unit.rarity != 0)
                        lv.set(0, config.defLevel);
                }

                boolean isFrame = (param & PARAM_SECOND) == 0 && config.useFrame;
                boolean talent = (param & PARAM_TALENT) > 0;
                boolean extra = (param & PARAM_EXTRA) > 0 || config.extra;

                Message result = EntityHandler.showUnitEmb(forms.get(0), ch, isFrame, talent, extra, lv, lang, true);

                if(result != null) {
                    getMember(event).ifPresent(m -> {
                        Message author = getMessage(event);

                        if(author != null) {
                            StaticStore.putHolder(m.getId().asString(), new FormButtonHolder(forms.get(0), author, result, isFrame, talent, extra, lv, lang, ch.getId().asString(), m.getId().asString()));
                        }
                    });
                }
            } else if (forms.isEmpty()) {
                createMessageWithNoPings(ch, LangID.getStringByID("formst_nounit", lang).replace("_", filterCommand(getContent(event))));
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

                Message res = getMessageWithNoPings(ch, sb.toString());

                int param = checkParameters(getContent(event));

                ArrayList<Integer> lv = handleLevel(getContent(event));

                if(res != null) {
                    getMember(event).ifPresent(member -> {
                        Message msg = getMessage(event);

                        if(msg != null)
                            StaticStore.putHolder(member.getId().asString(), new FormStatMessageHolder(forms, msg, config, res, ch.getId().asString(), param, lv, lang));
                    });
                }

            }
        }
    }

    private int checkParameters(String message) {
        String[] msg = message.split(" ");

        int result =  1;

        if(msg.length >= 2) {
            String[] pureMessage = message.split(" ", 2)[1].split(" ");

            label:
            for(String str : pureMessage) {
                switch (str) {
                    case "-t":
                        if ((result & PARAM_TALENT) == 0) {
                            result |= PARAM_TALENT;
                        } else
                            break label;
                        break;
                    case "-s":
                        if ((result & PARAM_SECOND) == 0) {
                            result |= PARAM_SECOND;
                        } else
                            break label;
                        break;
                    case "-e":
                    case "-extra":
                        if ((result & PARAM_EXTRA) == 0) {
                            result |= PARAM_EXTRA;
                        } else
                            break label;
                        break;
                }
            }
        }

        return result;
    }

    private String filterCommand(String msg) {
        String[] content = msg.split(" ");

        boolean isSec = false;
        boolean isLevel = false;
        boolean isTalent = false;
        boolean isExtra = false;

        StringBuilder command = new StringBuilder();

        for(int i = 1; i < content.length; i++) {
            boolean written = false;

            switch (content[i]) {
                case "-s":
                    if(!isSec)
                        isSec = true;
                    else {
                        command.append(content[i]);
                        written = true;
                    }
                    break;
                case "-t":
                    if(!isTalent)
                        isTalent = true;
                    else {
                        command.append(content[i]);
                        written = true;
                    }
                    break;
                case "-e":
                case "-extra":
                    if(!isExtra)
                        isExtra = true;
                    else {
                        command.append(content[i]);
                        written = true;
                    }
                    break;
                case "-lv":
                    if(!isLevel && i < content.length - 1) {
                        String text = getLevelText(content, i + 1);

                        if(text.contains(" ")) {
                            i += getLevelText(content, i + 1).split(" ").length;
                        } else if(msg.endsWith(text)) {
                            i++;
                        }

                        isLevel = true;
                    } else {
                        command.append(content[i]);
                        written = true;
                    }
                    break;
                default:
                    command.append(content[i]);
                    written = true;
            }

            if(written && i < content.length - 1) {
                command.append(" ");
            }
        }

        if(command.toString().isBlank()) {
            return "";
        }

        return command.toString().trim();
    }

    private ArrayList<Integer> handleLevel(String msg) {
        if(msg.contains("-lv")) {
            String[] content = msg.split(" ");

            for(int i = 0; i < content.length; i++) {
                if(content[i].equals("-lv") && i != content.length -1) {
                    String[] trial = getLevelText(content, i+1).replace(" ", "").split(",");

                    int length = 0;

                    for (String s : trial) {
                        if (StaticStore.isNumeric(s))
                            length++;
                        else
                            break;
                    }

                    if(length == 0) {
                        ArrayList<Integer> res = new ArrayList<>();

                        res.add(-1);

                        return res;
                    }
                    else {
                        ArrayList<Integer> lv = new ArrayList<>();

                        for (int j = 0; j < length; j++) {
                            lv.add(StaticStore.safeParseInt(trial[j]));
                        }

                        return lv;
                    }
                }
            }
        } else {
            ArrayList<Integer> res = new ArrayList<>();

            res.add(-1);

            return res;
        }

        ArrayList<Integer> res = new ArrayList<>();

        res.add(-1);

        return res;
    }

    private String getLevelText(String[] trial, int index) {
        StringBuilder sb = new StringBuilder();

        for(int i = index; i < trial.length; i++) {
            sb.append(trial[i]);

            if(i != trial.length - 1)
                sb.append(" ");
        }

        StringBuilder fin = new StringBuilder();

        boolean commaStart = false;
        boolean beforeSpace = false;
        int numberLetter = 0;
        int commaAdd = 0;

        for(int i = 0; i < sb.length(); i++) {
            if(sb.charAt(i) == ',') {
                if(!commaStart && commaAdd <= 5) {
                    commaStart = true;
                    commaAdd++;
                    fin.append(sb.charAt(i));
                    numberLetter++;
                } else {
                    break;
                }
            } else if(sb.charAt(i) == ' ') {
                beforeSpace = true;
                numberLetter = 0;
                fin.append(sb.charAt(i));
            } else {
                if(Character.isDigit(sb.charAt(i))) {
                    commaStart = false;
                    fin.append(sb.charAt(i));
                    numberLetter++;
                } else if(beforeSpace) {
                    numberLetter = 0;
                    break;
                } else {
                    break;
                }

                beforeSpace = false;
            }

            if(i == sb.length() - 1)
                numberLetter = 0;
        }

        String result = fin.toString();

        result = result.substring(0, result.length() - numberLetter);

        return result;
    }
}
