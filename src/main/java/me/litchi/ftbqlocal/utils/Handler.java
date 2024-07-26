package me.litchi.ftbqlocal.utils;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.ftb.mods.ftbquests.quest.*;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.util.TextUtils;
import me.litchi.ftbqlocal.FtbQuestLocalizerMod;
import net.minecraft.Util;
import net.minecraft.network.chat.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class Handler {
    private final TreeMap<String, String> transKeys;
    private String prefix;
    private int chapters = 0;
    private int quests = 0;
    private int counter = 0;
    private int description = 0;
    private int image = 0;
    private final List<String> descList = new ArrayList<>();
    private static final Logger log = FtbQuestLocalizerMod.log;




    public Handler(){
        this.transKeys = new TreeMap<>();
    }

    public void handleRewardTables(List<RewardTable> rewardTables){
        rewardTables.forEach(rewardTable -> {
            counter++;
            transKeys.put("ftbquests.loot_table.title"+counter, rewardTable.getRawTitle());
            rewardTable.setRawTitle("{" + "ftbquests.loot_table.title" + counter + "}");
        });
        counter = 0;
    }

    public void handleChapterGroup(ChapterGroup chapterGroup){
        if(chapterGroup.getTitle() != null){
            if (!chapterGroup.getRawTitle().isEmpty()){
                transKeys.put("ftbquests.chapter_groups.title" + counter, chapterGroup.getRawTitle());
                chapterGroup.setRawTitle("{" + "ftbquests.chapter_groups.title" + counter + "}");
                counter++;
            }
        }
        //counter = 0;
    }

    public void handleChapter(Chapter chapter){
        prefix = "ftbquests.chapter."+chapter.getFilename();
        if(chapter.getTitle() != null){
            transKeys.put(prefix + ".title", chapter.getRawTitle());
            chapter.setRawTitle("{" + prefix + ".title" + "}");
        }
        if(!chapter.getRawSubtitle().isEmpty()){
            transKeys.put(prefix + ".subtitle", String.join("\n", chapter.getRawSubtitle()));
            chapter.getRawSubtitle().clear();
            chapter.getRawSubtitle().add("{" + prefix + ".subtitle" + "}");
        }


//        handleChapterImages(chapter.getImages());
        handleQuests(chapter.getQuests());

    }

    private void handleTasks(List<Task> tasks){

        tasks.stream().filter(task -> !task.getRawTitle().isEmpty()).forEach(task -> {
            counter++;
            String textKey = prefix + ".task.title" + counter;
            transKeys.put(textKey, task.getRawTitle());
            task.setRawTitle("{"+textKey+"}");
        });
        counter = 0;

    }

    private void handleRewards(List<Reward> rewards){
        rewards.stream().filter(reward -> !reward.getRawTitle().isEmpty()).forEach(reward -> {
            counter++;
            String textKey = prefix + ".reward.title" + counter;
            transKeys.put(textKey, reward.getRawTitle());
            descList.add("{"+textKey+"}");
        });
        counter = 0;

    }

    private void handleQuests(List<Quest> allQuests) {
        allQuests.forEach(quest ->{
            quests++;
            prefix = "ftbquests.chapter." + quest.getChapter().getFilename() + ".quest" + quests;
            if(quest.getTitle() != null){
                if (!quest.getRawTitle().isEmpty()){
                    transKeys.put(prefix + ".title", quest.getRawTitle());
                    quest.setRawTitle("{" + prefix + ".title" + "}");
                }
            }
            if(!quest.getRawSubtitle().isEmpty()){
                transKeys.put(prefix + ".subtitle", quest.getRawSubtitle());
                quest.setRawSubtitle("{" + prefix + ".subtitle" + "}");
            }
            //System.out.println("Task:" + quest.getTasksAsList());
            //System.out.println("Des:" + quest.getRawDescription());
            handleTasks(quest.getTasksAsList());
            handleRewards(quest.getRewards().stream().toList());
            handleQuestDescriptions(quest.getRawDescription());

            quest.getRawDescription().clear();
            quest.getRawDescription().addAll(descList);
            descList.clear();
        });
        quests = 0;
    }

    private void handleQuestDescriptions(List<String> descriptions) {
        descriptions.forEach(desc -> {
            image++;

            if (desc.isBlank()) {
                descList.add("");
            }
            else if (desc.contains("{image:")){
                handleDescriptionImage(desc);
            }
            else if(desc.contains("{@pagebreak}")){
                descList.add(desc);
            }
            else if((desc.startsWith("[") && desc.endsWith("]")) || (desc.startsWith("{") && desc.endsWith("}"))){
                description++;
                Component parsedText = TextUtils.parseRawText(desc);
                handleJSON(parsedText); // This is gonna be messy
            }
            else {
                description++;
                String textKey = prefix + ".description" + description;
                transKeys.put(textKey, desc);
                descList.add("{"+textKey+"}");
            }
        });
        description = 0;
        image = 0;
    }

    private void handleJSON(Component parsedText) {
        try{
            String jsonString;
            List<Component> flatList = parsedText.toFlatList();
            int styleInt = 1;
            //counter = 0;
            StringBuilder jsonStringBuilder = new StringBuilder("[\"\",");
            for(Component c : flatList){
                counter++;
                String text = c.getContents().toString().substring(8, c.getContents().toString().length() -1);
                Style style = c.getStyle();

                if(style != Style.EMPTY){
                    jsonStringBuilder.append("{");
                    TextColor color = style.getColor();
                    if(color != null){
                        jsonStringBuilder.append("\"color\":\"").append(color).append("\",");
                    }
                    if (style.isUnderlined()){
                        jsonStringBuilder.append("\"underlined\":"+ 1).append(",");
                    }
                    if (style.isStrikethrough()){
                        jsonStringBuilder.append("\"strikethrough\":"+ 1).append(",");
                    }
                    if (style.isBold()){
                        jsonStringBuilder.append("\"bold\":"+ 1).append(",");
                    }
                    if (style.isItalic()){
                        jsonStringBuilder.append("\"italic\":"+ 1).append(",");
                    }
                    if (style.isObfuscated()){
                        jsonStringBuilder.append("\"obfuscated\":"+ 1).append(",");
                    }
                    String textKey = prefix + ".rich_description" + counter + ".style" + styleInt;
                    transKeys.put(textKey, text);
                    jsonStringBuilder.append("\"translate\":\"").append(textKey).append("\",");

                    ClickEvent clickEvent = style.getClickEvent();
                    if(clickEvent != null){
                        String clickEventValue = clickEvent.getValue();
                        String clickEventAction = clickEvent.getAction().getSerializedName();
                        jsonStringBuilder.append("\"clickEvent\":{\"action\":\"").append(clickEventAction).append("\",\"value\":\"").append(clickEventValue).append("\"}},");
                    }
                    HoverEvent hoverEvent = style.getHoverEvent();
                    if(hoverEvent != null){
                        String hoverEventAction = hoverEvent.getAction().getSerializedName();
                        //TODO: Check if this is the correct way to get the JsonObject from the CODEC
                        JsonObject hoverEventJSON = Util.getOrThrow(HoverEvent.CODEC.encodeStart(JsonOps.INSTANCE, hoverEvent), IllegalStateException::new).getAsJsonObject();
                        System.out.println(hoverEventJSON);
                        JsonObject hoverValue = hoverEventJSON.get("contents").getAsJsonObject();
                        String hoverText = hoverValue.get("text").getAsString();

                        textKey = prefix + ".rich_description" + ".style." + styleInt + ".hover.text." + counter;
                        String hoverString = "\"hoverEvent\":{\"action\":\"" + hoverEventAction + "\",\"contents\":{\"translate\":\"" + textKey +"\"";
                        transKeys.put(textKey, hoverText);

                        hoverString += "}},";
                        jsonStringBuilder.append(hoverString);
                    }
                    styleInt++;
                }
                else{
                    String textKey = prefix + ".rich_description" + counter;
                    transKeys.put(textKey, text);
                    jsonStringBuilder.append("{\"translate\":\"").append(textKey).append("\"},");
                }
            }
            jsonString = jsonStringBuilder.toString();
            jsonString = jsonString.substring(0, jsonString.length()-1);
            jsonString += "]";
            descList.add(jsonString);
        }catch(Exception e){
            log.info(e.getMessage());
        }
    }

//    private void handleChapterImages(List<ChapterImage> chapterImages){
//        chapterImages.stream().filter(chapterImage -> !chapterImage.getImage().isEmpty()).forEach(chapterImage -> {
//            counter++;
//            transKeys.put(prefix + ".image." + counter, String.join("\n", chapterImage.getImage().toString()));
//            chapterImage;
//            chapterImage.hover.add("{" + prefix + ".image." + counter + "}");
//        });
//        counter = 0;
//    }

    private void handleDescriptionImage(String desc){
        //final String regex = "\\{image:.*?}";
        transKeys.put(prefix + ".image." + image, desc);
        descList.add("{" + prefix + ".image." + image + "}");
        image++;
            /*
            final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
            final Matcher matcher = pattern.matcher(desc);

            while (matcher.find()) {
                desc = desc.replace(matcher.group(0), "");
                descList.add(matcher.group(0));
            }
             */
    }

    public TreeMap<String, String> getTransKeys() {
        return transKeys;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }
    public void addChapters() {
        this.chapters++;
    }
    public int getChapters(){
        return this.chapters;
    }
}


