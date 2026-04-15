package entities;
public class Zoo {
    private String nom;
    private String city;
    private Animal[] animals;
    private int nbrCages; 
    private   Aquatic  []aquatiques;
    private  int count ;


    public Zoo(String nom, int capacite, String city) {
        this.aquatiques=new Aquatic[10];
        this.count =0;
        setNom(nom); 
        this.city = city;
        this.animals = new Animal[capacite];
        this.nbrCages = 0;
    }

    public String getNom() {
        return nom;
    }

    public String getCity() {
        return city;
    }

    public int getNbrCages() {
        return nbrCages;
    }

    public Animal[] getAnimals() {
        return animals;
    }

    public void setNom(String nom) {
        if (nom == null || nom.trim().isEmpty()) {
            System.out.println("Le nom d'un zoo ne doit pas etre vide.");
        }
        this.nom = nom;
    }

    public void setCity(String city) {
        this.city = city;
    }

   public void addAnimal(Animal a) throws ZooFullException, InvalidAgeException {
    if (a.getAge() < 0) {
        throw new InvalidAgeException(a.getAge());
    }
    
    if (isZooFull()) {
        throw new ZooFullException();
    }
    
    if (searchAnimal(a) != -1) {
        System.out.println("Animal deja present dans le zoo : " + a.getNom());
        return;
    }

    animals[nbrCages] = a;
    nbrCages++;
   }  
public void displayAnimals() {
        for (int i = 0; i < nbrCages; i++) {
            System.out.println(animals[i].toString());
        }
    }

    public int searchAnimal(Animal animal) {
        for (int i = 0; i < nbrCages; i++) {
            if (animals[i].getNom().equals(animal.getNom())) {
                return i;
            }
        }
        return -1;
    }

    public boolean removeAnimal(Animal animal) {
        int index = searchAnimal(animal);
        if (index == -1) return false;
        for (int i = index; i < nbrCages - 1; i++) {
            animals[i] = animals[i + 1];
        }
        animals[nbrCages - 1] = null;
        nbrCages--;
        return true;
    }
    public boolean isZooFull() {
        return nbrCages == animals.length ;
    }

    public static Zoo comparerZoo(Zoo z1, Zoo z2) {
        if (z1.nbrCages >= z2.nbrCages) return z1;
        return z2;
    }

    public String toString() {
        return "Zoo " + getNom() + " à " + city + " contient " + nbrCages + " animaux.";
    }
      public void addquaticAnimal(Aquatic x){

        if(aquatiques.length>this.count){
            this.aquatiques[count]=x;
            count ++;
        }
        else
         {
            System.out.println("zoo plein");
         }


   }
   public void makeAllSwim(){
    for(Aquatic a :this.aquatiques ){
        if(a != null)
        a.swim();
    }
   }
   public float maxPenguinSwiming(){
    
    float max =0;
    for(Aquatic a : this.aquatiques)
        if(a instanceof Penguin){
            Penguin p=(Penguin)a;
            if(p.swimmingDepth>max)
            max=p.swimmingDepth;
        }


    return max;

   }
    public void diplayNumberOfAquaticsByType(){
        if(this.aquatiques != null){
            int p =0;
            int d= 0;
            for(Aquatic a : this.aquatiques){
                if(a instanceof Penguin)
                p++;
                if(a instanceof Dolphin)
                d++;

            }
            System.out.println("Penguin ="+p);
            System.out.println("Dolphin ="+d);

        }
        else 
        System.out.println("vide ");
    }
}
