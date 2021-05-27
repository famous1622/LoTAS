package de.pfannekuchen.lotas.mixin;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.pfannekuchen.lotas.core.MCVer;
import de.pfannekuchen.lotas.core.utils.KeybindsUtils;
import de.pfannekuchen.lotas.core.utils.Keyboard;
import de.pfannekuchen.lotas.mods.TickrateChangerMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
	
	@Shadow
	private Screen currentScreen;
	
	@Shadow
	private WorldRenderer worldRenderer;

	private int save;


	@Inject(method = "joinWorld", at = @At("HEAD"))
	public void injectloadWorld(ClientWorld worldClientIn, CallbackInfo ci) {
		
	}
	
	@Inject(method = "tick", at = @At(value="HEAD"))
	public void injectrunTick(CallbackInfo ci) {
		if (TickrateChangerMod.advanceClient) {
			TickrateChangerMod.resetAdvanceClient();
		}
	}
	
	
	@Inject(method = "render", at = @At(value = "HEAD"))
	public void injectrunGameLoop(CallbackInfo ci) {

		if (TickrateChangerMod.tickrate == 0) {
			TickrateChangerMod.timeOffset += System.currentTimeMillis() - TickrateChangerMod.timeSinceZero;
			TickrateChangerMod.timeSinceZero = System.currentTimeMillis();
		}

		if (KeybindsUtils.toggleAdvanceKeybind.wasPressed() && TickrateChangerMod.advanceClient == false && !KeybindsUtils.isFreecaming && currentScreen == null) { 
    		if (TickrateChangerMod.tickrate > 0) {
    			save = TickrateChangerMod.index;
    			TickrateChangerMod.updateTickrate(0);
    			TickrateChangerMod.index = 0;
    		} else {
    			TickrateChangerMod.updateTickrate(TickrateChangerMod.ticks[save]);
    			TickrateChangerMod.index = save;
    		}
    	}
		
		if (TickrateChangerMod.tickrate == 0 && KeybindsUtils.advanceTicksKeybind.wasPressed() /* && !KeybindsUtils.isFreecaming */) {
			TickrateChangerMod.advanceTick();
		}
		boolean flag = false;
		if (KeybindsUtils.increaseTickrateKeybind.wasPressed()) {
			flag = true;
			TickrateChangerMod.index++;
		} else if (KeybindsUtils.decreaseTickrateKeybind.wasPressed()) {
			flag = true;
			TickrateChangerMod.index--;
		}
		if (flag) {
			TickrateChangerMod.index = MCVer.clamp(TickrateChangerMod.index, 0, 11);
			TickrateChangerMod.updateTickrate(TickrateChangerMod.ticks[TickrateChangerMod.index]);
		}

		if (TickrateChangerMod.tickrate == 0 && currentScreen == null && Keyboard.isKeyDown(GLFW.GLFW_KEY_ESCAPE)) {
			((MinecraftClient) (Object) this).openScreen(new GameMenuScreen(false));
			TickrateChangerMod.updateTickrate(KeybindsUtils.savedTickrate);
//    		KeybindsUtils.isFreecaming = false; MCVer.player((Minecraft) (Object) this).noClip = false;
			worldRenderer.reload();
		}
	}
}
