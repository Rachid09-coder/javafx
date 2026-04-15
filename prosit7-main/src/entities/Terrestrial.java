package entities;

public class Terrestrial extends Animal {
    protected int nbrLegs;
    
    public Terrestrial() {
        super();
    }
    
    public Terrestrial(String nom, String family, int age, boolean isMammal, int nbrLegs) {
        super(nom, family, age, isMammal);
        this.nbrLegs = nbrLegs;
    }
    
    @Override
    public String toString() {
        return super.toString() + ", nbrLegs=" + nbrLegs;
    }
    
    public int getNbrLegs() {
        return nbrLegs;
    }
    
    public void setNbrLegs(int nbrLegs) {
        this.nbrLegs = nbrLegs;
    }
}