package mandarin.packpack.supporter.server.holder.component;

import mandarin.packpack.supporter.StaticStore;
import mandarin.packpack.supporter.server.holder.Holder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

import javax.annotation.Nonnull;

public abstract class ComponentHolder extends Holder {
    public ComponentHolder(@Nonnull Message author, @Nonnull String channelID, @Nonnull String messageID) {
        super(author, channelID, messageID);
    }

    public final long time = System.currentTimeMillis();

    @Override
    public final STATUS handleEvent(Event event) {
        if(event instanceof GenericComponentInteractionCreateEvent componentEvent && canHandleEvent(componentEvent)) {
            onEvent(componentEvent);
        }

        return STATUS.FINISH;
    }

    public abstract void onEvent(GenericComponentInteractionCreateEvent event);

    public int parseDataToInt(GenericComponentInteractionCreateEvent event) {
        if(!(event instanceof StringSelectInteractionEvent)) {
            throw new IllegalStateException("Event type isn't StringSelectInteractionEvent!");
        }

        return StaticStore.safeParseInt(((StringSelectInteractionEvent) event).getValues().get(0));
    }

    private boolean canHandleEvent(GenericComponentInteractionCreateEvent event) {
        return event.getChannel().getId().equals(channelID)
                && event.getMessage().getId().equals(messageID)
                && event.getUser().getId().equals(userID);
    }
}
