package mandarin.packpack.supporter.server.holder.component.search;

import common.util.Data;
import mandarin.packpack.supporter.StaticStore;
import mandarin.packpack.supporter.bc.EntityHandler;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;

import java.util.ArrayList;
import java.util.List;

public class MedalMessageHolder extends SearchHolder {
    private final ArrayList<Integer> id;
    
    public MedalMessageHolder(ArrayList<Integer> id, Message author, Message msg, int lang, String channelID) {
        super(author, msg, channelID, lang);

        this.id = id;

        registerAutoFinish(this, msg, lang, FIVE_MIN);
    }

    @Override
    public List<String> accumulateListData(boolean onText) {
        List<String> data = new ArrayList<>();

        for(int i = PAGE_CHUNK * page; i < PAGE_CHUNK * (page + 1) ; i++) {
            if(i >= id.size())
                break;

            String medalName = Data.trio(id.get(i)) + " " + StaticStore.MEDNAME.getCont(id.get(i), lang);

            data.add(medalName);
        }

        return data;
    }

    @Override
    public void onSelected(GenericComponentInteractionCreateEvent event) {
        MessageChannel ch = event.getChannel();

        int i = parseDataToInt(event);

        msg.delete().queue();

        try {
            EntityHandler.showMedalEmbed(id.get(i), ch, getAuthorMessage(), lang);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getDataSize() {
        return id.size();
    }
}
