package ua.stratos.gtcoreminer;

import java.io.File;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import gregtech.api.enums.Dyes;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.enums.Tier;
import gregtech.api.interfaces.IItemContainer;
import gregtech.api.util.GT_ModHandler;
import gregtech.api.util.GT_Utility;
import gregtech.common.tileentities.machines.multi.GT_MetaTileEntity_AdvMiner2;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

@Mod(modid = "gt_miner_stratos", name = "GT Ore Miner by Stratos", version = "0.1")
public class GTMinerMod {

	public static GT_MetaTileEntity_OreCoreMiner GTCoreMiner;
	public static boolean isUseEnergy = true;

	@EventHandler
	public void init(FMLInitializationEvent event) {
		
		initConfig(event);
		
		GTCoreMiner = new GT_MetaTileEntity_OreCoreMiner(22994, "gt.stratos.orecoreminer", "Ore Core Miner");
		long bits = GT_ModHandler.RecipeBits.NOT_REMOVABLE | GT_ModHandler.RecipeBits.REVERSIBLE
				| GT_ModHandler.RecipeBits.BUFFERED;

		GT_ModHandler.addCraftingRecipe(GTCoreMiner.getStackForm(1L), bits,
				new Object[] { "GDG", "WMW", "TTT", 
						'M', ItemList.Hull_MV, 
						'T', ItemList.Electric_Motor_MV, 
						'D', OrePrefixes.gem.get(Materials.Diamond), 
						'G', OrePrefixes.gear.get(Materials.CobaltBrass), 
						'W', Tier.ELECTRIC[2].mManagingObject });

	}

	public static void initConfig(FMLInitializationEvent event) { 
		Configuration config = new Configuration(new File("config/GTMinerMod.cfg"));
	    try {
	        config.load();
	        Property isUseEnergyProp = config.get(Configuration.CATEGORY_GENERAL, 
	                "isUseEnergy",
	                "true");

	        isUseEnergy = isUseEnergyProp.getBoolean();
	    } catch (Exception e) {
	        // Failed reading/writing, just continue
	    } finally {
	        // Save props to config IF config changed
	        if (config.hasChanged()) config.save();
	    }
	}
}
