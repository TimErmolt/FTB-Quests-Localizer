package timermolt.ftbqpl.handler;

import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.ChapterGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
//import dev.ftb.mods.ftbquests.quest.reward.Reward;
//import dev.ftb.mods.ftbquests.quest.task.Task;

import java.util.List;

public interface FtbQHandler {
    public void handleRewardTables(List<RewardTable> rewardTables);
    public void handleChapterGroup(ChapterGroup chapterGroup);
    public void handleChapter(Chapter chapter);
    public void handleQuests(List<Quest> allQuests);
}