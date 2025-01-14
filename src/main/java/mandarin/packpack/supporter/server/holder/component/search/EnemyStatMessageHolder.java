package mandarin.packpack.supporter.server.holder.component.search;

import common.util.Data;
import common.util.lang.MultiLangCont;
import common.util.unit.Enemy;
import mandarin.packpack.supporter.bc.EntityHandler;
import mandarin.packpack.supporter.server.data.TreasureHolder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class EnemyStatMessageHolder extends SearchHolder {
    private final ArrayList<Enemy> enemy;

    private final boolean isFrame;
    private final boolean isExtra;
    private final boolean isCompact;
    private final int[] magnification;
    private final TreasureHolder treasure;

    public EnemyStatMessageHolder(ArrayList<Enemy> enemy, @Nonnull Message author, @Nonnull Message msg, String channelID, int[] magnification, boolean isFrame, boolean isExtra, boolean isCompact, TreasureHolder treasure, int lang) {
        super(author, msg, channelID, lang);

        this.enemy = enemy;

        this.magnification = magnification;
        this.isFrame = isFrame;
        this.isExtra = isExtra;
        this.isCompact = isCompact;
        this.treasure = treasure;

        registerAutoFinish(this, msg, lang, FIVE_MIN);
    }

    @Override
    public List<String> accumulateListData(boolean onText) {
        List<String> data = new ArrayList<>();

        for (int i = PAGE_CHUNK * page; i < PAGE_CHUNK * (page + 1); i++) {
            if (i >= enemy.size())
                break;

            Enemy e = enemy.get(i);

            String ename = Data.trio(e.id.id) + " ";

            if (MultiLangCont.get(e, lang) != null)
                ename += MultiLangCont.get(e, lang);

            data.add(ename);
        }

        return data;
    }

    @Override
    public void onSelected(GenericComponentInteractionCreateEvent event) {
        MessageChannel ch = event.getChannel();

        int id = parseDataToInt(event);

        msg.delete().queue();

        try {
            EntityHandler.showEnemyEmb(enemy.get(id), ch, getAuthorMessage(), isFrame, isExtra, isCompact, magnification, treasure, lang);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getDataSize() {
        return enemy.size();
    }
}
