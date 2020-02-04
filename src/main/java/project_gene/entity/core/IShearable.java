package project_gene.entity.core;

@SuppressWarnings("deprecation")
public interface IShearable extends net.minecraftforge.common.IShearable {
	boolean getSheared();
	void setSheared(boolean sheared);
}
