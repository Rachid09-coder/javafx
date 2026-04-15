package entities;

public class Dolphin extends Aquatic {
    protected float swimmingSpeed;
    
    public Dolphin() {
        super();
    }
    
    public Dolphin(String nom, String family, int age, boolean isMammal, String habitat, float swimmingSpeed) {
        super(nom, family, age, isMammal, habitat);
        this.swimmingSpeed = swimmingSpeed;
    }
    
    @Override
    public String toString() {
        return super.toString() + ", swimmingSpeed=" + swimmingSpeed;
    }
    
    @Override
    public void swim() {
        System.out.println("This dolphin is swimming.");
    }
    
    public float getSwimmingSpeed() {
        return swimmingSpeed;
    }
    
    public void setSwimmingSpeed(float swimmingSpeed) {
        this.swimmingSpeed = swimmingSpeed;
    }
}