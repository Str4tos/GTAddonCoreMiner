package ua.stratos.gtcoreminer;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.util.GT_ModHandler;
import gregtech.common.tileentities.machines.multi.GT_MetaTileEntity_AdvMiner2;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

@Mod(modid = "gt_miner_stratos", name = "GT Ore Miner by Stratos", version = "0.1")
public class GTMinerMod {

	public static GT_MetaTileEntity_OreCoreMiner GTCoreMiner;

	@EventHandler
	public void init(FMLInitializationEvent event) {
		
		GTCoreMiner = new GT_MetaTileEntity_OreCoreMiner(22994, "gt.stratos.orecoreminer", "Ore Core Miner");
		/*long bitsd = GT_ModHandler.RecipeBits.DISMANTLEABLE | GT_ModHandler.RecipeBits.NOT_REMOVABLE | GT_ModHandler.RecipeBits.REVERSIBLE | GT_ModHandler.RecipeBits.BUFFERED;
		
	GT_ModHandler.addCraftingRecipe(ItemList.AdvancedMiner2.get(1L, new Object[0]), bitsd,
				new Object[] { "WWW", "EME", "CCC", 'M', ItemList.Hull_EV, 'W',
						OrePrefixes.frameGt.get(Materials.Titanium), 'E', OrePrefixes.circuit.get(Materials.Data), 'C',
						ItemList.Electric_Motor_EV });
*/
	}
}
