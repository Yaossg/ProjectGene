package project_gene.client.renderer;

import com.google.common.collect.Maps;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.model.CowModel;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import project_gene.ProjectGene;
import project_gene.client.renderer.layer.CowMushroomLayer;
import project_gene.entity.living.Cow;

import javax.annotation.Nullable;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class CowRenderer extends MobRenderer<Cow, CowModel<Cow>> {

	public CowRenderer(EntityRendererManager p_i47200_1_) {
		super(p_i47200_1_, new CowModel<Cow>() {
			private float headRotationAngleX;
			public void setLivingAnimations(Cow entityIn, float limbSwing, float limbSwingAmount, float partialTick) {
				super.setLivingAnimations(entityIn, limbSwing, limbSwingAmount, partialTick);
				this.headModel.rotationPointY = 6.0F + entityIn.getHeadRotationPointY(partialTick) * 9.0F;
				this.headRotationAngleX = entityIn.getHeadRotationAngleX(partialTick);
			}

			public void setRotationAngles(Cow entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
				super.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
				this.headModel.rotateAngleX = this.headRotationAngleX;
			}
		}, 0.7F);
		addLayer(new CowMushroomLayer(this));
	}
	
	@Override
	protected void preRenderCallback(Cow entitylivingbaseIn, float partialTickTime) {
		super.preRenderCallback(entitylivingbaseIn, partialTickTime);
	}

	private static final Map<Cow.MushroomType, ResourceLocation> texture = Util.make(Maps.newEnumMap(Cow.MushroomType.class), (map) -> {
		map.put(Cow.MushroomType.NONE, new ResourceLocation("textures/entity/cow/cow.png"));
		map.put(Cow.MushroomType.BROWN, new ResourceLocation("textures/entity/cow/brown_mooshroom.png"));
		map.put(Cow.MushroomType.RED, new ResourceLocation("textures/entity/cow/red_mooshroom.png"));
		map.put(Cow.MushroomType.MIXED, new ResourceLocation(ProjectGene.MODID, "textures/entity/cow/mixed_mooshroom.png"));
	});
	
	@Nullable
	@Override
	protected ResourceLocation getEntityTexture(Cow cow) {
		return texture.get(cow.getMushroomType());
	}
}
