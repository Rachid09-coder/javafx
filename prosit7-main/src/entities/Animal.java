package entities;
public class Animal {
    protected String nom;
    protected String family ;
    protected int age ;
    protected boolean isMammal;

    public Animal() {
        this.nom = "";
        this.family = "";
        this.age = 0;
        this.isMammal = false;
    }

    public Animal(String nom, String family, int age, boolean isMammal) {
        this.nom = nom;
        this.family = family;
        setAge(age); 
        this.isMammal = isMammal;
    }

    public String getNom() {
        return nom;
    }

   public String getFamily(){
    return this.family;
   }
   public int getAge(){
    return this.age;
   }

   public boolean isMammal() {
    return this.isMammal;
   }

   public void setNom(String nom) {
    this.nom = nom;
   }

   public void setFamily(String family) {
    this.family = family;
   }

   public void setAge(int age) {
    if (age < 0) {
        System.err.println("L'age d'un animal ne peut pas etre negatif.");
    }
    this.age = age;
   }

   public void setMammal(boolean isMammal) {
    this.isMammal = isMammal;
   }

   @Override
   public String toString() {
    return "Animal [nom=" + nom + ", family=" + family + ", age=" + age + ", isMammal=" + isMammal + "]";
   }
 
}
