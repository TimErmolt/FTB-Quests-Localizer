package net.ftbconv.utils;

import dev.architectury.platform.Platform;

public class Constants {
    public static class PackMCMeta {
        public static final String MOD_NAME = "FTB Quest Localization Files Exporter";
        public static final String PACKNAME = "FTB Quest Localization Files";
        public static final String DESCRIPTION = "Localization Files ";
        // https://minecraft.wiki/w/Pack_format
        public static final int PACKFORMAT = 9;
        public static final String FILEPATH = "pack.mcmeta";// Output file path
        public static final String OUTPUTFOLDER = "FTBLang";
        public static final String KUBEJSFOLDER = "kubejs/assets/ftbquests/lang/";
        public static final String QUESTFOLDER = "config/ftbquests/quests/";
        public static final String BACKUPFOLDER = "backup/ftbquests";
        public static final String GAMEDIR = Platform.getGameFolder().toString();

    }
}
