package net.ftbconv.utils;

import com.google.gson.JsonObject;
import dev.ftb.mods.ftbquests.quest.*;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.util.TextUtils;
import net.ftbconv.FtbLangConvertMod;
import net.minecraft.network.chat.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Handler {
    private final TreeMap<String, String> transKeys;
    private String prefix;
    private int chapters = 0;
    private int quests = 0;
    private int counter = 0;
    private int description = 0;
    private int image = 0;
    private final List<String> descList = new ArrayList<>();
    private static final Logger log = LoggerFactory.getLogger(FtbLangConvertMod.class);




    public Handler(){
        this.transKeys = new TreeMap<>();
    }

    public void handleRewardTables(List<RewardTable> rewardTables){
        rewardTables.forEach(rewardTable -> {
           counter++;
           transKeys.put("loot_table."+counter, rewardTable.getRawTitle());
           rewardTable.setRawTitle("{" + "loot_table." + counter + "}");
        });
        counter = 0;
    }

    public void handleChapterGroup(ChapterGroup chapterGroup){
            counter++;
            if(chapterGroup.getTitle() != null){
                transKeys.put("category." + counter, chapterGroup.getTitle().toString());
                chapterGroup.setRawTitle("{" + "category." + counter + "}");
            }
            counter = 0;
    }

    public void handleChapter(Chapter chapter){
        prefix = "chapter." + chapter.getIndex();
        if(chapter.getTitle() != null){
            transKeys.put(prefix + ".title", chapter.getTitle().toString());
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

        tasks.stream().filter(task -> !task.getRawTitle().isBlank()).forEach(task -> {
            counter++;
            String textKey = prefix + ".task." + counter + ".title";
            transKeys.put(textKey, task.getRawTitle() + "\n");
            descList.add("{"+textKey+"}");
        });
        counter = 0;

    }

    private void handleRewards(List<Reward> rewards){
        rewards.stream().filter(reward -> !reward.getRawTitle().isBlank()).forEach(reward -> {
            counter++;
            String textKey = prefix + ".reward." + counter + ".title";
            transKeys.put(textKey, reward.getTitle() + "\n");
            descList.add("{"+textKey+"}");
        });
        counter = 0;

    }

    private void handleQuests(List<Quest> allQuests) {
        allQuests.forEach(quest ->{
            quests++;
            prefix = "chapter." + chapters + ".quest." + quests;
            if(quest.getTitle() != null){
                transKeys.put(prefix + ".title", quest.getRawTitle());
                quest.setRawTitle("{" + prefix + ".title" + "}");
            }
            if(!quest.getRawSubtitle().isBlank()){
                transKeys.put(prefix + ".subtitle", quest.getRawSubtitle());
                quest.setRawSubtitle("{" + prefix + ".subtitle" + "}");
            }

            handleTasks(quest.getTasksAsList());
            handleRewards((List<Reward>) quest.getRewards());
            handleQuestDescriptions(quest.getRawDescription());

            quest.getRawDescription().clear();
            quest.getRawDescription().addAll(descList);

        });
        quests = 0;
    }

    private void handleQuestDescriptions(List<String> descriptions) {
        descriptions.forEach(desc -> {
            description++;
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
            else if(desc.startsWith("[\"") && desc.endsWith("\"]")){
                Component parsedText = TextUtils.parseRawText(desc);
                handleJSON(parsedText); // This is gonna be messy
            }
            else {
                String textKey = prefix + ".quest." + (quests) + ".description." + description;
                transKeys.put(textKey, desc + "\n");
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
            counter = 0;
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

                    String textKey = prefix + ".quest." + (quests) + ".description." + counter + ".style." + styleInt;
                    transKeys.put(textKey, text+"\n");
                    jsonStringBuilder.append("\"translate\":\"").append(textKey).append("\",");


                    ClickEvent clickEvent = style.getClickEvent();
                    if(clickEvent != null){
                        String clickEventValue = clickEvent.getValue();
                        String clickEventAction = clickEvent.getAction().getName();
                        jsonStringBuilder.append("\"clickEvent\":{\"action\":\"").append(clickEventAction).append("\",\"value\":\"").append(clickEventValue).append("\"},");
                    }
                    HoverEvent hoverEvent = style.getHoverEvent();
                    if(hoverEvent != null){
                        String hoverEventAction = hoverEvent.getAction().getName();
                        JsonObject hoverEventJSON = hoverEvent.serialize();
                        JsonObject hoverValue = hoverEventJSON.get("contents").getAsJsonObject();

                        String hoverText = hoverValue.get("text").getAsString();
                        textKey = prefix + ".quest." + (quests) + ".description" + ".style." + styleInt + ".hover.text." + counter;
                        String hoverString = "\"hoverEvent\":{\"action\":\"" + hoverEventAction + "\",\"contents\":{\"translate\":\"" + textKey +"\"";
                        transKeys.put(textKey, hoverText + "\n");



                        hoverString += "}}},";
                        jsonStringBuilder.append(hoverString);

                    }
                    styleInt++;
                }
                else{
                    String textKey = prefix + ".quest." + (quests) + ".description." + counter;
                    transKeys.put(textKey, text + "\n");
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
            final String regex = "\\{image:.*?}";
            transKeys.put(prefix + ".quest." + (quests) + ".image." + image, "\n");
            descList.add("{" + prefix + ".quest." + (quests) + ".image." + image + "}");
            image++;

            final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
            final Matcher matcher = pattern.matcher(desc);

            while (matcher.find()) {
                desc = desc.replace(matcher.group(0), "");
                descList.add(matcher.group(0));
            }


    }

    public TreeMap<String, String> getTransKeys() {
        return transKeys;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }
}


