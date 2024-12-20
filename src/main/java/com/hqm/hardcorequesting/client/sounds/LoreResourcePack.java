package com.hqm.hardcorequesting.client.sounds;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import net.minecraft.client.resources.AbstractResourcePack;
import net.minecraft.util.ResourceLocation;

import com.google.common.collect.Sets;

public class LoreResourcePack extends AbstractResourcePack {

    private static final Set domains = Sets.newHashSet("hqm");

    public LoreResourcePack(File folder) {
        super(folder);
    }

    @Override
    protected InputStream getInputStreamByName(String name) throws IOException {
        return new BufferedInputStream(new FileInputStream(new File(this.resourcePackFile, "lore.ogg")));
    }

    @Override
    protected boolean hasResourceName(String name) {
        return name.contains("lore") && name.endsWith(".ogg") && new File(this.resourcePackFile, "lore.ogg").isFile();
    }

    @Override
    public InputStream getInputStream(ResourceLocation resource) throws IOException {
        return this.getInputStreamByName(
            resource.getResourcePath()
                .replace("sounds/", ""));
    }

    @Override
    public boolean resourceExists(ResourceLocation resource) {
        return hasResourceName(
            resource.getResourcePath()
                .replace("sounds/", ""));
    }

    @Override
    public Set getResourceDomains() {
        return domains;
    }
}
