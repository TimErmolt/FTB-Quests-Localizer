package timermolt.ftbqpl.handler.impl;

import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.ChapterGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.util.TextUtils;
import timermolt.ftbqpl.handler.FtbQHandler;
import timermolt.ftbqpl.service.impl.JSONService;
import timermolt.ftbqpl.utils.HandlerHelper;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class Handler implements FtbQHandler {
    private final TreeMap<String, String> transKeys = HandlerHelper.transKeys;
    private final JSONService handleJSON = new JSONService();

    @Override
    public void handleRewardTables(List<RewardTable> rewardTables) {
        rewardTables.forEach(rewardTable -> {
            String textKey = HandlerHelper.getPrefix() + ".reward_table." + GetHexID(rewardTable);
            transKeys.put(textKey, rewardTable.getRawTitle());
            rewardTable.setRawTitle("{" + textKey + "}");
        });
    }

    @Override
    public void handleChapterGroup(ChapterGroup chapterGroup) {
        if(chapterGroup.getTitle() != null){
            if (!chapterGroup.getRawTitle().isEmpty()){
                String textKey = HandlerHelper.getPrefix() + ".chapter_group." + GetHexID(chapterGroup);
                transKeys.put(textKey, chapterGroup.getRawTitle());
                chapterGroup.setRawTitle("{" + textKey + "}");
            }
        }
    }

    @Override
    public void handleChapter(Chapter chapter) {
        //HandlerHelper.setPrefix("ftbquests.chapter."+chapter.getFilename());
        String prefix = HandlerHelper.getPrefix() + ".chapter." + GetHexID(chapter);
        if(chapter.getTitle() != null){
            transKeys.put(prefix + ".title", chapter.getRawTitle());
            chapter.setRawTitle("{" + prefix + ".title" + "}");
        }
        if(!chapter.getRawSubtitle().isEmpty()){
            transKeys.put(prefix + ".subtitle", String.join("\n", chapter.getRawSubtitle()));
            try {
                Field rawSubtitle = chapter.getClass().getDeclaredField("rawSubtitle");
                rawSubtitle.setAccessible(true);
                List<String> subTitleList = new ArrayList<>();
                subTitleList.add("{" + prefix + ".subtitle" + "}");
                rawSubtitle.set(chapter,subTitleList);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void handleTasks(List<Task> tasks) {
        tasks.stream().filter(task -> !task.getRawTitle().isEmpty()).forEach(task -> {
            String textKey = HandlerHelper.getPrefix() + ".task." + GetHexID(task);
            transKeys.put(textKey, task.getRawTitle());
            task.setRawTitle("{" + textKey + "}");
        });
        HandlerHelper.setCounter(0);
    }
    private void handleRewards(List<Reward> rewards) {
        rewards.stream().filter(reward -> !reward.getRawTitle().isEmpty()).forEach(reward -> {
            String textKey = HandlerHelper.getPrefix() + ".reward." + GetHexID(reward);
            transKeys.put(textKey, reward.getRawTitle());
            reward.setRawTitle("{"+textKey+"}");
        });
        HandlerHelper.setCounter(0);
    }

    @Override
    public void handleQuests(List<Quest> allQuests) {
        allQuests.forEach(quest ->{
            //HandlerHelper.setPrefix("ftbquests.chapter." + quest.getChapter().getFilename() + ".quest" + HandlerHelper.getQuests());
            String prefix = HandlerHelper.getPrefix() + ".quest." + GetHexID(quest);
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
            handleTasks(quest.getTasksAsList());
            handleRewards(quest.getRewards().stream().toList());
            handleQuestDescriptions(quest.getRawDescription(), prefix);

            quest.getRawDescription().clear();
            quest.getRawDescription().addAll(HandlerHelper.descList);
            HandlerHelper.descList.clear();
        });
    }

    private void handleQuestDescriptions(List<String> descriptions, String prefix) {
        String rich_desc_regex = "\\s*[\\[\\{].*\"+.*[\\]\\}]\\s*";
        Pattern rich_desc_pattern = Pattern.compile(rich_desc_regex);

        descriptions.forEach(desc -> {

            if (desc.isBlank()) {
                HandlerHelper.descList.add("");
            }
            else if (desc.contains("{image:")){
                handleDescriptionImage(desc, prefix);
            }
            else if(desc.contains("{@pagebreak}")){
                HandlerHelper.descList.add(desc);
            }
            else if(rich_desc_pattern.matcher(desc).find()){
                HandlerHelper.addDescription();
                Component parsedText = TextUtils.parseRawText(desc);
                handleJSON.handleJSON(parsedText, prefix);
            }
            else {
                HandlerHelper.addDescription();
                String textKey = prefix + ".description" + HandlerHelper.getDescription();
                transKeys.put(textKey, desc);
                HandlerHelper.descList.add("{"+textKey+"}");
            }
        });
        HandlerHelper.setDescription(0);
        HandlerHelper.setImage(0);
    }

    private void handleDescriptionImage(String desc, String prefix) {
        String imgKey = prefix + ".image" + HandlerHelper.getImage();
        transKeys.put(imgKey, desc);
        HandlerHelper.descList.add("{" + imgKey + "}");
        HandlerHelper.addImage();
    }

    /** 
     * Quest objects IDs are returned as Longs, but are actually hex numbers in Strings in files.
     * So this function takes care of that headache and makes sure a leading zero is maintained if it's present.
    */
    private String GetHexID(QuestObjectBase object) {
        String hex_id = Long.toString(QuestObjectBase.getID(object), 16).toUpperCase();
        if(hex_id.length() == 15) {
            hex_id = "0" + hex_id;
        }
        return hex_id;
    }
}
