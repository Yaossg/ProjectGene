package project_gene.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.SheepWoolLayer;
import net.minecraft.client.renderer.entity.model.SheepModel;
import net.minecraft.client.renderer.entity.model.SheepWoolModel;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import project_gene.entity.living.Sheep;

@OnlyIn(Dist.CLIENT)
public class SheepRenderer extends MobRenderer<SheepEntity, SheepModel<SheepEntity>> {
	private static final ResourceLocation SHEARED_SHEEP_TEXTURES = new ResourceLocation("textures/entity/sheep/sheep.png");
	private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/sheep/sheep_fur.png");
	
	public SheepRenderer(EntityRendererManager p_i47195_1_) {
		super(p_i47195_1_, new SheepModel<>(), 0.7F);
		addLayer(new SheepWoolLayer(this) {
			private final SheepWoolModel<SheepEntity> sheepModel = new SheepWoolModel<>();
			@Override
			public void render(SheepEntity p_212842_1_, float p_212842_2_, float p_212842_3_, float p_212842_4_, float p_212842_5_, float p_212842_6_, float p_212842_7_, float p_212842_8_) {
				if (!p_212842_1_.getSheared() && !p_212842_1_.isInvisible()) {
					this.bindTexture(TEXTURE);
					if (((Sheep) p_212842_1_).isRainbow()) {
						int lvt_10_1_ = p_212842_1_.ticksExisted / 25 + p_212842_1_.getEntityId();
						int lvt_11_1_ = DyeColor.values().length;
						int lvt_12_1_ = lvt_10_1_ % lvt_11_1_;
						int lvt_13_1_ = (lvt_10_1_ + 1) % lvt_11_1_;
						float lvt_14_1_ = ((float)(p_212842_1_.ticksExisted % 25) + p_212842_4_) / 25.0F;
						float[] lvt_15_1_ = SheepEntity.getDyeRgb(DyeColor.byId(lvt_12_1_));
						float[] lvt_16_1_ = SheepEntity.getDyeRgb(DyeColor.byId(lvt_13_1_));
						GlStateManager.color3f(lvt_15_1_[0] * (1.0F - lvt_14_1_) + lvt_16_1_[0] * lvt_14_1_, lvt_15_1_[1] * (1.0F - lvt_14_1_) + lvt_16_1_[1] * lvt_14_1_, lvt_15_1_[2] * (1.0F - lvt_14_1_) + lvt_16_1_[2] * lvt_14_1_);
					} else {
						float[] lvt_9_2_ = SheepEntity.getDyeRgb(p_212842_1_.getFleeceColor());
						GlStateManager.color3f(lvt_9_2_[0], lvt_9_2_[1], lvt_9_2_[2]);
					}
					getEntityModel().setModelAttributes(this.sheepModel);
					this.sheepModel.setLivingAnimations(p_212842_1_, p_212842_2_, p_212842_3_, p_212842_4_);
					this.sheepModel.render(p_212842_1_, p_212842_2_, p_212842_3_, p_212842_5_, p_212842_6_, p_212842_7_, p_212842_8_);
				}
			}
		});
		
	}

	@Override
	protected void preRenderCallback(SheepEntity entitylivingbaseIn, float p_77041_2_) {
		float scale = entitylivingbaseIn.getRenderScale();
		GlStateManager.scalef(scale, scale, scale);
	}

	protected ResourceLocation getEntityTexture(SheepEntity p_110775_1_) {
		return SHEARED_SHEEP_TEXTURES;
	}
}
