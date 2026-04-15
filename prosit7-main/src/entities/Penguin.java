package entities;

public class Penguin extends Aquatic {
    protected float swimmingDepth;
    
    public Penguin() {
        super();
    }
    
    public Penguin(String nom, String family, int age, boolean isMammal, String habitat, float swimmingDepth) {
        super(nom, family, age, isMammal, habitat);
        this.swimmingDepth = swimmingDepth;
    }
     @Override
    public void swim() {
        System.out.println("This Penguin is swimming.");
    }
    @Override
    public String toString() {
        return super.toString() + ", swimmingDepth=" + swimmingDepth;
    }
    
    public float getSwimmingDepth() {
        return swimmingDepth;
    }
    
    public void setSwimmingDepth(float swimmingDepth) {
        this.swimmingDepth = swimmingDepth;
    }
}