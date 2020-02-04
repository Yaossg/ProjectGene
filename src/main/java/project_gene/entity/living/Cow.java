package project_gene.entity.living;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerBlock;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SuspiciousStewItem;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import project_gene.ProjectGene;
import project_gene.entity.core.IGenePooled;
import project_gene.entity.core.IShearable;
import project_gene.entity.core.SharedGenes;
import project_gene.gene.GenePool;
import project_gene.gene.Genotype;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import static project_gene.entity.core.SharedGenes.Ft;
import static project_gene.entity.core.SharedGenes.Ln;
import static project_gene.entity.living.Cow.LeatherGenes.Rg;
import static project_gene.entity.living.Cow.LeatherGenes.Sm;
import static project_gene.entity.living.Cow.MushroomGenes.Br;
import static project_gene.entity.living.Cow.MushroomGenes.Rd;

public class Cow extends CowEntity implements IGenePooled<Cow>, IShearable {
	public static final DataParameter<GenePool> GENE_POOL = EntityDataManager.createKey(Cow.class, ProjectGene.gene_pool);
	public static final DataParameter<Boolean> SHEARED = EntityDataManager.createKey(Cow.class, DataSerializers.BOOLEAN);

	interface LeatherGenes {
		String Sm = "Sm", Rg = "Rg";
	}
	
	interface MushroomGenes {
		String Rd = "Rd", Br = "Br";
	}
	
	@Override
	public GenePool getDefaultGenePool() {
		return new GenePool()
				.putAll(SharedGenes.class, LeatherGenes.class, MushroomGenes.class)
				.put(Rd, Genotype.dominant_homozygote)
				.put(Br, Genotype.dominant_homozygote);
	}

	public Cow(EntityType<? extends CowEntity> p_i48567_1_, World p_i48567_2_) {
		super(p_i48567_1_, p_i48567_2_);
	}

	@Override
	protected void registerData() {
		super.registerData();
		dataManager.register(GENE_POOL, getDefaultGenePool());
		dataManager.register(SHEARED, false);
	}

	public enum MushroomType {
		NONE, RED, BROWN, MIXED
	}
	
	@Override
	public void setSheared(boolean sheared) {
		dataManager.set(SHEARED, sheared);
	}
	
	@Override
	public boolean getSheared() {
		return dataManager.get(SHEARED);
	}

	public Block mushroom(int flag1, boolean flag2) {
		switch (getMushroomType()) {
			case RED:
				return Blocks.RED_MUSHROOM;
			case BROWN:
				return Blocks.BROWN_MUSHROOM;
			default:
				return flag2 ? Blocks.ALLIUM : (flag1 & 1) == 0 ? Blocks.RED_MUSHROOM : Blocks.BROWN_MUSHROOM;
		}
	}
	
