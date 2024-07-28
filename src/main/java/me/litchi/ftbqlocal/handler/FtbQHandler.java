package me.litchi.ftbqlocal.handler;

import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.ChapterGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.task.Task;

import java.util.List;

public interface FtbQHandler {
    void handleRewardTables(List<RewardTable> rewardTables);
    void handleChapterGroup(ChapterGroup chapterGroup);
    void handleChapter(Chapter chapter);
    void handleQuests(List<Quest> allQuests);
}