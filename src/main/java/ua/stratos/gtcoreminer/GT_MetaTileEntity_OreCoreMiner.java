package ua.stratos.gtcoreminer;

import java.util.ArrayList;

import gregtech.api.enums.OrePrefixes;
import gregtech.api.enums.Textures;
import gregtech.api.gui.GT_Container_2by2;
import gregtech.api.gui.GT_Container_4by4;
import gregtech.api.gui.GT_GUIContainer_2by2;
import gregtech.api.gui.GT_GUIContainer_4by4;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.objects.GT_RenderedTexture;
import gregtech.api.objects.ItemData;
import gregtech.api.util.GT_ModHandler;
import gregtech.api.util.GT_OreDictUnificator;
import gregtech.api.util.GT_Utility;
import gregtech.common.blocks.GT_Block_Ores_Abstract;
import gregtech.common.blocks.GT_TileEntity_Ores;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;
import net.minecraftforge.oredict.OreDictionary;

public class GT_MetaTileEntity_OreCoreMiner extends MetaTileEntity {

	private int mProgressTime = 0;
	private int areaSize = 0;
	private int currentAltitude = 0;
	private int amoutCycles = 0;
	//private boolean isEndOreCore = false;

	private final ArrayList<ChunkPosition> mMineList = new ArrayList();

	public GT_MetaTileEntity_OreCoreMiner(int aID, String aBasicName, String aRegionalName) {
		super(aID, aBasicName, aRegionalName, 4 + 4);
	}

	public GT_MetaTileEntity_OreCoreMiner(String aBasicName) {
		super(aBasicName, 4 + 4);
	}

