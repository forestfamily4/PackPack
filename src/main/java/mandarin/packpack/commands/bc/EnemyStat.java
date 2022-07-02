package mandarin.packpack.commands.bc;

import common.CommonStatic;
import common.util.Data;
import common.util.lang.MultiLangCont;
import common.util.unit.Enemy;
import mandarin.packpack.commands.ConstraintCommand;
import mandarin.packpack.supporter.StaticStore;
import mandarin.packpack.supporter.bc.EntityFilter;
import mandarin.packpack.supporter.bc.EntityHandler;
import mandarin.packpack.supporter.lang.LangID;
import mandarin.packpack.supporter.server.data.ConfigHolder;
import mandarin.packpack.supporter.server.data.IDHolder;
import mandarin.packpack.supporter.server.holder.EnemyStatMessageHolder;
import mandarin.packpack.supporter.server.slash.SlashOption;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.ArrayList;
import java.util.List;

public class EnemyStat extends ConstraintCommand {
    public static void performInteraction(GenericCommandInteractionEvent interaction) {
        if(interaction.getOptions().isEmpty()) {
            StaticStore.logger.uploadLog("W/EnemyStat::performInteraction - Options are absent!");
            return;
        }

        int lang = LangID.EN;

        if(interaction.getGuild() != null) {
            String gID = interaction.getGuild().getId();

            if(gID.equals(StaticStore.BCU_KR_SERVER))
                lang = LangID.KR;
        }

        if(interaction.getMember() != null) {
            Member m = interaction.getMember();

            if(StaticStore.config.containsKey(m.getId())) {
                lang =  StaticStore.config.get(m.getId()).lang;

                if(lang == -1) {
                    if(interaction.getGuild() == null) {
                        lang = LangID.EN;
                    } else {
                        IDHolder idh = StaticStore.idHolder.get(interaction.getGuild().getId());

                        if(idh == null) {
                            lang = LangID.EN;
                        } else {
                            lang = idh.serverLocale;
                        }
                    }
                }
            }
        }

        List<OptionMapping> options = interaction.getOptions();

        String name = SlashOption.getStringOption(options, "name", "");

        Enemy e = name.isBlank() ? null : EntityFilter.pickOneEnemy(name, lang);

        boolean frame = SlashOption.getBooleanOption(options, "frame", true);
        boolean extra = SlashOption.getBooleanOption(options, "extra", false);

        int[] magnification = {
                SlashOption.getIntOption(options, "magnification", 100),
                SlashOption.getIntOption(options, "atk_magnification", 100)
        };

        final int finalLang = lang;

        if(e == null) {
            interaction.deferReply().allowedMentions(new ArrayList<>()).setContent(LangID.getStringByID("formst_specific", finalLang)).queue();
        } else {
            try {
                EntityHandler.performEnemyEmb(e, interaction, frame, extra, magnification, finalLang);
            } catch (Exception exception) {
                StaticStore.logger.uploadErrorLog(exception, "E/EnemyStat::performInteraction - Failed to show enemy embed");
            }
        }
    }

    private static final int PARAM_SECOND = 2;
    private static final int PARAM_EXTRA = 4;

    private final ConfigHolder config;

    public EnemyStat(ROLE role, int lang, IDHolder id, ConfigHolder config) {
        super(role, lang, id);

        this.config = config;
    }

