package entities;

import java.util.Objects;

public abstract class Aquatic extends Animal {
    protected String habitat;
    
    public Aquatic() {
        super();
    }
    
    public Aquatic(String nom, String family, int age, boolean isMammal, String habitat) {
        super(nom, family, age, isMammal);
        this.habitat = habitat;
    }
    
    @Override
    public String toString() {
        return super.toString() + ", habitat=" + habitat;
    }
    
    public  abstract void swim() ;
    
    public String getHabitat() {
        return habitat;
    }
    
    public void setHabitat(String habitat) {
        this.habitat = habitat;
    }
    
   @Override
public boolean equals(Object obj) {
    return (obj instanceof Aquatic other)
            && Objects.equals(this.getNom(), other.getNom())
            && this.getAge() == other.getAge()
            && Objects.equals(this.getHabitat(), other.getHabitat());
}

}