	@Override
	public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
		if (aBaseMetaTileEntity.isServerSide()) {

			if (!aBaseMetaTileEntity.isAllowedToWork())
				return;
			// Usage EU
			if(GTMinerMod.isUseEnergy){
				if (!aBaseMetaTileEntity.decreaseStoredEnergyUnits(64, false)) {
					if (!hasCanContinueWork())
						StopMachine(aBaseMetaTileEntity);
					return;
				}
			}
			if (++mProgressTime > 20) {
				mProgressTime = 0;
				aBaseMetaTileEntity.setActive(true);

				TileEntity tTileEntity2 = aBaseMetaTileEntity.getTileEntityAtSide(aBaseMetaTileEntity.getFrontFacing());
				if (tTileEntity2 == null) {
					StopMachine(aBaseMetaTileEntity);
					return;
				}

				if (mMineList.isEmpty()) {

					if (areaSize == 0) {
						for (; currentAltitude < 7; currentAltitude++) {
							AddBlocToMineList(new ChunkPosition(0, currentAltitude, 0));
						}

					} else {
						for (int iX = -areaSize; iX < areaSize; iX++) {

							AddBlocToMineList(new ChunkPosition(iX, currentAltitude, areaSize));
							AddBlocToMineList(new ChunkPosition(iX + 1, currentAltitude, -areaSize));

						}
						for (int iZ = -areaSize; iZ < areaSize; iZ++) {
							AddBlocToMineList(new ChunkPosition(areaSize, currentAltitude, iZ + 1));
							AddBlocToMineList(new ChunkPosition(-areaSize, currentAltitude, iZ));

						}
					}

				}
				if (mMineList.isEmpty()) {
					if (/* isEndOreCore && areaSize > 5 || */ areaSize > maxProgresstime()) {
						areaSize = 0;
						currentAltitude = 0;
						amoutCycles++;
						if (amoutCycles > 3)
							StopMachine(aBaseMetaTileEntity);
						return;
					}
					if (currentAltitude < 6) {
						currentAltitude++;
						return;
					}
					// isEndOreCore = true;
					areaSize++;
					currentAltitude = 0;
					return;
				}

				ArrayList<ItemStack> tDrops = new ArrayList();
				Block tMineBlock = null;
				ChunkPosition mle = null;
				while ((tMineBlock == null || tMineBlock == Blocks.air) && !mMineList.isEmpty()) {
					mle = mMineList.get(0);
					mMineList.remove(0);
					tMineBlock = getBaseMetaTileEntity().getBlockOffset(mle.chunkPosX, mle.chunkPosY, mle.chunkPosZ);
				}

				if (tMineBlock != null && tMineBlock != Blocks.air) {
					int posX = mle.chunkPosX + getBaseMetaTileEntity().getXCoord();
					int posY = mle.chunkPosY + getBaseMetaTileEntity().getYCoord();
					int posZ = mle.chunkPosZ + getBaseMetaTileEntity().getZCoord();

					int metadata = getBaseMetaTileEntity().getMetaIDOffset(mle.chunkPosX, mle.chunkPosY, mle.chunkPosZ);
					boolean silkTouch = tMineBlock.canSilkHarvest(getBaseMetaTileEntity().getWorld(), null, posX, posY,
							posZ, metadata);
					if (silkTouch) {
						ItemStack IS = new ItemStack(tMineBlock);
						IS.setItemDamage(metadata);
						IS.stackSize = 1;
						tDrops.add(IS);
					} else {
						tDrops = tMineBlock.getDrops(getBaseMetaTileEntity().getWorld(), posX, posY, posZ, metadata, 1);
					}

					getBaseMetaTileEntity().getWorld().setBlockToAir(posX, posY, posZ);
					if (!tDrops.isEmpty()) {
						for (int inventoryI = 4, dropI = 0; dropI < tDrops.size()
								&& inventoryI < mInventory.length; inventoryI++, dropI++) {
							mInventory[inventoryI] = tDrops.get(dropI).copy();
							GT_Utility.moveOneItemStack(aBaseMetaTileEntity, tTileEntity2,
									aBaseMetaTileEntity.getFrontFacing(), aBaseMetaTileEntity.getBackFacing(), null,
									false, (byte) 64, (byte) 1, (byte) 64, (byte) 1);
						}
					}

				}

			}

		}

	}

	public void StopMachine(IGregTechTileEntity aBaseMetaTileEntity) {
		//isEndOreCore = false;
		currentAltitude = 0;
		mProgressTime = 0;
		mMineList.clear();
		aBaseMetaTileEntity.disableWorking();
		aBaseMetaTileEntity.setActive(false);
	}

	public void AddBlocToMineList(ChunkPosition posOffset) {
		Block newBlock = getBaseMetaTileEntity().getBlockOffset(posOffset.chunkPosX, posOffset.chunkPosY,
				posOffset.chunkPosZ);
		if (newBlock == Blocks.air || mMineList.contains(posOffset))
			return;
		if (newBlock instanceof GT_Block_Ores_Abstract) {
			TileEntity tTileEntity = getBaseMetaTileEntity().getTileEntityOffset(posOffset.chunkPosX,
					posOffset.chunkPosY, posOffset.chunkPosZ);
			if ((tTileEntity != null) && (tTileEntity instanceof GT_TileEntity_Ores)
					&& ((GT_TileEntity_Ores) tTileEntity).mNatural == true) {
				mMineList.add(posOffset);
				// isEndOreCore = false;
				return;
			}
		}

		int tMetaID = getBaseMetaTileEntity().getMetaIDOffset(posOffset.chunkPosX, posOffset.chunkPosY,
				posOffset.chunkPosZ);
		ItemData tAssotiation = GT_OreDictUnificator.getAssociation(new ItemStack(newBlock, 1, tMetaID));
		if (tAssotiation != null && tAssotiation.mPrefix.toString().startsWith("ore")) {
			mMineList.add(posOffset);
			// isEndOreCore = false;
			return;
		}

		if (posOffset.chunkPosX == 0 || posOffset.chunkPosZ == 0 || posOffset.chunkPosX == 1
				|| posOffset.chunkPosZ == 1) {
			if (itemIsOredict(new ItemStack(newBlock))) {
				mMineList.add(posOffset);
			}
		}

	}

	public boolean hasCanContinueWork() {
		if(!GTMinerMod.isUseEnergy)
			return true;
		if (mInventory[0] != null) {
			ItemStack battarySlot = mInventory[0];
			if (GT_ModHandler.isElectricItem(battarySlot)
					&& battarySlot.getUnlocalizedName().startsWith("gt.metaitem.01.")) {
				String name = battarySlot.getUnlocalizedName();
				if (name.equals("gt.metaitem.01.32510") || name.equals("gt.metaitem.01.32511")
						|| name.equals("gt.metaitem.01.32520") || name.equals("gt.metaitem.01.32521")
						|| name.equals("gt.metaitem.01.32530") || name.equals("gt.metaitem.01.32531")) {
					if (ic2.api.item.ElectricItem.manager.getCharge(battarySlot) != 0) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean itemIsOredict(ItemStack itemstack) {
		int[] oreidArray = OreDictionary.getOreIDs(itemstack);
		boolean result = false;
		for (int oreid : oreidArray) {
			String oreName = OreDictionary.getOreName(oreid);
			if (oreName.startsWith("stone") || oreName.startsWith("dirt"))
				return true;
		}
		return result;
	}

	@Override
	public String[] getDescription() {
		return new String[] { "Does not have a buffer of items", "All items are automatically put in chest",
				"Has a place for batteries: 4", "Mine area 48x48x7",
				"Controller position Center(24)-Center(24)-Bottom(0)",
				"Beginning of mining from the center and to the full height." };
	}

	@Override
	public boolean isSimpleMachine() {
		return false;
	}

	@Override
	public boolean isElectric() {
		return true;
	}

	@Override
	public boolean isValidSlot(int aIndex) {
		return true;
	}

	@Override
	public boolean isFacingValid(byte aFacing) {
		return true;
	}

	@Override
	public boolean isEnetInput() {
		return false;
	}

	@Override
	public boolean isTeleporterCompatible() {
		return false;
	}

	@Override
	public long getMinimumStoredEU() {
		return 128;
	}

	@Override
	public long maxEUStore() {
		return 8192;
	}

	@Override
	public long maxEUInput() {
		return 128;
	}

	@Override
	public int rechargerSlotStartIndex() {
		return 0;
	}

	@Override
	public int dechargerSlotStartIndex() {
		return 0;
	}

	@Override
	public int dechargerSlotCount() {
		return 4;
	}

	@Override
	public int getProgresstime() {
		return mProgressTime;
	}

	@Override
	public int maxProgresstime() {
		return 26;
	}

	@Override
	public boolean isAccessAllowed(EntityPlayer aPlayer) {
		return true;
	}

	@Override
	public void saveNBTData(NBTTagCompound aNBT) {
		aNBT.setInteger("areaSize", areaSize);
		aNBT.setInteger("currentAltitude", currentAltitude);
		aNBT.setInteger("amoutCycles", amoutCycles);
		//aNBT.setBoolean("isEndOreCore", isEndOreCore);
	}

	@Override
	public void loadNBTData(NBTTagCompound aNBT) {
		areaSize = aNBT.getInteger("areaSize");
		currentAltitude = aNBT.getInteger("currentAltitude");
		amoutCycles = aNBT.getInteger("amoutCycles");
		//isEndOreCore = aNBT.getBoolean("isEndOreCore");
	}

	@Override
	public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
		if (aBaseMetaTileEntity.isClientSide())
			return true;
		aBaseMetaTileEntity.openGUI(aPlayer);
		return true;
	}

	@Override
	public Object getServerGUI(int aID, InventoryPlayer aPlayerInventory, IGregTechTileEntity aBaseMetaTileEntity) {
		return new GT_Container_2by2(aPlayerInventory, aBaseMetaTileEntity);
	}

	@Override
	public Object getClientGUI(int aID, InventoryPlayer aPlayerInventory, IGregTechTileEntity aBaseMetaTileEntity) {
		return new GT_GUIContainer_2by2(aPlayerInventory, aBaseMetaTileEntity, getLocalName());
	}

	@Override
	public boolean allowPullStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, byte aSide, ItemStack aStack) {
		if (3 < aIndex && aIndex < mInventory.length)
			return true;
		String name = aStack.getUnlocalizedName();
		if (GT_ModHandler.isElectricItem(aStack) && aStack.getUnlocalizedName().startsWith("gt.metaitem.01.")) {
			/*
			 * if (name.equals("gt.metaitem.01.32510") ||
			 * name.equals("gt.metaitem.01.32511") ||
			 * name.equals("gt.metaitem.01.32520") ||
			 * name.equals("gt.metaitem.01.32521") ||
			 * name.equals("gt.metaitem.01.32530") ||
			 * name.equals("gt.metaitem.01.32531")) {
			 */
			if (ic2.api.item.ElectricItem.manager.getCharge(aStack) == 0) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean allowPutStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, byte aSide, ItemStack aStack) {
		if (aIndex < 0 || aIndex > 3)
			return false;
		if (!GT_Utility.isStackValid(aStack)) {
			return false;
		}
		if (mInventory[aIndex] == null && GT_ModHandler.isElectricItem(aStack)) {
			return true;
		}
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public byte getTileEntityBaseType() {
		return 2;
	}

	@Override
	public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
		return new GT_MetaTileEntity_OreCoreMiner(mName);
	}

	@Override
	public String[] getInfoData() {
		return new String[] { "Current altitude: " + currentAltitude,
				"Progress: " + areaSize + " / " + maxProgresstime() };
	}

	@Override
	public boolean isGivingInformation() {
		return true;
	}

	@Override
	public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, byte aSide, byte aFacing, byte aColorIndex,
			boolean aActive, boolean aRedstone) {
		if (aSide == aFacing) {
			return new ITexture[] { Textures.BlockIcons.CASING_BLOCKS[16],
					new GT_RenderedTexture(Textures.BlockIcons.OVERLAY_PIPE_OUT) };
		}
		byte activeSide = (byte) (aFacing == 5 ? 2 : aFacing + 1);
		if (aSide == activeSide || aSide == 1) {
			return new ITexture[] { Textures.BlockIcons.CASING_BLOCKS[16],
					new GT_RenderedTexture(aActive ? Textures.BlockIcons.OVERLAY_FRONT_ROCK_BREAKER_ACTIVE
							: Textures.BlockIcons.OVERLAY_FRONT_ROCK_BREAKER) };
		} else {
			return new ITexture[] { Textures.BlockIcons.CASING_BLOCKS[16] };
		}
	}

}