    @Override
    public void doSomething(GenericMessageEvent event) throws Exception {
        MessageChannel ch = getChannel(event);

        String[] list = getContent(event).split(" ", 2);

        if(list.length == 1 || filterCommand(getContent(event)).isBlank()) {
            ch.sendMessage(LangID.getStringByID("formst_noname", lang)).queue();
        } else {
            ArrayList<Enemy> enemies = EntityFilter.findEnemyWithName(filterCommand(getContent(event)), lang);

            if(enemies.size() == 1) {
                int param = checkParameters(getContent(event));

                int[] magnification = handleMagnification(getContent(event));

                boolean isFrame = (param & PARAM_SECOND) == 0 && config.useFrame;
                boolean isExtra = (param & PARAM_EXTRA) > 0 || config.extra;

                EntityHandler.showEnemyEmb(enemies.get(0), ch, isFrame, isExtra, magnification, lang);
            } else if(enemies.size() == 0) {
                createMessageWithNoPings(ch, LangID.getStringByID("enemyst_noenemy", lang).replace("_", filterCommand(getContent(event))));
            } else {
                StringBuilder sb = new StringBuilder(LangID.getStringByID("formst_several", lang).replace("_", filterCommand(getContent(event))));

                String check;

                if(enemies.size() <= 20)
                    check = "";
                else
                    check = LangID.getStringByID("formst_next", lang);

                sb.append("```md\n").append(LangID.getStringByID("formst_pick", lang)).append(check);

                for(int i = 0; i < 20; i++) {
                    if(i >= enemies.size())
                        break;

                    Enemy e = enemies.get(i);

                    String ename = e.id == null ? "UNKNOWN " : Data.trio(e.id.id)+" ";

                    int oldConfig = CommonStatic.getConfig().lang;
                    CommonStatic.getConfig().lang = lang;

                    if(MultiLangCont.get(e) != null)
                        ename += MultiLangCont.get(e);

                    CommonStatic.getConfig().lang = oldConfig;

                    sb.append(i+1).append(". ").append(ename).append("\n");
                }

                if(enemies.size() > 20)
                    sb.append(LangID.getStringByID("formst_page", lang).replace("_", "1").replace("-", String.valueOf(enemies.size() / 20 + 1)));

                sb.append(LangID.getStringByID("formst_can", lang));
                sb.append("```");

                Message res = getMessageWithNoPings(ch, sb.toString());

                int[] magnification = handleMagnification(getContent(event));

                int param = checkParameters(getContent(event));

                boolean isFrame = (param & PARAM_SECOND) == 0 && config.useFrame;
                boolean isExtra = (param & PARAM_EXTRA) > 0 || config.extra;

                if(res != null) {
                    Member member = getMember(event);

                    if(member != null) {
                        Message msg = getMessage(event);

                        if(msg != null)
                            StaticStore.putHolder(member.getId(), new EnemyStatMessageHolder(enemies, msg, res, ch.getId(), magnification, isFrame, isExtra, lang));
                    }
                }
            }
        }
    }

    private String filterCommand(String msg) {
        String[] content = msg.split(" ");

        boolean isSec = false;
        boolean isExtra = false;
        boolean isLevel = false;

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
                case "-e":
                case "-extra":
                    if(!isExtra)
                        isExtra = true;
                    else {
                        command.append(content[i]);
                        written = true;
                    }
                case "-m":
                    if(!isLevel && i < content.length -1) {
                        String text = getLevelText(content, i + 1);

                        if(text.contains(" ")) {
                            i += text.split(" ").length;
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

        if(command.toString().isBlank())
            return "";

        return command.toString().trim();
    }

    private int checkParameters(String message) {
        String[] msg = message.split(" ");

        int result = 1;

        if(msg.length >= 2) {
            String[] pureMessage = message.split(" ", 2)[1].split(" ");

            for(String str : pureMessage) {
                if(str.startsWith("-s")) {
                    if((result & PARAM_SECOND) == 0) {
                        result |= PARAM_SECOND;
                    } else
                        break;
                } else if(str.startsWith("-e") || str.startsWith("-extra")) {
                    if((result & PARAM_EXTRA) == 0) {
                        result |= PARAM_EXTRA;
                    } else {
                        break;
                    }
                }
            }
        }

        return result;
    }

    private int[] handleMagnification(String msg) {
        if(msg.contains("-m")) {
            String[] content = msg.split(" ");

            for(int i = 0; i < content.length; i++) {
                if(content[i].equals("-m") && i != content.length -1) {
                    String[] trial = getLevelText(content, i+1).replace(" ", "").split(",");

                    int length = 0;

                    for (String s : trial) {
                        if (StaticStore.isNumeric(s))
                            length++;
                        else
                            break;
                    }

                    if(length == 0)
                        return new int[] {100};
                    else {
                        int[] lv = new int[length];

                        for (int j = 0; j < length; j++) {
                            if(trial[j].isBlank() && StaticStore.isNumeric(trial[j])) {
                                lv[j] = 100;
                            } else {
                                lv[j] = StaticStore.safeParseInt(trial[j]);
                            }
                        }

                        return lv;
                    }
                }
            }
        } else {
            return new int[] {100};
        }

        return new int[] {100};
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
                if(!commaStart && commaAdd <= 1) {
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
