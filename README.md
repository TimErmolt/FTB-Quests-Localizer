# Overview

This is a fork of [FTB Quests Localizer](https://github.com/Litchiiiiii/FTB-Quests-Localizer) by [Litchiiiiii](https://github.com/Litchiiiiii),

which is a fork of [FTB Quests Localization](https://github.com/Mrbysco/FTB-Quests-Localization) by [Mrbysco](https://github.com/Mrbysco),

which is a fork of [FTB Quests Localization](https://github.com/TheonlyTazz/FTB-Quests-Localization) by [TheonlyTazz](https://github.com/TheonlyTazz),

which is a fork of the now-archived [FTB Quest Language Generator](https://github.com/Horeak/ftb-quest-lang-generator) by [Horeak](https://github.com/Horeak),

all distributed under the MIT license, as is this repository.

> [!NOTE]
> This mod is newly released. Please report unexpected behavior and technical issues.

# What is different from the original FTB Quests Localizer?

When exporting FTBQ elements to language files, FTBQL uses an arbitrary quests-by-chapter numbering system for naming the lang keys (e.g. "chapter1.quest1"). Some developers may find this inconvenient to work with, as determining which quest a given lang string is associated with can be tricky.

FTB Quests **Precision** Localizer instead uses a given element's hexadecimal ID, as it is presented in the code, to name the lang keys associated with it. Thus, the element can easily be found by searching through the quest files, as every FTBQ element is given a unique ID.

FTBQPL also supports modpacks with multiple available difficulties. See [the wiki](https://github.com/TimErmolt/FTB-Quests-Precision-Localizer/wiki) for details on using the mod. 

# Using the mod -- TL;DR

Install the latest release for your pack's Minecraft and Forge version, join any world and type in the following command:

```
/ftblang <lang> <prefix> <mode>
```

- `<lang>.json` will be the name of the generated language file. See [this list](https://minecraft.wiki/w/Language) of languages available in Minecraft and the filenames associated with each;

- All generated lang keys will start with `<prefix>` ("**ftbquests**" by default);

- `<mode>` is the current difficulty the pack is set to. **Use "normal" if your pack has no difficulty options.** Otherwise, please consult [the wiki](https://github.com/TimErmolt/FTB-Quests-Precision-Localizer/wiki) for a guide on using this argument.

A notification will appear above your hotbar indicating that the export is successful. All generated files will be available in the **FTBLang** folder located in your game instance's root folder.
