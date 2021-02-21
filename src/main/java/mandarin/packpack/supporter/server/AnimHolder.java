package mandarin.packpack.supporter.server;

import common.io.assets.UpdateCheck;
import common.system.files.VFile;
import common.util.anim.ImgCut;
import common.util.anim.MaAnim;
import common.util.anim.MaModel;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import mandarin.packpack.supporter.StaticStore;
import mandarin.packpack.supporter.bc.AnimMixer;
import mandarin.packpack.supporter.bc.DataToString;
import mandarin.packpack.supporter.bc.EntityHandler;
import mandarin.packpack.supporter.lang.LangID;

import javax.imageio.ImageIO;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class AnimHolder {
    public static final int RESULT_FAIL = -1;
    public static final int RESULT_STILL = 0;
    public static final int RESULT_FINISH = 1;

    private final AnimMixer mixer;
    private final Message msg;
    private final int lang;
    private final String channelID;
    private final File container;

    private final boolean debug;

    private boolean pngDone = false;
    private boolean cutDone = false;
    private boolean modelDone = false;
    private boolean animDone = false;

    private final AtomicReference<String> png = new AtomicReference<>("PNG : -");
    private final AtomicReference<String> imgcut = new AtomicReference<>("IMGCUT : -");
    private final AtomicReference<String> mamodel = new AtomicReference<>("MAMODEL : -");
    private final AtomicReference<String> maanim = new AtomicReference<>("MAANIM : -");

    private String pngName;
    private String cutName;
    private String modelName;
    private String animName;

    private boolean expired = false;

    public AnimHolder(Message msg, Message target, int lang, String channelID, File container, boolean debug, MessageChannel ch) throws Exception {
        this.msg = target;
        this.lang = lang;
        this.channelID = channelID;
        this.container = container;

        this.debug = debug;

        mixer = new AnimMixer();

        AtomicReference<Long> now = new AtomicReference<>(System.currentTimeMillis());

        System.out.println(msg.getAttachments().isEmpty());

        if(!msg.getAttachments().isEmpty()) {
            for(Attachment a : msg.getAttachments()) {
                System.out.println(a.getFilename());
                if(a.getFilename().endsWith(".png") && mixer.png == null) {
                    UpdateCheck.Downloader down = StaticStore.getDownloader(a, container);

                    if(down != null) {
                        down.run(d -> {
                            String p = "PNG : ";

                            if(d == 1.0) {
                                p += "VALIDATING...";
                            } else {
                                p += DataToString.df.format(d * 100.0) + "%";
                            }

                            png.set(p);

                            if(System.currentTimeMillis() - now.get() >= 1500) {
                                now.set(System.currentTimeMillis());

                                edit();
                            }
                        });

                        File res = new File(container, a.getFilename());

                        if(res.exists()) {
                            if(mixer.validPng(res)) {
                                png.set("PNG : SUCCESS");
                                pngDone = true;
                                pngName = a.getFilename();

                                mixer.png = ImageIO.read(res);
                            } else {
                                png.set("PNG : INVALID");
                            }
                        } else {
                            png.set("PNG : DOWN_FAIL");
                        }

                        edit();
                    }
                } else if((a.getFilename().endsWith(".imgcut") || a.getFilename().contains("imgcut.txt")) && mixer.imgCut == null) {
                    UpdateCheck.Downloader down = StaticStore.getDownloader(a, container);

                    if(down != null) {
                        down.run(d -> {
                            String p = "IMGCUT : ";

                            if(d == 1.0) {
                                p += "VALIDATING...";
                            } else {
                                p += DataToString.df.format(d * 100.0) + "%";
                            }

                            imgcut.set(p);

                            if(System.currentTimeMillis() - now.get() >= 1500) {
                                now.set(System.currentTimeMillis());

                                edit();
                            }
                        });

                        File res = new File(container, a.getFilename());

                        if(res.exists()) {
                            if(mixer.validImgCut(res)) {
                                imgcut.set("IMGCUT : SUCCESS");
                                cutDone = true;
                                cutName = a.getFilename();

                                VFile vf = VFile.getFile(res);

                                if(vf != null) {
                                    mixer.imgCut = ImgCut.newIns(vf.getData());
                                }
                            } else {
                                imgcut.set("IMGCUT : INVALID");
                            }
                        } else {
                            imgcut.set("IMGCUT : DOWN_FAIL");
                        }

                        edit();
                    }
                } else if((a.getFilename().endsWith(".mamodel") || a.getFilename().contains("mamodel.txt")) && mixer.model == null) {
                    UpdateCheck.Downloader down = StaticStore.getDownloader(a, container);

                    if(down != null) {
                        down.run(d -> {
                            String p = "MAMODEL : ";

                            if(d == 1.0) {
                                p += "VALIDATING...";
                            } else {
                                p += DataToString.df.format(d * 100.0) + "%";
                            }

                            mamodel.set(p);

                            if(System.currentTimeMillis() - now.get() >= 1500) {
                                now.set(System.currentTimeMillis());

                                edit();
                            }
                        });

                        File res = new File(container, a.getFilename());

                        if(res.exists()) {
                            if(mixer.validMamodel(res)) {
                                mamodel.set("MAMODEL : SUCCESS");
                                modelDone = true;
                                modelName = a.getFilename();

                                VFile vf = VFile.getFile(res);

                                if(vf != null) {
                                    mixer.model = MaModel.newIns(vf.getData());
                                }
                            } else {
                                mamodel.set("MAMODEL : INVALID");
                            }
                        } else {
                            mamodel.set("MAMODEL : DOWN_FAIL");
                        }

                        edit();
                    }
                } else if((a.getFilename().endsWith(".maanim") || (a.getFilename().startsWith("maanim") && a.getFilename().endsWith(".txt"))) && mixer.anim == null) {
                    UpdateCheck.Downloader down = StaticStore.getDownloader(a, container);

                    if(down != null) {
                        down.run(d -> {
                            String p = "MAANIM : ";

                            if(d == 1.0) {
                                p += "VALIDATING...";
                            } else {
                                p += DataToString.df.format(d * 100.0) + "%";
                            }

                            maanim.set(p);

                            if(System.currentTimeMillis() - now.get() >= 1500) {
                                now.set(System.currentTimeMillis());

                                edit();
                            }
                        });

                        File res = new File(container, a.getFilename());

                        if(res.exists()) {
                            if(mixer.validMaanim(res)) {
                                maanim.set("MAANIM : SUCCESS");
                                animDone = true;
                                animName = a.getFilename();

                                VFile vf = VFile.getFile(res);

                                if(vf != null) {
                                    mixer.anim = MaAnim.newIns(vf.getData());
                                }
                            } else {
                                maanim.set("MAANIM : INVALID");
                            }
                        } else {
                            maanim.set("MAANIM : DOWN_FAIL");
                        }

                        edit();
                    }
                }
            }
        }

        if(pngDone && cutDone && modelDone && animDone) {
            new Thread(() -> {
                try {
                    String id = generateMD5ID();

                    if(id != null) {
                        EntityHandler.generateAnimGif(ch, id, mixer, lang, debug, -1);
                    }

                    StaticStore.deleteFile(container, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            msg.getAuthor().ifPresent(u -> StaticStore.animHolder.put(u.getId().asString(), this));

            Timer autoFinish = new Timer();

            autoFinish.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(expired)
                        return;

                    msg.edit(m -> {
                        m.setContent(LangID.getStringByID("formst_expire", lang));

                        expired = true;

                        msg.getAuthor().ifPresent(u -> StaticStore.animHolder.remove(u.getId().asString()));
                    }).subscribe();
                }
            }, TimeUnit.MINUTES.toMillis(5));
        }
    }

    public int handleEvent(MessageCreateEvent event) throws Exception {
        if(expired) {
            System.out.println("Expired!!");
            return RESULT_FAIL;
        }

        MessageChannel ch = event.getMessage().getChannel().block();

        if(ch == null) {
            return RESULT_STILL;
        }

        if(!ch.getId().asString().equals(channelID)) {
            return RESULT_STILL;
        }

        AtomicReference<Long> now = new AtomicReference<>(System.currentTimeMillis());

        Message m = event.getMessage();

        if(!m.getAttachments().isEmpty()) {
            for(Attachment a : m.getAttachments()) {
                System.out.println(a.getFilename());
                if(a.getFilename().endsWith(".png") && mixer.png == null) {
                    UpdateCheck.Downloader down = StaticStore.getDownloader(a, container);

                    if(down != null) {
                        down.run(d -> {
                            String p = "PNG : ";

                            if(d == 1.0) {
                                p += "VALIDATING...";
                            } else {
                                p += DataToString.df.format(d * 100.0) + "%";
                            }

                            png.set(p);

                            if(System.currentTimeMillis() - now.get() >= 1500) {
                                now.set(System.currentTimeMillis());

                                edit();
                            }
                        });

                        File res = new File(container, a.getFilename());

                        if(res.exists()) {
                            if(mixer.validPng(res)) {
                                png.set("PNG : SUCCESS");
                                pngDone = true;
                                pngName = a.getFilename();

                                mixer.png = ImageIO.read(res);
                            } else {
                                png.set("PNG : INVALID");
                            }
                        } else {
                            png.set("PNG : DOWN_FAIL");
                        }

                        edit();
                    }
                } else if((a.getFilename().endsWith(".imgcut") || a.getFilename().contains("imgcut.txt")) && mixer.imgCut == null) {
                    UpdateCheck.Downloader down = StaticStore.getDownloader(a, container);

                    if(down != null) {
                        down.run(d -> {
                            String p = "IMGCUT : ";

                            if(d == 1.0) {
                                p += "VALIDATING...";
                            } else {
                                p += DataToString.df.format(d * 100.0) + "%";
                            }

                            imgcut.set(p);

                            if(System.currentTimeMillis() - now.get() >= 1500) {
                                now.set(System.currentTimeMillis());

                                edit();
                            }
                        });

                        File res = new File(container, a.getFilename());

                        if(res.exists()) {
                            if(mixer.validImgCut(res)) {
                                imgcut.set("IMGCUT : SUCCESS");
                                cutDone = true;
                                cutName = a.getFilename();

                                VFile vf = VFile.getFile(res);

                                if(vf != null) {
                                    mixer.imgCut = ImgCut.newIns(vf.getData());
                                }
                            } else {
                                imgcut.set("IMGCUT : INVALID");
                            }
                        } else {
                            imgcut.set("IMGCUT : DOWN_FAIL");
                        }

                        edit();
                    }
                } else if((a.getFilename().endsWith(".mamodel") || a.getFilename().contains("mamodel.txt")) && mixer.model == null) {
                    UpdateCheck.Downloader down = StaticStore.getDownloader(a, container);

                    if(down != null) {
                        down.run(d -> {
                            String p = "MAMODEL : ";

                            if(d == 1.0) {
                                p += "VALIDATING...";
                            } else {
                                p += DataToString.df.format(d * 100.0) + "%";
                            }

                            mamodel.set(p);

                            if(System.currentTimeMillis() - now.get() >= 1500) {
                                now.set(System.currentTimeMillis());

                                edit();
                            }
                        });

                        File res = new File(container, a.getFilename());

                        if(res.exists()) {
                            if(mixer.validMamodel(res)) {
                                mamodel.set("MAMODEL : SUCCESS");
                                modelDone = true;
                                modelName = a.getFilename();

                                VFile vf = VFile.getFile(res);

                                if(vf != null) {
                                    mixer.model = MaModel.newIns(vf.getData());
                                }
                            } else {
                                mamodel.set("MAMODEL : INVALID");
                            }
                        } else {
                            mamodel.set("MAMODEL : DOWN_FAIL");
                        }

                        edit();
                    }
                } else if((a.getFilename().endsWith(".maanim") || (a.getFilename().startsWith("maanim") && a.getFilename().endsWith(".txt"))) && mixer.anim == null) {
                    UpdateCheck.Downloader down = StaticStore.getDownloader(a, container);

                    if(down != null) {
                        down.run(d -> {
                            String p = "MAANIM : ";

                            if(d == 1.0) {
                                p += "VALIDATING...";
                            } else {
                                p += DataToString.df.format(d * 100.0) + "%";
                            }

                            maanim.set(p);

                            if(System.currentTimeMillis() - now.get() >= 1500) {
                                now.set(System.currentTimeMillis());

                                edit();
                            }
                        });

                        File res = new File(container, a.getFilename());

                        if(res.exists()) {
                            if(mixer.validMaanim(res)) {
                                maanim.set("MAANIM : SUCCESS");
                                animDone = true;
                                animName = a.getFilename();

                                VFile vf = VFile.getFile(res);

                                if(vf != null) {
                                    mixer.anim = MaAnim.newIns(vf.getData());
                                }
                            } else {
                                maanim.set("MAANIM : INVALID");
                            }
                        } else {
                            maanim.set("MAANIM : DOWN_FAIL");
                        }

                        edit();
                    }
                }
            }

            m.delete().subscribe();

            if(pngDone && cutDone && modelDone && animDone) {
                new Thread(() -> {
                    try {
                        String id = generateMD5ID();

                        if(id != null) {
                            EntityHandler.generateAnimGif(ch, id, mixer, lang, debug, -1);
                        }

                        StaticStore.deleteFile(container, true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();

                return RESULT_FINISH;
            }
        }

        return RESULT_STILL;
    }

    private void edit() {
        msg.edit(m -> m.setContent(png.get()+"\n"+imgcut.get()+"\n"+mamodel.get()+"\n"+maanim.get())).subscribe();
    }

    public String generateMD5ID() {
        if(!container.exists() || container.isFile())
            return null;

        if(pngName == null || cutName == null || modelName == null || animName == null)
            return null;

        File png = new File(container, pngName);

        if(!png.exists())
            return null;

        File cut = new File(container, cutName);

        if(!cut.exists())
            return null;

        File mod = new File(container, modelName);

        if(!mod.exists())
            return null;

        File anim = new File(container, animName);

        if(!anim.exists())
            return null;

        return "C - "+StaticStore.fileToMD5(png)+" - "+StaticStore.fileToMD5(cut)+" - "+StaticStore.fileToMD5(mod)+" - "+StaticStore.fileToMD5(anim);
    }
}