/*
 * This file is part of the CoreLib project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2024  Sakura Ryoko and contributors
 *
 * CoreLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CoreLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with CoreLib.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sakuraryoko.corelib.api.modinit;

import com.sakuraryoko.corelib.api.text.ITextHandler;
import com.sakuraryoko.corelib.impl.CoreLib;
import com.sakuraryoko.corelib.impl.text.BuiltinTextHandler;
import net.minecraft.DetectedVersion;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforgespi.language.IConfigurable;
import net.neoforged.neoforgespi.language.IModInfo;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModInitData
{
    public static final List<String> BASIC_INFO = Arrays.asList("ver", "auth", "desc");
    public static final List<String> ALL_INFO = Arrays.asList("ver", "auth", "con", "lic", "home", "src", "iss", "desc");
    private String MOD_ID;
    private String mcVersion;
    private FMLLoader instance;
    private ModContainer modContainer;
    private IModInfo modMetadata;
	private IConfigurable modConfigurable;
    private String modName;
    private String modVersion;
    private String description;
    private String authors;
    private String license;
    private String authorString;
    private String licenseString;
    private String homepage;
    private String issues;
    private ITextHandler iTextUtils;

    // Server mode
    private boolean integratedServer;
    private boolean dedicatedServer;
    private boolean openToLan;

    public ModInitData(String modID)
    {
        if (modID.isEmpty())
        {
            return;
        }

	    this.mcVersion = DetectedVersion.BUILT_IN.name();
        this.MOD_ID = modID;
        this.integratedServer = false;
        this.dedicatedServer = false;
        this.openToLan = false;
        this.iTextUtils = BuiltinTextHandler.getInstance();

        if (ModList.get().getModContainerById(this.MOD_ID).isPresent())
        {
            this.modContainer = ModList.get().getModContainerById(this.MOD_ID).get();
            this.modMetadata = modContainer.getModInfo();
			this.modConfigurable = this.modMetadata.getConfig();
            this.modVersion = this.modMetadata.getOwningFile().versionString();
            this.modName = this.modMetadata.getDisplayName();
            this.description = this.modMetadata.getDescription();
            this.authors = this.modConfigurable.getConfigElement("authors").map(Object::toString).orElse("");
            this.license = this.modMetadata.getOwningFile().getLicense();
            this.homepage = this.modConfigurable.getConfigElement("displayURL").map(Object::toString).orElse("");
            this.issues = this.modConfigurable.getConfigElement("issueTrackerURL").map(Object::toString).orElse("");
	        this.authorString = this.authors;
	        this.licenseString = this.license;
        }
    }

    public String getMCVersion() {return this.mcVersion;}

    public String getModID() {return this.MOD_ID;}

    public ITextHandler getTextHandler()
    {
        return this.iTextUtils;
    }

    public void setTextHandler(@Nonnull ITextHandler handler)
    {
        this.iTextUtils = handler;
    }

	public boolean isClient()
	{
		return FMLEnvironment.getDist().isClient();
	}

	public boolean isServer()
	{
		return FMLEnvironment.getDist().isDedicatedServer();
	}

    public boolean isIntegratedServer() {return this.integratedServer;}

    public boolean isDedicatedServer() {return this.dedicatedServer;}

    public boolean isOpenToLan() {return this.openToLan;}

    public void setIntegratedServer(boolean toggle)
    {
        this.integratedServer = toggle;
    }

    public void setDedicatedServer(boolean toggle)
    {
        this.dedicatedServer = toggle;
    }

    public void setOpenToLan(boolean toggle)
    {
        if (toggle)
        {
            this.openToLan = true;
            this.integratedServer = true;
        }
        else
        {
            this.openToLan = false;
        }
    }

    public String getModVersion() {return this.modVersion;}

    public String getModName() {return this.modName;}

    public String getModDesc() {return this.description;}

    public String getModLicense() {return this.license;}

    public String getModAuthor$String() {return this.authorString;}

    public String getModLicense$String() {return this.licenseString;}

    public String getModHomepage() {return this.homepage;}

    public String getModIssues() {return this.issues;}

    public Map<String, String> getModBasicInfo()
    {
        Map<String, String> basicInfo = new HashMap<>();

        basicInfo.put("ver", this.modName + "-" + this.mcVersion + "-" + this.modVersion);

        if (!this.authorString.isEmpty())
        {
            basicInfo.put("auth", "Author: " + this.authorString);
        }

        if (!this.licenseString.isEmpty())
        {
            basicInfo.put("lic", "License: " + this.licenseString);
        }

        if (!this.homepage.isEmpty())
        {
            basicInfo.put("home", "Homepage: " + this.homepage);
        }

        if (!this.issues.isEmpty())
        {
            basicInfo.put("iss", "Issues: " + this.issues);
        }

        if (!this.description.isEmpty())
        {
            basicInfo.put("desc", "Description: " + this.description);
        }

        return basicInfo;
    }

    public Map<String, Component> getModFormattedInfo()
    {
        Map<String, Component> fmtInfo = new HashMap<>();

        fmtInfo.put("ver", this.iTextUtils.of(this.modName + "-" + this.mcVersion + "-" + this.modVersion));

        if (!this.authorString.isEmpty())
        {
            fmtInfo.put("auth", this.iTextUtils.formatTextSafe("Author: §d" + this.authorString + "§r"));
        }

        if (!this.licenseString.isEmpty())
        {
            fmtInfo.put("lic", this.iTextUtils.formatTextSafe("License: §e" + this.licenseString + "§r"));
        }

        if (!this.homepage.isEmpty())
        {
            fmtInfo.put("home", this.iTextUtils.formatTextSafe("Homepage: §3" + this.homepage + "§r"));
        }

        if (!this.issues.isEmpty())
        {
            fmtInfo.put("iss", this.iTextUtils.formatTextSafe("Issues: §3" + this.issues + "§r"));
        }

        if (!this.description.isEmpty())
        {
            fmtInfo.put("desc", this.iTextUtils.formatTextSafe("Description: §9" + this.description + "§r"));
        }

        return fmtInfo;
    }

    public Map<String, Component> getModFormattedInfoForPlaceholder()
    {
        Map<String, Component> fmtInfo = new HashMap<>();

        fmtInfo.put("ver", this.iTextUtils.of(this.modName + "-" + this.mcVersion + "-" + this.modVersion));

        if (!this.authorString.isEmpty())
        {
            fmtInfo.put("auth", this.iTextUtils.formatTextSafe("Author: <pink>" + this.authorString + "</pink>"));
        }

        if (!this.licenseString.isEmpty())
        {
            fmtInfo.put("lic", this.iTextUtils.formatTextSafe("License: <yellow>" + this.licenseString + "</yellow>"));
        }

        if (!this.homepage.isEmpty())
        {
            fmtInfo.put("home", this.iTextUtils.formatTextSafe("Homepage: <cyan><url:'" + this.homepage + "'>" + this.homepage + "</url></cyan>"));
        }

        if (!this.issues.isEmpty())
        {
            fmtInfo.put("iss", this.iTextUtils.formatTextSafe("Issues: <cyan><url:'" + this.issues + "'>" + this.issues + "</url></cyan>"));
        }

        if (!this.description.isEmpty())
        {
            fmtInfo.put("desc", this.iTextUtils.formatTextSafe("Description: <light_blue>" + this.description + "</light_blue>"));
        }

        return fmtInfo;
    }

    public void reset()
    {
        CoreLib.debugLog("ModInitData: reset()");
        this.integratedServer = false;
        this.openToLan = false;
        this.dedicatedServer = false;
    }
}
