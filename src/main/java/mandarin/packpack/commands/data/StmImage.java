package mandarin.packpack.commands.data;

import common.system.fake.FakeImage;
import common.system.fake.ImageBuilder;
import common.system.files.FDFile;
import common.util.anim.ImgCut;
import mandarin.packpack.commands.ConstraintCommand;
import mandarin.packpack.supporter.FontStageImageGenerator;
import mandarin.packpack.supporter.ImageGenerator;
import mandarin.packpack.supporter.StageImageGenerator;
import mandarin.packpack.supporter.StaticStore;
import mandarin.packpack.supporter.lang.LangID;
import mandarin.packpack.supporter.server.CommandLoader;
import mandarin.packpack.supporter.server.data.IDHolder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class StmImage extends ConstraintCommand {
    private static final int PARAM_REAL = 2;
    private static final int PARAM_JP = 4;
    private static final int PARAM_FORCE = 8;

    public StmImage(ROLE role, int lang, IDHolder holder) {
        super(role, lang, holder, false);
    }

    private int startIndex = 1;

    @Override
    public void prepare() throws Exception {
        registerRequiredPermission(Permission.MESSAGE_ATTACH_FILES);
    }

    @Override
    public void doSomething(CommandLoader loader) {
        try {
            MessageChannel ch = loader.getChannel();

            String [] list = loader.getContent().split(" ");

            if(list.length >= 2) {
                File temp = new File("./temp");

                if(!temp.exists()) {
                    boolean res = temp.mkdirs();

                    if(!res) {
                        System.out.println("Can't create folder : "+temp.getAbsolutePath());
                        return;
                    }
                }

                int param = checkParameters(loader.getContent());

                ImageGenerator generator;
                File f;
                String[] messages = loader.getContent().split(" ", startIndex+1);

                if(messages.length <= startIndex) {
                    replyToMessageSafely(ch, LangID.getStringByID("stimg_more", lang).replace("_", holder == null ? StaticStore.globalPrefix : holder.serverPrefix), loader.getMessage(), a -> a);

                    return;
                }

                String message = messages[startIndex];

                if((param & PARAM_JP) > 0) {
                    File fon = new File("./data/Font.otf");
                    Font font = Font.createFont(Font.TRUETYPE_FONT, fon).deriveFont(102f);

                    if(!FontStageImageGenerator.valid(font, message)) {
                        File alt = new File("./data/ForceFont.otf");

                        font = Font.createFont(Font.TRUETYPE_FONT, alt).deriveFont(102f);
                    }

                    generator = new FontStageImageGenerator(font, 11.5f);
                } else if(!canImage(message) || (param & PARAM_FORCE) > 0) {
                    File fon = new File("./data/ForceFont.otf");
                    Font font = Font.createFont(Font.TRUETYPE_FONT, fon).deriveFont(102f);

                    generator = new FontStageImageGenerator(font, 11.5f);
                } else {
                    ImgCut ic = ImgCut.newIns(new FDFile(new File("./data/stage/stm/Font.imgcut")));
                    FakeImage img = ImageBuilder.builder.build(new File("./data/stage/stm/Font.png"));

                    generator = new StageImageGenerator(ic.cut(img));
                }

                if((param & PARAM_REAL) > 0) {
                    f = generator.generateRealImage(message, false);
                } else {
                    f = generator.generateImage(message, false);
                }

                handleLast(message, f, ch, loader.getMessage(), generator);
            } else {
                replyToMessageSafely(ch, LangID.getStringByID("stmimg_argu", lang).replace("_", holder == null ? StaticStore.globalPrefix : holder.serverPrefix), loader.getMessage(), a -> a);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if(e instanceof ErrorResponseException) {
                onFail(loader, SERVER_ERROR);
            } else {
                onFail(loader, DEFAULT_ERROR);
            }
        }
    }

    private void handleLast(String message, File f, MessageChannel ch, Message reference, ImageGenerator generator) {
        if(f != null) {
            sendMessageWithFile(ch, LangID.getStringByID("stimg_result", lang), f, f.getName(), reference);
        } else {
            ArrayList<String> invalid = generator.getInvalids(message);

            if(invalid.isEmpty()) {
                replyToMessageSafely(ch, LangID.getStringByID("stimg_wrong", lang), reference, a -> a);
            } else {
                StringBuilder builder = new StringBuilder(LangID.getStringByID("stimg_letter", lang));

                for(int i = 0; i < invalid.size(); i++) {
                    if(i == invalid.size() -1) {
                        builder.append(invalid.get(i));
                    } else {
                        builder.append(invalid.get(i)).append(", ");
                    }
                }

                replyToMessageSafely(ch, builder.toString(), reference, a -> a);
            }
        }
    }

    private int checkParameters(String message) {
        String[] msg = message.split(" ");

        int result =  1;

        if(msg.length >= 2) {
            String[] pureMessage = message.split(" ", 2)[1].toLowerCase(Locale.ROOT).split(" ");

            label:
            for(String str : pureMessage) {
                switch (str) {
                    case "-r" -> {
                        if ((result & PARAM_REAL) == 0) {
                            result |= PARAM_REAL;
                            startIndex++;
                        } else
                            break label;
                    }
                    case "-jp" -> {
                        if ((result & PARAM_JP) == 0) {
                            result |= PARAM_JP;
                            startIndex++;
                        } else
                            break label;
                    }
                    case "-f" -> {
                        if ((result & PARAM_FORCE) == 0) {
                            result |= PARAM_FORCE;
                            startIndex++;
                        } else
                            break label;
                    }
                }
            }
        }

        return result;
    }

    private boolean canImage(String message) {
        for(int i = 0; i < message.length(); i++) {
            String str = Character.toString(message.charAt(i));

            if(str.equals(" "))
                continue;

            if(!StageImageGenerator.contains(str)) {
                return false;
            }
        }

        return true;
    }
}
