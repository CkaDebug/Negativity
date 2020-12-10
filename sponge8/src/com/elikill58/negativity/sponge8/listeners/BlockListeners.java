package com.elikill58.negativity.sponge8.listeners;

import org.spongepowered.api.block.transaction.Operations;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.First;

import com.elikill58.negativity.api.events.EventManager;
import com.elikill58.negativity.api.events.block.BlockBreakEvent;
import com.elikill58.negativity.api.events.block.BlockPlaceEvent;
import com.elikill58.negativity.sponge8.impl.block.SpongeBlock;
import com.elikill58.negativity.sponge8.impl.entity.SpongeEntityManager;

public class BlockListeners {

	@Listener
	public void onBlockChange(ChangeBlockEvent.All e, @First ServerPlayer p) {
		e.getTransactions(Operations.BREAK.get())
			.forEach(transaction -> {
				BlockBreakEvent event = new BlockBreakEvent(SpongeEntityManager.getPlayer(p), new SpongeBlock(transaction.getOriginal()));
				EventManager.callEvent(event);
				if (event.isCancelled()) {
					transaction.invalidate();
				}
			});
		e.getTransactions(Operations.PLACE.get())
			.forEach(transaction -> {
				BlockPlaceEvent event = new BlockPlaceEvent(SpongeEntityManager.getPlayer(p), new SpongeBlock(transaction.getOriginal()));
				EventManager.callEvent(event);
				if (event.isCancelled()) {
					transaction.invalidate();
				}
			});
	}
}