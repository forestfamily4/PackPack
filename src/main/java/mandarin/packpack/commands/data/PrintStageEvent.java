package mandarin.packpack.commands.data;

import discord4j.core.event.domain.message.MessageEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.MessageCreateFields;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.AllowedMentions;
import mandarin.packpack.commands.ConstraintCommand;
import mandarin.packpack.supporter.StaticStore;
import mandarin.packpack.supporter.lang.LangID;
import mandarin.packpack.supporter.server.data.IDHolder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class PrintStageEvent extends ConstraintCommand {
    public PrintStageEvent(ROLE role, int lang, IDHolder id) {
        super(role, lang, id);
    }

    @Override
    public void doSomething(MessageEvent event) throws Exception {
        MessageChannel ch = getChannel(event);

        if(ch == null)
            return;

        ArrayList<String> result = StaticStore.event.printStageEvent(getLocale(getContent(event)), lang, isFull(getContent(event)));

        if(result.isEmpty()) {
            createMessage(ch, m -> m.content(LangID.getStringByID("chevent_noup", lang)));
            return;
        }

        boolean goWithFile = false;

        for(int k = 0; k < result.size(); k++) {
            if(result.get(k).length() >= 1950) {
                goWithFile = true;
                break;
            }
        }

        if(goWithFile) {
            MessageCreateSpec.Builder builder = MessageCreateSpec.builder();

            StringBuilder total = new StringBuilder(LangID.getStringByID("event_stage", holder.serverLocale).replace("**", "")).append("\n");

            for(int k = 0; k < result.size(); k++) {
                total.append(result.get(k).replace("```scss\n", "").replace("```", ""));

                if(k < result.size() - 1)
                    total.append("\n");
            }

            File temp = new File("./temp");

            if(!temp.exists() && !temp.mkdirs()) {
                StaticStore.logger.uploadLog("Failed to create folder : "+temp.getAbsolutePath());
                return;
            }

            File res = new File(temp, StaticStore.findFileName(temp, "event", ".txt"));

            if(!res.exists() && !res.createNewFile()) {
                StaticStore.logger.uploadLog("Failed to create file : "+res.getAbsolutePath());
                return;
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(res, StandardCharsets.UTF_8));

            writer.write(total.toString());

            writer.close();

            FileInputStream fis = new FileInputStream(res);

            builder.content(LangID.getStringByID("printstage_toolong", holder.serverLocale))
                    .addFile(MessageCreateFields.File.of("stageAndEvent.txt", fis));

            ch.createMessage(builder.build()).subscribe(null, (e) -> {
                StaticStore.logger.uploadErrorLog(e, "Failed to perform uploading stage event data");

                try {
                    fis.close();
                } catch (IOException ex) {
                    StaticStore.logger.uploadErrorLog(ex, "Failed close stream while uploading stage event data");
                }

                if(res.exists() && !res.delete()) {
                    StaticStore.logger.uploadLog("Failed to delete file : "+res.getAbsolutePath());
                }
            }, () -> {
                try {
                    fis.close();
                } catch (IOException e) {
                    StaticStore.logger.uploadErrorLog(e, "Failed close stream while uploading stage event data");
                }

                if(res.exists() && !res.delete()) {
                    StaticStore.logger.uploadLog("Failed to delete file : "+res.getAbsolutePath());
                }
            });
        } else {
            for(int k = 0; k < result.size(); k++) {
                MessageCreateSpec.Builder builder = MessageCreateSpec.builder();

                StringBuilder merge = new StringBuilder();

                if(k == 0) {
                    merge.append(LangID.getStringByID("event_stage", holder.serverLocale)).append("\n\n");
                } else {
                    merge.append("** **\n");
                }

                while(merge.length() < 2000) {
                    if(k >= result.size())
                        break;

                    if(result.get(k).length() + merge.length() >= 2000) {
                        k--;
                        break;
                    }

                    merge.append(result.get(k));

                    if(k < result.size() - 1) {
                        merge.append("\n");
                    }

                    k++;
                }

                builder.content(merge.toString());
                builder.allowedMentions(AllowedMentions.builder().build());

                ch.createMessage(builder.build()).subscribe();
            }
        }
    }

    private int getLang() {
        if(lang >= 1 && lang < 4) {
            return lang;
        } else {
            return 0;
        }
    }

    private int getLocale(String content) {
        String[] contents = content.split(" ");

        for(int i = 0; i < contents.length; i++) {
            switch (contents[i]) {
                case "-en":
                    return LangID.EN;
                case "-tw":
                    return LangID.ZH;
                case "-kr":
                    return LangID.KR;
                case "-jp":
                    return LangID.JP;
            }
        }

        return getLang();
    }

    private boolean isFull(String content) {
        String[] contents = content.split(" ");

        for(int i = 0; i < contents.length; i++) {
            if(contents[i].equals("-f") || contents[i].equals("-full"))
                return true;
        }

        return false;
    }
}
