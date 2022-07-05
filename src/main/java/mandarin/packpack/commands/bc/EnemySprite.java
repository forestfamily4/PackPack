package mandarin.packpack.commands.bc;

import common.util.Data;
import common.util.unit.Enemy;
import mandarin.packpack.commands.ConstraintCommand;
import mandarin.packpack.commands.TimedConstraintCommand;
import mandarin.packpack.supporter.StaticStore;
import mandarin.packpack.supporter.bc.EntityFilter;
import mandarin.packpack.supporter.bc.EntityHandler;
import mandarin.packpack.supporter.lang.LangID;
import mandarin.packpack.supporter.server.data.IDHolder;
import mandarin.packpack.supporter.server.holder.EnemySpriteMessageHolder;
import mandarin.packpack.supporter.server.holder.SearchHolder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;

import java.util.ArrayList;
import java.util.List;

public class EnemySprite extends TimedConstraintCommand {
    private static final int PARAM_EDI = 2;

    public EnemySprite(ConstraintCommand.ROLE role, int lang, IDHolder id, long time) {
        super(role, lang, id, time, StaticStore.COMMAND_ENEMYSPRITE_ID);
    }

    @Override
    public void doSomething(GenericMessageEvent event) throws Exception {
        MessageChannel ch = getChannel(event);

        if(ch == null)
            return;

        String[] contents = getContent(event).split(" ");

        if(contents.length == 1) {
            ch.sendMessage(LangID.getStringByID("eimg_more", lang)).queue();
        } else {
            String search = filterCommand(getContent(event));

            if(search.isBlank()) {
                ch.sendMessage(LangID.getStringByID("eimg_more", lang)).queue();
                return;
            }

            ArrayList<Enemy> enemies = EntityFilter.findEnemyWithName(search, lang);

            if(enemies.isEmpty()) {
                createMessageWithNoPings(ch, LangID.getStringByID("enemyst_noenemy", lang).replace("_", filterCommand(getContent(event))));
                disableTimer();
            } else if(enemies.size() == 1) {
                int param = checkParameter(getContent(event));

                EntityHandler.getEnemySprite(enemies.get(0), ch, getModeFromParam(param), lang);
            } else {
                StringBuilder sb = new StringBuilder(LangID.getStringByID("formst_several", lang).replace("_", filterCommand(getContent(event))));

                sb.append("```md\n").append(LangID.getStringByID("formst_pick", lang));

                List<String> data = accumulateData(enemies);

                for(int i = 0; i < data.size(); i++) {
                    sb.append(i+1).append(". ").append(data.get(i)).append("\n");
                }

                if(enemies.size() > SearchHolder.PAGE_CHUNK)
                    sb.append(LangID.getStringByID("formst_page", lang).replace("_", "1").replace("-", String.valueOf(enemies.size()/SearchHolder.PAGE_CHUNK + 1))).append("\n");

                sb.append("```");

                int param = checkParameter(getContent(event));

                int mode = getModeFromParam(param);

                Message res = registerSearchComponents(ch.sendMessage(sb.toString()).allowedMentions(new ArrayList<>()), enemies.size(), data, lang).complete();

                if(res != null) {
                    Member m = getMember(event);

                    if(m != null) {
                        Message msg = getMessage(event);

                        if(msg != null)
                            StaticStore.putHolder(m.getId(), new EnemySpriteMessageHolder(enemies, msg, res, ch.getId(), mode, lang));
                    }
                }

                disableTimer();
            }
        }
    }

    private int checkParameter(String message) {
        String[] contents = message.split(" ");

        int res = 1;

        for (String content : contents) {
            if ("-edi".equals(content)) {
                res |= PARAM_EDI;
                break;
            }
        }

        return res;
    }

    String filterCommand(String message) {
        String[] contents = message.split(" ");

        if(contents.length == 1)
            return "";

        StringBuilder result = new StringBuilder();

        boolean edi = false;

        for(int i = 1; i < contents.length; i++) {
            boolean written = false;

            if(contents[i].equals("-edi")) {
                if(!edi) {
                    edi = true;
                } else {
                    result.append(contents[i]);
                    written = true;
                }
            } else {
                result.append(contents[i]);
                written = true;
            }

            if(written && i < contents.length - 1)
                result.append(" ");
        }

        return result.toString().trim();
    }

    private int getModeFromParam(int param) {
        if((param & PARAM_EDI) > 0)
            return 3;
        else
            return 0;
    }

    private List<String> accumulateData(List<Enemy> enemies) {
        List<String> data = new ArrayList<>();

        for(int i = 0; i < SearchHolder.PAGE_CHUNK; i++) {
            if(i >= enemies.size())
                break;

            Enemy e = enemies.get(i);

            String ename;

            if(e.id != null) {
                ename = Data.trio(e.id.id)+" ";
            } else {
                ename = " ";
            }

            String name = StaticStore.safeMultiLangGet(e, lang);

            if(name != null)
                ename += name;

            data.add(ename);
        }

        return data;
    }
}
