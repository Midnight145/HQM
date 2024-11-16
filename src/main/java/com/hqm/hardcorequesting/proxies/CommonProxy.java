package com.hqm.hardcorequesting.proxies;

import com.hqm.hardcorequesting.quests.Quest;
import com.hqm.hardcorequesting.quests.QuestTicker;

public class CommonProxy {

    public void initSounds(String path) {

    }

    public void initRenderers() {

    }

    public void init() {
        Quest.serverTicker = new QuestTicker(false);
    }

    public boolean isClient() {
        return false;
    }

    public boolean isServer() {
        return true;
    }

}
