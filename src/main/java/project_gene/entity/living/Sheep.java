package project_gene.entity.living;

import com.google.common.collect.Maps;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import project_gene.ProjectGene;
import project_gene.entity.core.IGenePooled;
import project_gene.entity.core.IShearable;
import project_gene.entity.core.SharedGenes;
import project_gene.gene.GenePool;

import javax.annotation.Nullable;
import java.util.Map;

import static project_gene.entity.core.SharedGenes.Ft;
import static project_gene.entity.core.SharedGenes.Ln;
import static project_gene.entity.living.Sheep.WoolGenes.*;

public class Sheep extends SheepEntity implements IGenePooled<Sheep>, IShearable {
	public static final DataParameter<GenePool> GENE_POOL = EntityDataManager.createKey(Sheep.class, ProjectGene.gene_pool);

	interface WoolGenes {
		String H = "H", L = "L", R = "R", Y = "Y", G = "G", B = "B", Sp = "Sp", Tk = "Tk";
	}
	
	@Override
	public GenePool getDefaultGenePool() {
		return new GenePool().putAll(SharedGenes.class, WoolGenes.class);
	}
	
	public Sheep(EntityType<? extends SheepEntity> p_i50245_1_, World p_i50245_2_) {
		super(p_i50245_1_, p_i50245_2_);
	}

	@Override
	protected void registerData() {
		super.registerData();
		dataManager.register(GENE_POOL, getDefaultGenePool());
	}

	@Override
	public GenePool getGenePool() {
		return dataManager.get(GENE_POOL);
	}

	@Override
	public void setGenePool(GenePool genePool) {
		dataManager.set(GENE_POOL, genePool);
	}

	@Override
	public void onStruckByLightning(LightningBoltEntity p_70077_1_) {
		lightning();
	}

	@Override
	public ResourceLocation getLootTable() {
		return null;
	}

	@Override
	public void notifyDataManagerChange(DataParameter<?> p_184206_1_) {
		if (p_184206_1_.equals(GENE_POOL)) {
			setFleeceColor0(decideColor());
			recalculateSize();
			updateAttributes();
		}
		super.notifyDataManagerChange(p_184206_1_);
	}

	@Override
	@Nullable
	public ILivingEntityData onInitialSpawn(IWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag) {
		spawnDataIn = super.onInitialSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
		setGenePool(getDefaultGenePool()
				.randomize(SharedGenes.class)
				.randomizeAll(H, L, Sp, Tk));
		return spawnDataIn;
	}

	@Override
	public float getRenderScale() {
		GenePool genePool = getGenePool();
		int wool = genePool.get(Sp).dominance() + genePool.get(Tk).dominance() - 4;
		int meat = genePool.get(Ln).dominance() + genePool.get(Ft).dominance() - 4;
		return 1 + wool * 0.035F + meat * 0.17F;
	}

	@Override
	protected void onGrowingAdult() {
		updateAttributes();
	}

	private void updateAttributes() {
		GenePool genePool = getGenePool();
		int wool = genePool.get(Sp).dominance() + genePool.get(Tk).dominance();
		int meat = genePool.get(Ln).dominance() + genePool.get(Ft).dominance();
		getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(4 + 2 * meat);
		getAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(wool - 2);
		double weight = (meat - 2) * 0.08 + (wool - 2) * 0.01;
		getAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(weight * (isChild() ? 0.7 : 1.0));
		getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue((isChild() ? 1.5 : 1) * (0.23 + 0.18 - weight));
	}
	
	@Override
	public Sheep createChild(AgeableEntity p_90011_1_) {
		return child(p_90011_1_);
	}

	@Override
	public void eatGrassBonus() {
		GenePool genePool = getGenePool();
		int wool = genePool.get(Sp).dominance() + genePool.get(Tk).dominance() - 4;
		int chance = 3 - wool;
		int random = rand.nextInt(5);
		if (random < chance)
			setSheared(false);
		if (isChild()) {
			int meat = genePool.get(Ln).dominance() + genePool.get(Ft).dominance() - 4;
			addGrowth((int)(60 * (1 - (wool * 0.15 + meat * 0.25))));
		}
	}

	@Override
	@Deprecated
	public void setFleeceColor(DyeColor p_175512_1_) {
		// Denying vanilla behaviors
	}
	
