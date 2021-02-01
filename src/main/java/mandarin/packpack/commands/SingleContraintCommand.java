package mandarin.packpack.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import mandarin.packpack.supporter.StaticStore;
import mandarin.packpack.supporter.lang.LangID;
import mandarin.packpack.supporter.server.IDHolder;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public abstract class SingleContraintCommand implements Command {
    static String ABORT = "ABORT";

    final String constRole;
    protected final int lang;
    final String mainID;
    protected String optionalID = "";
    protected final ArrayList<String> aborts = new ArrayList<>();
    final long time;

    public SingleContraintCommand(ConstraintCommand.ROLE role, int lang, IDHolder id, String mainID, long millis) {
        switch (role) {
            case DEV:
                constRole = id.DEV;
                break;
            case MOD:
                constRole = id.MOD;
                break;
            case MEMBER:
                constRole = id.MEMBER;
                break;
            case PRE_MEMBER:
                constRole = id.PRE_MEMBER;
                break;
            case MANDARIN:
                constRole = "MANDARIN";
                break;
            default:
                throw new IllegalStateException("Invalid ROLE enum : "+role);
        }

        this.lang = lang;
        this.mainID = mainID;
        this.time = millis;

        aborts.add(ABORT);
    }

    @Override
    public void execute(MessageCreateEvent event) {
        prepareAborts();

        Message msg = event.getMessage();
        MessageChannel ch = getChannel(event);

        if(ch == null)
            return;

        AtomicReference<Boolean> isDev = new AtomicReference<>(false);

        msg.getAuthorAsMember().subscribe(m -> {
            String role = StaticStore.rolesToString(m.getRoleIds());

            if(constRole.equals("MANDARIN")) {
                isDev.set(m.getId().asString().equals(StaticStore.MANDARIN_SMELL));
            } else {
                isDev.set(role.contains(constRole) || m.getId().asString().equals(StaticStore.MANDARIN_SMELL));
            }

        }, e -> onFail(event, DEFAULT_ERROR), pause::resume);

        pause.pause(() -> onFail(event, DEFAULT_ERROR));

        if(!isDev.get()) {
            if(constRole.equals("MANDARIN")) {
                ch.createMessage(LangID.getStringByID("const_man", lang)).subscribe();
            } else {
                String role = StaticStore.roleNameFromID(event, constRole);
                ch.createMessage(LangID.getStringByID("const_role", lang).replace("_", role)).subscribe();
            }
        } else {
            try {
                setOptionalID(event);

                String id = mainID+optionalID;

                if(StaticStore.canDo.containsKey(id) && !StaticStore.canDo.get(id)) {
                    ch.createMessage(LangID.getStringByID("single_wait", lang)).subscribe();
                } else {

                    if(!aborts.contains(optionalID)) {
                        pause.reset();

                        System.out.println("Added process : "+id);

                        StaticStore.canDo.put(id, false);

                        Timer timer = new Timer();

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                System.out.println("Remove Process : "+id);
                                StaticStore.canDo.put(id, true);
                            }
                        }, time);

                        doSomething(event);

                        pause.pause(() -> onFail(event, DEFAULT_ERROR));
                    } else {
                        onAbort(event);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();

                onFail(event, DEFAULT_ERROR);
            }

            try {
                onSuccess(event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void doSomething(MessageCreateEvent event) throws Exception {
        doThing(event);

        pause.resume();
    }

    protected abstract void doThing(MessageCreateEvent event) throws Exception;

    protected abstract void setOptionalID(MessageCreateEvent event);

    protected abstract void prepareAborts();

    protected void onAbort(MessageCreateEvent event) {}
}