package mandarin.packpack;

import common.CommonStatic;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.role.RoleDeleteEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.rest.request.RouterOptions;
import mandarin.packpack.commands.*;
import mandarin.packpack.commands.bc.*;
import mandarin.packpack.commands.data.AnimAnalyzer;
import mandarin.packpack.commands.data.StageImage;
import mandarin.packpack.commands.data.StmImage;
import mandarin.packpack.commands.server.*;
import mandarin.packpack.supporter.AssetDownloader;
import mandarin.packpack.supporter.PackContext;
import mandarin.packpack.supporter.StaticStore;
import mandarin.packpack.supporter.bc.DataToString;
import mandarin.packpack.supporter.lang.LangID;
import mandarin.packpack.supporter.server.*;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Usage : java -jar JARNAME DISCORD_BOT_TOKEN IMGUR_API_ACCESS_TOKEN
 */
public class PackBot {
    public static void main(String[] args) {
        initialize(args);

        final String TOKEN = args[0];

        DiscordClientBuilder<DiscordClient, RouterOptions> builder = DiscordClientBuilder.create(TOKEN);

        DiscordClient client = builder.build();

        GatewayDiscordClient gate = client.gateway().login().block();

        if(gate == null) {
            return;
        }

        gate.updatePresence(Presence.online(Activity.playing("p!help, but under Construction!"))).subscribe();

        gate.getGuilds().collectList().subscribe(l -> {
            for (Guild guild : l) {
                if (guild != null) {
                    IDHolder id = StaticStore.idHolder.get(guild.getId().asString());

                    if (id == null) {
                        final IDHolder idh = new IDHolder();

                        String modID = StaticStore.getRoleIDByName("PackPackMod", guild);

                        if(modID == null) {
                            guild.createRole(r -> r.setName("PackPackMod")).subscribe(r -> idh.MOD = r.getId().asString());
                        } else {
                            idh.MOD = modID;
                        }

                        StaticStore.idHolder.put(guild.getId().asString(), idh);
                    } else {
                        //Validate Role
                        String mod = id.MOD;

                        if(mod == null) {
                            String modID = StaticStore.getRoleIDByName("PackPackMod", guild);

                            if(modID == null) {
                                guild.createRole(r -> r.setName("PackPackMod")).subscribe(r -> id.MOD = r.getId().asString());
                            } else {
                                id.MOD = modID;
                            }
                        } else {
                            guild.getRoles().collectList().subscribe(ro -> {
                                for(Role r : ro) {
                                    if(r.getId().asString().equals(mod))
                                        return;
                                }

                                String modID = StaticStore.getRoleIDByName("PackPackMod", guild);

                                if(modID == null) {
                                    guild.createRole(r -> r.setName("PackPackMod")).subscribe(r -> id.MOD = r.getId().asString());
                                } else {
                                    id.MOD = modID;
                                }
                            });
                        }
                    }
                }
            }

            StaticStore.saveServerInfo();
        });

        gate.on(RoleDeleteEvent.class).subscribe(e -> {
            Guild guild = e.getGuild().block();

            if(guild == null)
                return;

            IDHolder holder = StaticStore.idHolder.get(guild.getId().asString());

            if(holder != null) {
                String mod = holder.MOD;

                if(mod == null) {
                    String modID = StaticStore.getRoleIDByName("PackPackMod", guild);

                    if(modID == null) {
                        guild.createRole(r -> r.setName("PackPackMod")).subscribe(r -> holder.MOD = r.getId().asString());
                    } else {
                        holder.MOD = modID;
                    }
                } else {
                    e.getRole().ifPresent(r -> {
                        if(r.getId().asString().equals(mod)) {
                            String modID = StaticStore.getRoleIDByName("PackPackMod", guild);

                            if(modID == null) {
                                guild.createRole(ro -> ro.setName("PackPackMod")).subscribe(ro -> holder.MOD = r.getId().asString());
                            } else {
                                holder.MOD = modID;
                            }
                        }
                    });
                }
            } else {
                final IDHolder idh = new IDHolder();

                String modID = StaticStore.getRoleIDByName("PackPackMod", guild);

                if(modID == null) {
                    guild.createRole(r -> r.setName("PackPackMod")).subscribe(r -> idh.MOD = r.getId().asString());
                } else {
                    idh.MOD = modID;
                }

                StaticStore.idHolder.put(guild.getId().asString(), idh);
            }

            StaticStore.saveServerInfo();
        });

        gate.on(GuildCreateEvent.class).subscribe(e -> {
            Guild guild = e.getGuild();

            IDHolder id = StaticStore.idHolder.get(guild.getId().asString());

            if (id == null) {
                final IDHolder idh = new IDHolder();

                String modID = StaticStore.getRoleIDByName("PackPackMod", guild);

                if(modID == null) {
                    guild.createRole(r -> r.setName("PackPackMod")).subscribe(r -> idh.MOD = r.getId().asString());
                } else {
                    idh.MOD = modID;
                }

                StaticStore.idHolder.put(guild.getId().asString(), idh);
            } else {
                //Validate Role
                String mod = id.MOD;

                if(mod == null) {
                    String modID = StaticStore.getRoleIDByName("PackPackMod", guild);

                    if(modID == null) {
                        guild.createRole(r -> r.setName("PackPackMod")).subscribe(r -> id.MOD = r.getId().asString());
                    } else {
                        id.MOD = modID;
                    }
                } else {
                    guild.getRoles().collectList().subscribe(ro -> {
                        for(Role r : ro) {
                            if(r.getId().asString().equals(mod))
                                return;
                        }

                        String modID = StaticStore.getRoleIDByName("PackPackMod", guild);

                        if(modID == null) {
                            guild.createRole(r -> r.setName("PackPackMod")).subscribe(r -> id.MOD = r.getId().asString());
                        } else {
                            id.MOD = modID;
                        }
                    });
                }
            }
        });

        gate.on(MessageCreateEvent.class)
                .filter(event -> {
                    MessageChannel mc = event.getMessage().getChannel().block();

                    if(mc == null)
                        return false;
                    else {
                        AtomicReference<Boolean> mandarin = new AtomicReference<>(false);
                        AtomicReference<Boolean> isMod = new AtomicReference<>(false);
                        AtomicReference<Boolean> canGo = new AtomicReference<>(true);

                        Guild g = event.getGuild().block();

                        IDHolder ids;

                        if(g != null) {
                            ids = StaticStore.idHolder.get(g.getId().asString());
                        } else {
                            return true;
                        }

                        event.getMember().ifPresent(m -> {
                            mandarin.set(m.getId().asString().equals(StaticStore.MANDARIN_SMELL));

                            if(ids.MOD != null) {
                                isMod.set(StaticStore.rolesToString(m.getRoleIds()).contains(ids.MOD));
                            }

                            ArrayList<String> channels = ids.getAllAllowedChannels(m.getRoleIds());

                            if(channels == null)
                                return;

                            if(channels.isEmpty())
                                canGo.set(false);
                            else {
                                MessageChannel channel = event.getMessage().getChannel().block();

                                if(channel == null)
                                    return;

                                canGo.set(channels.contains(channel.getId().asString()));
                            }
                        });

                        String acc = ids.GET_ACCESS;

                        return ((acc == null || !mc.getId().asString().equals(ids.GET_ACCESS)) && canGo.get()) || mandarin.get() || isMod.get();
                    }
                }).subscribe(event -> {
                    Guild g = event.getGuild().block();
                    IDHolder ids;

                    if(g != null) {
                        ids = StaticStore.idHolder.get(g.getId().asString());
                    } else {
                        ids = new IDHolder();
                    }

                    Message msg = event.getMessage();

                    MessageChannel ch = msg.getChannel().block();

                    event.getMember().ifPresent(m -> {
                        String prefix = StaticStore.getPrefix(m.getId().asString());

                        if(msg.getContent().startsWith(ids.serverPrefix))
                            prefix = ids.serverPrefix;

                        if(msg.getContent().startsWith(StaticStore.serverPrefix))
                            prefix = StaticStore.serverPrefix;

                        if(StaticStore.holderContainsKey(m.getId().asString())) {
                            Holder holder = StaticStore.getHolder(m.getId().asString());

                            int result = holder.handleEvent(event);

                            if(result == Holder.RESULT_FINISH) {
                                holder.clean();
                                StaticStore.removeHolder(m.getId().asString(), holder);
                            } else if(result == Holder.RESULT_FAIL) {
                                System.out.println("ERROR : Expired process tried to be handled : "+m.getId().asString() + " | "+holder.getClass().getName());
                                StaticStore.removeHolder(m.getId().asString(), holder);
                            }
                        }

                        if(ch != null) {
                            IDHolder idh;

                            if(g != null) {
                                idh = StaticStore.idHolder.get(g.getId().asString());
                            } else {
                                idh = new IDHolder();
                            }

                            int lang;

                            if(g != null) {
                                if(g.getId().asString().equals(StaticStore.BCU_SERVER))
                                    lang = LangID.EN;
                                else if(g.getId().asString().equals(StaticStore.BCU_KR_SERVER))
                                    lang = LangID.KR;
                                else
                                    lang = LangID.EN;
                            } else {
                                lang = LangID.EN;
                            }

                            if(StaticStore.locales.containsKey(m.getId().asString())) {
                                lang = StaticStore.locales.get(m.getId().asString());
                            }

                            if(idh == null)
                                idh = StaticStore.idHolder.get(StaticStore.BCU_SERVER);

                            switch (StaticStore.getCommand(msg.getContent(), prefix)) {
                                case "checkbcu":
                                    new CheckBCU(lang, idh).execute(event);
                                    break;
                                case "bcustat":
                                    new BCUStat(lang, idh).execute(event);
                                    break;
                                case "analyze":
                                    new Analyze(ConstraintCommand.ROLE.MANDARIN, lang, idh).execute(event);
                                    break;
                                case "help":
                                    new Help(lang, idh).execute(event);
                                    break;
                                case "prefix":
                                    new Prefix(ConstraintCommand.ROLE.MEMBER, lang, idh).execute(event);
                                    break;
                                case "serverpre":
                                    new ServerPrefix(ConstraintCommand.ROLE.MOD, lang, idh).execute(event);
                                    break;
                                case "save":
                                    new Save(ConstraintCommand.ROLE.MOD, lang, idh).execute(event);
                                    break;
                                case "stimg":
                                case "stimage":
                                case "stageimg":
                                case "stageimage":
                                    new StageImage(ConstraintCommand.ROLE.MEMBER, lang, idh).execute(event);
                                    break;
                                case "stmimg":
                                case "stmimage":
                                case "stagemapimg":
                                case "stagemapimage":
                                    new StmImage(ConstraintCommand.ROLE.MEMBER, lang, idh).execute(event);
                                    break;
                                case "formstat":
                                case "fs":
                                    new FormStat(ConstraintCommand.ROLE.MEMBER, lang, idh).execute(event);
                                    break;
                                case "locale":
                                case "loc":
                                    new Locale(ConstraintCommand.ROLE.MEMBER, lang, idh).execute(event);
                                    break;
                                case "music":
                                case "ms":
                                    new Music(ConstraintCommand.ROLE.MEMBER, lang, idh, "music_").execute(event);
                                    break;
                                case "enemystat":
                                case "es":
                                    new EnemyStat(ConstraintCommand.ROLE.MEMBER, lang, idh).execute(event);
                                    break;
                                case "castle":
                                case "cs":
                                    new Castle(ConstraintCommand.ROLE.MEMBER, lang, idh).execute(event);
                                    break;
                                case "stageinfo":
                                case "si":
                                    new StageInfo(ConstraintCommand.ROLE.MEMBER, lang, idh, 5000).execute(event);
                                    break;
                                case "memory":
                                case "mm":
                                    new Memory(ConstraintCommand.ROLE.MOD, lang, idh).execute(event);
                                    break;
                                case "formimage":
                                case "formimg":
                                case "fimage":
                                case "fimg":
                                    new FormImage(ConstraintCommand.ROLE.MEMBER, lang, idh, 10000).execute(event);
                                    break;
                                case "enemyimage":
                                case "enemyimg":
                                case "eimage":
                                case "eimg":
                                    new EnemyImage(ConstraintCommand.ROLE.MEMBER, lang, idh, 10000).execute(event);
                                    break;
                                case "background":
                                case "bg":
                                    new Background(ConstraintCommand.ROLE.MEMBER, lang, idh, 10000).execute(event);
                                    break;
                                case "test":
                                    new Test(ConstraintCommand.ROLE.MANDARIN, lang, idh, "test").execute(event);
                                    break;
                                case "formgif":
                                case "fgif":
                                case "fg":
                                    new FormGif(ConstraintCommand.ROLE.MEMBER, lang, idh, "gif").execute(event);
                                    break;
                                case "enemygif":
                                case "egif":
                                case "eg":
                                    new EnemyGif(ConstraintCommand.ROLE.MEMBER, lang, idh, "gif").execute(event);
                                    break;
                                case "idset":
                                    new IDSet(ConstraintCommand.ROLE.MOD, lang, idh).execute(event);
                                    break;
                                case "clearcache":
                                    new ClearCache(ConstraintCommand.ROLE.MOD, lang, idh).execute(event);
                                    break;
                                case "aa":
                                case "animanalyzer":
                                    new AnimAnalyzer(ConstraintCommand.ROLE.MOD, lang, idh).execute(event);
                                    break;
                                case "channelpermission":
                                case "channelperm":
                                case "chpermission":
                                case "chperm":
                                case "chp":
                                    new ChannelPermission(ConstraintCommand.ROLE.MOD, lang, idh).execute(event);
                                    break;
                                case "formsprite":
                                case "fsprite":
                                case "formsp":
                                case "fsp":
                                    new FormSprite(ConstraintCommand.ROLE.MEMBER, lang, idh, TimeUnit.SECONDS.toMillis(10)).execute(event);
                                    break;
                                case "enemysprite":
                                case "esprite":
                                case "enemysp":
                                case "esp":
                                    new EnemySprite(ConstraintCommand.ROLE.MEMBER, lang, idh, TimeUnit.SECONDS.toMillis(10)).execute(event);
                                    break;
                                case "medal":
                                case "md":
                                    new Medal(ConstraintCommand.ROLE.MEMBER, lang, idh).execute(event);
                                    break;
                            }
                        }
                    });
        });

        gate.onDisconnect().block();
    }

    public static void initialize(String... arg) {
        if(!StaticStore.initialized) {
            CommonStatic.ctx = new PackContext();
            StaticStore.readServerInfo();

            if(arg.length >= 2) {
                StaticStore.imgur.registerClient(arg[1]);
            }

            AssetDownloader.checkAssetDownload();

            LangID.initialize();

            DataToString.initialize();

            StaticStore.idHolder.put("490262537527623692", new IDHolder(
                    "490935132564357131",
                    "632835571655507968", "490940081738350592",
                    "490940151501946880", "787391428916543488",
                    "632836623931015185", "563745009912774687"
            ));

            StaticStore.idHolder.put("679858366389944409", new IDHolder(
                    "679871555794108416",
                    "679869691656667157", "743808872376041553",
                    "679870744561188919", "800632019418742824",
                    null, "689333420794707984"
            ));

            StaticStore.saver = new Timer();
            StaticStore.saver.schedule(new TimerTask() {
                @Override
                public void run() {
                    System.out.println("Save Process");
                    StaticStore.saveServerInfo();
                }
            }, 0, TimeUnit.MINUTES.toMillis(5));

            StaticStore.initialized = true;
        }
    }
}