	private int grassTimer;
	private EatGrassGoal eatGrassGoal;
	
	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new SwimGoal(this));
		this.goalSelector.addGoal(1, new PanicGoal(this, 2.0D) {
			@Override
			public boolean shouldExecute() {
				if (creature.getRevengeTarget() != null)
					return findRandomPosition();
				if (creature.isBurning() && Cow.this.getMushroomType() != MushroomType.MIXED) {
					BlockPos pos = getRandPos(creature.world, creature, 5, 4);
					if (pos != null) {
						randPosX = pos.getX();
						randPosY = pos.getY();
						randPosZ = pos.getZ();
						return true;
					}
					return findRandomPosition();
				}
				return false;
			}
		});
		this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
		this.goalSelector.addGoal(3, new TemptGoal(this, 1.25D, Ingredient.fromItems(Items.WHEAT), false));
		this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.25D));
		this.goalSelector.addGoal(5, eatGrassGoal = new EatGrassGoal(this));
		this.goalSelector.addGoal(6, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
		this.goalSelector.addGoal(7, new LookAtGoal(this, PlayerEntity.class, 6.0F));
		this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
	}
	
	@Override
	protected void updateAITasks() {
		grassTimer = eatGrassGoal.getEatingGrassTimer();
		super.updateAITasks();
	}

	private static final EffectInstance effect = new EffectInstance(Effects.FIRE_RESISTANCE, 5);
	public void livingTick() {
		if (world.isRemote) grassTimer = Math.max(0, grassTimer - 1);
		else if (getMushroomType() == MushroomType.MIXED) addPotionEffect(effect);
		super.livingTick();
	}

	@OnlyIn(Dist.CLIENT)
	public void handleStatusUpdate(byte id) {
		if (id == 10) {
			grassTimer = 40;
		} else {
			super.handleStatusUpdate(id);
		}

	}

	@OnlyIn(Dist.CLIENT)
	public float getHeadRotationPointY(float p_70894_1_) {
		if (grassTimer <= 0) {
			return 0.0F;
		} else if (grassTimer >= 4 && grassTimer <= 36) {
			return 1.0F;
		} else {
			return grassTimer < 4 ? ((float)grassTimer - p_70894_1_) / 4.0F : -((float)(grassTimer - 40) - p_70894_1_) / 4.0F;
		}
	}

	@OnlyIn(Dist.CLIENT)
	public float getHeadRotationAngleX(float p_70890_1_) {
		if (grassTimer > 4 && grassTimer <= 36) {
			float f = ((float)(grassTimer - 4) - p_70890_1_) / 32.0F;
			return (float)Math.PI / 5F + 0.21991149F * MathHelper.sin(f * 28.7F);
		} else {
			return grassTimer > 0 ? (float)Math.PI / 5F : rotationPitch * ((float)Math.PI / 180F);
		}
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

	public static boolean canSpawn(EntityType<Cow> p_223318_0_, IWorld p_223318_1_, SpawnReason p_223318_2_, BlockPos p_223318_3_, Random p_223318_4_) {
		Block block = p_223318_1_.getBlockState(p_223318_3_.down()).getBlock();
		return (block == Blocks.GRASS_BLOCK || block == Blocks.MYCELIUM) && p_223318_1_.getLightSubtracted(p_223318_3_, 0) > 8;
	}
	
	@Override
	@Nullable
	public ILivingEntityData onInitialSpawn(IWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag) {
		spawnDataIn = super.onInitialSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
		GenePool genePool = getDefaultGenePool().randomizeAll(SharedGenes.class, LeatherGenes.class);
		if (worldIn.getBiome(new BlockPos(this)).getCategory() == Biome.Category.MUSHROOM) {
//			int i = rand.nextInt();
//			if ((i & 1) != 0) genePool.put(Rd, Genotype.recessive);
//			if ((i & 2) != 0) genePool.put(Br, Genotype.recessive);
//			if ((i & 3) == 0) genePool.put(((i & 4) == 0) ? Rd : Br, Genotype.recessive);
//			if ((i & 8) != 0 && genePool.get(Br) == Genotype.recessive) {
//				genePool.put(Br, ((i & 16) == 0) ? Genotype.dominant_homozygote : Genotype.dominant_heterozygote);
//				genePool.put(Rd, Genotype.recessive);
//			}
//			if ((i & 32) != 0 && genePool.get(Br) == Genotype.recessive) {
//				genePool.put(Br, Genotype.dominant_heterozygote);
//				genePool.put(Rd, Genotype.recessive);
//			}
//			if ((i & 64) != 0 && (i & 3) == 3) {
//				genePool.put(Rd, ((i & 128) == 0) ? Genotype.dominant_homozygote : Genotype.dominant_heterozygote);
//			}
			int i = rand.nextInt(32);
			if (i < 15 + 6) {
				genePool.put(Rd, Genotype.recessive);
				if (i >= 15) {
					genePool.put(Br, Genotype.dominant_heterozygote);
				}
			} else if (i < 15 + 6 + 5 + 2) {
				genePool.put(Br, Genotype.recessive);
				if (i >= 15 + 6 + 5) {
					genePool.put(Rd, Genotype.dominant_heterozygote);
				}
			} else {
				genePool.putAll(MushroomGenes.class);
			}
		}
		setGenePool(genePool);
		return spawnDataIn;
	}

	@Override
	public float getRenderScale() {
		GenePool genePool = getGenePool();
		int leather = genePool.get(Sm).dominance() + genePool.get(Rg).dominance() - 4;
		int meat = genePool.get(Ln).dominance() + genePool.get(Ft).dominance() - 4;
		return 1 + leather * 0.035F + meat * 0.17F;
	}

	@Override
	protected void onGrowingAdult() {
		updateAttributes();
	}

	private void updateAttributes() {
		GenePool genePool = getGenePool();
		int leather = genePool.get(Sm).dominance() + genePool.get(Rg).dominance();
		int meat = genePool.get(Ln).dominance() + genePool.get(Ft).dominance();
		getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(6 + 2 * meat);
		getAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(leather - 2);
		double weight = (meat - 2) * 0.1 + (leather - 2) * 0.02;
		getAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(weight * (isChild() ? 0.7 : 1.0));
		getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue((isChild() ? 1.5 : 1) * (0.2 + 0.24 - weight));
	}
	
	private Effect stewEffect;
	private int effectDuration;
	
	private void eatFlower(FlowerBlock flower) {
		for (int i = 0; i < 4; ++i)
			world.addParticle(ParticleTypes.EFFECT, posX + rand.nextFloat() / 2.0F, posY + getHeight() / 2.0F, posZ + rand.nextFloat() / 2.0F, 0.0D, rand.nextFloat() / 5.0F, 0.0D);
		stewEffect = flower.getStewEffect();
		effectDuration = flower.getStewEffectDuration();
	}
	
	@Override
	public boolean processInteract(PlayerEntity player, Hand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (getMushroomType() != MushroomType.NONE) {
			if (stack.getItem().isIn(ItemTags.SMALL_FLOWERS)) {
				if (stewEffect != null) {
					for (int i = 0; i < 2; ++i)
						world.addParticle(ParticleTypes.SMOKE, posX + rand.nextFloat() / 2.0F, posY + getHeight() / 2.0F, posZ + rand.nextFloat() / 2.0F, 0.0D, rand.nextFloat() / 5.0F, 0.0D);
				} else {
					if (!player.isCreative()) stack.shrink(1);
					eatFlower((FlowerBlock) ((BlockItem) stack.getItem()).getBlock());
					playSound(SoundEvents.ENTITY_MOOSHROOM_EAT, 2.0F, 1.0F);
				}
			} else if (stack.getItem() == Items.BOWL && !isChild() && !player.isCreative()) {
				stack.shrink(1);
				ItemStack ret;
				SoundEvent soundEvent;
				if (stewEffect == null && getMushroomType() == MushroomType.MIXED)
					eatFlower((FlowerBlock) Blocks.ALLIUM);
				if (stewEffect != null) {
					soundEvent = SoundEvents.ENTITY_MOOSHROOM_SUSPICIOUS_MILK;
					ret = new ItemStack(Items.SUSPICIOUS_STEW);
					SuspiciousStewItem.addEffect(ret, stewEffect, effectDuration);
					stewEffect = null;
					effectDuration = 0;
				} else {
					soundEvent = SoundEvents.ENTITY_MOOSHROOM_MILK;
					ret = new ItemStack(Items.MUSHROOM_STEW);
				}
				if (stack.isEmpty()) player.setHeldItem(hand, ret);
				else if (!player.inventory.addItemStackToInventory(ret)) player.dropItem(ret, false);
				playSound(soundEvent, 1.0F, 1.0F);
				return true;
			}
		}
		return super.processInteract(player, hand);
	}

	@Override
	public Cow createChild(AgeableEntity p_90011_1_) {
		return child(p_90011_1_);
	}

	@Override
	public void eatGrassBonus() {
		GenePool genePool = getGenePool();
		setSheared(false);
		if (isChild()) {
			int leather = genePool.get(Sm).dominance() + genePool.get(Rg).dominance() - 4;
			int meat = genePool.get(Ln).dominance() + genePool.get(Ft).dominance() - 4;
			addGrowth((int)(60 * (1 - (leather * 0.15 + meat * 0.25))));
		}
	}

	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		compound.put("GenePool", getGenePool().serializeNBT());
		compound.putBoolean("Sheared", getSheared());
		if (stewEffect != null) {
			compound.putByte("EffectId", (byte)Effect.getId(stewEffect));
			compound.putInt("EffectDuration", effectDuration);
		}
	}

	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		GenePool genePool = new GenePool();
		genePool.deserializeNBT(compound.getCompound("GenePool"));
		setGenePool(genePool);
		setSheared(compound.getBoolean("Sheared"));
		if (compound.contains("EffectId", 1)) stewEffect = Effect.get(compound.getByte("EffectId"));
		if (compound.contains("EffectDuration", 3)) effectDuration = compound.getInt("EffectDuration");
	}
	
	public MushroomType getMushroomType() {
		GenePool genePool = getGenePool();
		boolean Rd_ = genePool.get(Rd) == Genotype.recessive;
		boolean Br_ = genePool.get(Br) == Genotype.recessive;
		if (Rd_ && Br_) return MushroomType.MIXED;
		if (Rd_) return MushroomType.RED;
		if (Br_) return MushroomType.BROWN;
		return MushroomType.NONE;
	}
	
	@Override
	protected void dropSpecialItems(DamageSource p_213333_1_, int looting, boolean p_213333_3_) {
		if (!world.isRemote) {
			if (!getSheared() && getMushroomType() == MushroomType.MIXED)
				entityDropItem(Blocks.ALLIUM);
			GenePool genePool = getGenePool();
			int min = genePool.get(Ln).dominance();
			int max = genePool.get(Ft).dominance();
			int i = looting + min + rand.nextInt(max * (looting + 1));
			entityDropItem(new ItemStack(isBurning() ? Items.COOKED_BEEF : Items.BEEF, i));
		}
	}

	@Override
	public boolean isShearable(@Nonnull ItemStack item, IWorldReader world, BlockPos pos) {
		return getMushroomType() != MushroomType.NONE && !getSheared() && !isChild();
	}

	@Nonnull
	@Override
	public List<ItemStack> onSheared(@Nonnull ItemStack item, IWorld world0, BlockPos pos, int fortune) {
		java.util.List<ItemStack> ret = new java.util.ArrayList<>();
		if (!world.isRemote) {
			setSheared(true);
			for (int i = 0; i < 5; ++i)
				ret.add(new ItemStack(mushroom(i, i == 0)));
		}
		return ret;
	}

	@Override
	protected int getExperiencePoints(PlayerEntity player) {
		return super.getExperiencePoints(player) + (getMushroomType() == MushroomType.MIXED ? 5 : 0);
	}
}
