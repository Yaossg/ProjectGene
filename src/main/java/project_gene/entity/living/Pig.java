package project_gene.entity.living;

import net.minecraft.entity.*;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import project_gene.ProjectGene;
import project_gene.entity.core.IGenePooled;
import project_gene.entity.core.IShearable;
import project_gene.entity.core.SharedGenes;
import project_gene.gene.GenePool;
import project_gene.gene.Genotype;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static project_gene.entity.core.SharedGenes.Ft;
import static project_gene.entity.core.SharedGenes.Ln;

public class Pig extends PigEntity implements IGenePooled<Pig>, IShearable {
	public static final DataParameter<GenePool> GENE_POOL = EntityDataManager.createKey(Pig.class, ProjectGene.gene_pool);
	
	interface SausageGenes {
		String Ssg = "Ssg";
	}
	
	@Override
	public GenePool getDefaultGenePool() {
		return new GenePool().putAll(SharedGenes.class, SausageGenes.class);
	}
	
	public Pig(EntityType<? extends PigEntity> p_i50250_1_, World p_i50250_2_) {
		super(p_i50250_1_, p_i50250_2_);
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
			recalculateSize();
			updateAttributes();
		}
		super.notifyDataManagerChange(p_184206_1_);
	}
	
	@Override
	@Nullable
	public ILivingEntityData onInitialSpawn(IWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag) {
		spawnDataIn = super.onInitialSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
		setGenePool(getDefaultGenePool().randomizeAll());
		return spawnDataIn;
	}

	@Override
	public float getRenderScale() {
		GenePool genePool = getGenePool();
		int meat = genePool.get(Ln).dominance() + genePool.get(Ft).dominance() - 4;
		return 1 + meat * 0.17F;
	}

	@Override
	protected void onGrowingAdult() {
		updateAttributes();
	}

	private void updateAttributes() {
		GenePool genePool = getGenePool();
		int meat = genePool.get(Ln).dominance() + genePool.get(Ft).dominance();
		getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(4 + 2 * meat);
		getAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(getSaddled() ? 2 : 0);
		double weight = (meat - 2) * 0.08 + (getSaddled() ? 0.01 : 0);
		getAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(weight * (isChild() ? 0.7 : 1.0));
		getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue((isChild() ? 1.5 : 1) * (0.26 + 0.18 - weight));
	}

	@Override
	public boolean processInteract(PlayerEntity player, Hand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (stack.getItem() == Items.SHEARS && !isBeingRidden()) return false;
		return super.processInteract(player, hand);
	}

	@Override
	public Pig createChild(AgeableEntity p_90011_1_) {
		return child(p_90011_1_);
	}

	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		compound.put("GenePool", getGenePool().serializeNBT());
	}

	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		GenePool genePool = new GenePool();
		genePool.deserializeNBT(compound.getCompound("GenePool"));
		setGenePool(genePool);
	}

	@Override
	protected void dropSpecialItems(DamageSource p_213333_1_, int looting, boolean p_213333_3_) {
		if (!world.isRemote) {
			GenePool genePool = getGenePool();
			int min = genePool.get(Ln).dominance();
			int max = genePool.get(Ft).dominance();
			int i = looting + min + rand.nextInt(max * (looting + 1));
			boolean ssg = genePool.get(SausageGenes.Ssg) == Genotype.dominant_homozygote;
			entityDropItem(new ItemStack(ssg ? ProjectGene.sausage : 
					isBurning() ? Items.COOKED_PORKCHOP : Items.PORKCHOP, ssg ? 1 : i));
			if ("Reuben".equals(getName().getUnformattedComponentText())) {
				ItemStack head = new ItemStack(Items.PLAYER_HEAD);
				head.setTagInfo("SkullOwner", new StringNBT("MHF_Pig"));
				head.setDisplayName(new TranslationTextComponent("project_gene.reuben.head"));
				CompoundNBT display = (CompoundNBT) head.getOrCreateTag().get("display");
				ListNBT lore = new ListNBT();
				lore.add(new StringNBT(ITextComponent.Serializer.toJson(new TranslationTextComponent("project_gene.reuben.head.tooltip"))));
				display.put("Lore", lore);
				entityDropItem(head);
			}
		}
	}
	
	@Override
	public void setSaddled(boolean saddled) {
		super.setSaddled(saddled);
		updateAttributes();
	}

	@Override
	public boolean getSheared() {
		return !getSaddled();
	}

	@Override
	public void setSheared(boolean sheared) {
		setSaddled(!sheared);
	}

	@Override
	public boolean isShearable(@Nonnull ItemStack item, IWorldReader world, BlockPos pos) {
		return getSaddled();
	}

	@Nonnull
	@Override
	public List<ItemStack> onSheared(@Nonnull ItemStack item, IWorld world, BlockPos pos, int fortune) {
		setSaddled(false);
		return Collections.singletonList(new ItemStack(Items.SADDLE));
	}

	@Override
	public void onDeath(DamageSource cause) {
		if (!world.isRemote && "Reuben".equals(getName().getUnformattedComponentText()))
			world.getServer().getPlayerList().sendMessage(new TranslationTextComponent("project_gene.reuben.death"));
		super.onDeath(cause);
	}
}
