package project_gene.entity.living;

import cpw.mods.modlauncher.api.INameMappingService;
import net.minecraft.entity.*;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import project_gene.ProjectGene;
import project_gene.entity.core.IGenePooled;
import project_gene.entity.core.SharedGenes;
import project_gene.gene.GenePool;
import project_gene.reflect.TrickyReflect;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import static net.minecraftforge.fml.common.ObfuscationReflectionHelper.remapName;
import static project_gene.entity.core.SharedGenes.Ft;
import static project_gene.entity.core.SharedGenes.Ln;
import static project_gene.entity.living.Chicken.EggGenes.M;
import static project_gene.entity.living.Chicken.EggGenes.P;
import static project_gene.entity.living.Chicken.FeatherGenes.Sp;
import static project_gene.entity.living.Chicken.FeatherGenes.Tk;

public class Chicken extends ChickenEntity implements IGenePooled<Chicken> {
	public static final DataParameter<GenePool> GENE_POOL = EntityDataManager.createKey(Chicken.class, ProjectGene.gene_pool);
	
	interface EggGenes {
		String P = "P", M = "M";
	}
	
	interface FeatherGenes {
		String Sp = "Sp", Tk = "Tk";
	}
	
	@Override
	public GenePool getDefaultGenePool() {
		return new GenePool().putAll(SharedGenes.class, EggGenes.class, FeatherGenes.class);
	}
	
	public Chicken(EntityType<? extends ChickenEntity> type, World worldIn) {
		super(type, worldIn);
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
		setGenePool(getDefaultGenePool()
				.randomizeAll(SharedGenes.class, EggGenes.class, FeatherGenes.class));
		return spawnDataIn;
	}


	@Override
	public float getRenderScale() {
		GenePool genePool = getGenePool();
		int feather = genePool.get(Sp).dominance() + genePool.get(Tk).dominance() - 4;
		int meat = genePool.get(Ln).dominance() + genePool.get(Ft).dominance() - 4;
		return 1 + feather * 0.035F + meat * 0.17F;
	}

	@Override
	protected void onGrowingAdult() {
		updateAttributes();
	}

	private void updateAttributes() {
		GenePool genePool = getGenePool();
		int feather = genePool.get(Sp).dominance() + genePool.get(Tk).dominance();
		int meat = genePool.get(Ln).dominance() + genePool.get(Ft).dominance();
		getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(4 + 2 * meat);
		getAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(feather - 2);
		double weight = (meat - 2) * 0.05 + (feather - 2) * 0.005;
		getAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(weight * (isChild() ? 0.7 : 1.0));
		getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue((isChild() ? 1.5 : 1) * (0.25 + 0.11 - weight));
	}

	@Override
	public boolean processInteract(PlayerEntity player, Hand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (stack.getItem() == Items.SHEARS && !world.isRemote) {
			stack.damageItem(1, player, (p_213613_1_) -> p_213613_1_.sendBreakAnimation(hand));
			attackEntityFrom(DamageSource.causePlayerDamage(player), 8);
		}
		return super.processInteract(player, hand);
	}

	@Override
	public Chicken createChild(AgeableEntity p_90011_1_) {
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
	
	private static final MethodHandle ancestor_livingTick;

	static {
		ancestor_livingTick = TrickyReflect.ancestorEnsured(AnimalEntity.class,
				remapName(INameMappingService.Domain.METHOD, "func_70636_d"), 
				MethodType.methodType(void.class));
	}
	
	@Override
	public void livingTick() {
		try {
			ancestor_livingTick.invoke(this);
		} catch (Throwable throwable) {
			ProjectGene.logger.fatal("Failed to tick chicken(UUID={})'s ancestor", getUniqueID());
			ProjectGene.logger.fatal("Removed the chicken(@{}) to ensure security", getPositionVec());
			remove();
			return;
		}
		oFlap = wingRotation;
		oFlapSpeed = destPos;
		destPos = (float)((double)destPos + (double)(onGround ? -1 : 4) * 0.3D);
		destPos = MathHelper.clamp(destPos, 0.0F, 1.0F);
		if (!onGround && wingRotDelta < 1.0F) {
			wingRotDelta = 1.0F;
		}

		wingRotDelta = (float)((double)wingRotDelta * 0.9D);
		Vec3d vec3d = getMotion();
		if (!onGround && vec3d.y < 0.0D) {
			setMotion(vec3d.mul(1.0D, 0.6D, 1.0D));
		}

		wingRotation += wingRotDelta * 2.0F;
		if (!world.isRemote && isAlive() && !isChild() && --timeUntilNextEgg <= 0) {
			playSound(SoundEvents.ENTITY_CHICKEN_EGG, 1.0F, (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
			entityDropItem(Items.EGG);
			GenePool genePool = getGenePool();
			int P_ = genePool.get(P).dominance() - 2;
			int M_ = genePool.get(M).dominance() - 2;
			timeUntilNextEgg = rand.nextInt(6000 + P_ * 3000 + M_ * 600) + 6000 + M_ * 1500  + P_ * 300;
		}
	}

	@Override
	protected void dropSpecialItems(DamageSource source, int looting, boolean recentlyHitIn) {
		if (!world.isRemote) {
			GenePool genePool = getGenePool();
			{
				int min = genePool.get(Ln).dominance();
				int max = genePool.get(Ft).dominance();
				int i = looting + min + rand.nextInt(max * (looting + 1));
				entityDropItem(new ItemStack(isBurning() ? Items.COOKED_CHICKEN : Items.CHICKEN, i));
			}
			{
				int min = genePool.get(Sp).dominance();
				int max = genePool.get(Tk).dominance();
				int i = looting + min + rand.nextInt(max * (looting + 1));
				entityDropItem(new ItemStack(Items.FEATHER, i));
			}
		}
	}
}
