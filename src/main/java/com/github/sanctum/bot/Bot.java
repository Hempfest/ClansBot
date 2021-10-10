package com.github.sanctum.bot;

import com.github.sanctum.bot.api.UserLink;
import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.FileType;

public class Bot extends ClanAddon {

	public static final String CHAT = "887534298096365608";
	public static final String ONLINE = "888839894670127196";
	public static final String VOTE = "888850675314872321";
	public static final String COMMANDS = "755568395671306322";

	@Override
	public boolean persist() {
		return true;
	}

	@Override
	public String getName() {
		return "Bot";
	}

	@Override
	public String getDescription() {
		return "A bot addon.";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public String[] getAuthors() {
		return new String[]{"Hempfest"};
	}

	@Override
	public void onLoad() {
		BotLoader.start(this);
	}

	@Override
	public void onEnable() {

		FileManager d = getFile(FileType.JSON, "discord");
		if (d.getRoot().exists()) {
			for (String key : d.getRoot().getKeys(false)) {
				String value = d.getRoot().getNode(key).toPrimitive().getString();
				UserLink.SET.add(new UserLink(key) {
					@Override
					public String getLink() {
						return value;
					}
				});
			}
		}

	}

	@Override
	public void onDisable() {

		UserLink.SET.forEach(l -> l.save(this));

	}
}