	private void setFleeceColor0(DyeColor color) {
		super.setFleeceColor(color);
	}

	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		compound.put("GenePool", getGenePool().serializeNBT());
	}
	
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		setFleeceColor0(DyeColor.byId(compound.getByte("Color")));
		GenePool genePool = new GenePool();
		genePool.deserializeNBT(compound.getCompound("GenePool"));
		setGenePool(genePool);
	}

	public DyeColor decideBrightness() {
		GenePool genePool = getGenePool();
		int bright = genePool.get(H).dominance() + genePool.get(L).dominance();
		switch (bright) {
			case 2:
				return DyeColor.BLACK;
			case 3:
				return DyeColor.GRAY;
			case 4:
				return DyeColor.LIGHT_GRAY;
			default:
				return DyeColor.WHITE;
		}
	}

	public boolean isRainbow() {
		DyeColor upstream = decideBrightness();
		GenePool genePool = getGenePool();
		boolean R_ = genePool.get(R).dominant;
		boolean Y_ = genePool.get(Y).dominant;
		boolean G_ = genePool.get(G).dominant;
		boolean B_ = genePool.get(B).dominant;
		boolean bright = upstream == DyeColor.WHITE;
		return bright && R_ && Y_ && G_ && B_;
	}
	
	public DyeColor decideColor() {
		DyeColor upstream = decideBrightness();
		GenePool genePool = getGenePool();
		boolean R_ = genePool.get(R).dominant;
		boolean Y_ = genePool.get(Y).dominant;
		boolean G_ = genePool.get(G).dominant;
		boolean B_ = genePool.get(B).dominant;
		boolean bright = upstream == DyeColor.WHITE;
		int count = 0;
		if (R_) ++count;
		if (Y_) ++count;
		if (G_) ++count;
		if (B_) ++count;
		switch (count) {
			case 4:
				return bright ? DyeColor.WHITE : DyeColor.BLACK;
			case 3:
				return bright ? DyeColor.LIGHT_GRAY : DyeColor.GRAY;
			case 2:
				if (R_ && Y_) return bright ? DyeColor.ORANGE : DyeColor.BROWN;
				if (R_ && G_) return bright ? DyeColor.YELLOW : DyeColor.BROWN;
				if (R_ && B_) return bright ? DyeColor.MAGENTA : DyeColor.PURPLE;
				if (Y_ && G_) return bright ? DyeColor.LIME : DyeColor.GREEN;
				if (Y_ && B_) return bright ? DyeColor.LIME : DyeColor.GREEN;
				if (G_ && B_) return bright ? DyeColor.LIGHT_BLUE : DyeColor.CYAN;
			case 1:
			default:
				if (R_) return bright ? DyeColor.PINK : DyeColor.RED;
				if (Y_) return bright ? DyeColor.YELLOW : DyeColor.BROWN;
				if (G_) return bright ? DyeColor.LIME : DyeColor.GREEN;
				if (B_) return bright ? DyeColor.LIGHT_BLUE : DyeColor.BLUE;
 		}
		return upstream;
	}

	private static final Map<DyeColor, IItemProvider> WOOL_BY_COLOR = Util.make(Maps.newEnumMap(DyeColor.class), map -> {
		map.put(DyeColor.WHITE, Blocks.WHITE_WOOL);
		map.put(DyeColor.ORANGE, Blocks.ORANGE_WOOL);
		map.put(DyeColor.MAGENTA, Blocks.MAGENTA_WOOL);
		map.put(DyeColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_WOOL);
		map.put(DyeColor.YELLOW, Blocks.YELLOW_WOOL);
		map.put(DyeColor.LIME, Blocks.LIME_WOOL);
		map.put(DyeColor.PINK, Blocks.PINK_WOOL);
		map.put(DyeColor.GRAY, Blocks.GRAY_WOOL);
		map.put(DyeColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_WOOL);
		map.put(DyeColor.CYAN, Blocks.CYAN_WOOL);
		map.put(DyeColor.PURPLE, Blocks.PURPLE_WOOL);
		map.put(DyeColor.BLUE, Blocks.BLUE_WOOL);
		map.put(DyeColor.BROWN, Blocks.BROWN_WOOL);
		map.put(DyeColor.GREEN, Blocks.GREEN_WOOL);
		map.put(DyeColor.RED, Blocks.RED_WOOL);
		map.put(DyeColor.BLACK, Blocks.BLACK_WOOL);
	});
	
	private IItemProvider wool() {
		return isRainbow() ? WOOL_BY_COLOR.get(DyeColor.byId(rand.nextInt(16))) : WOOL_BY_COLOR.get(getFleeceColor());
	}

	@Override
	public java.util.List<ItemStack> onSheared(ItemStack item, net.minecraft.world.IWorld world0, BlockPos pos, int fortune) {
		java.util.List<ItemStack> ret = new java.util.ArrayList<>();
		if (!world.isRemote) {
			setSheared(true);
			GenePool genePool = getGenePool();
			int min = genePool.get(Sp).dominance();
			int max = 2 + genePool.get(Tk).dominance();
			int i = min + rand.nextInt(max);
			for(int j = 0; j < i; ++j) ret.add(new ItemStack(wool()));
		}
		return ret;
	}

	@Override
	protected void dropSpecialItems(DamageSource p_213333_1_, int looting, boolean p_213333_3_) {
		if (!world.isRemote) {
			if (!getSheared())
				entityDropItem(wool());
			GenePool genePool = getGenePool();
			int min = genePool.get(Ln).dominance();
			int max = genePool.get(Ft).dominance();
			int i = looting + min + rand.nextInt(max * (looting + 1));
			entityDropItem(new ItemStack(isBurning() ? Items.COOKED_MUTTON : Items.MUTTON, i));
		}
	}

	@Override
	protected int getExperiencePoints(PlayerEntity p_70693_1_) {
		return isRainbow() ? 5 + rand.nextInt(10) : super.getExperiencePoints(p_70693_1_);
	}
}
