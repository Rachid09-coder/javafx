package Main;
import entities.*;

public class App {
    public static void main(String[] args) {
        
        Zoo zoo = new Zoo("Zoo Test", 3, "Tunis");
        
        try {
            Dolphin dauphin = new Dolphin("Flipper", "Mammifere marin", 8, true, "mer", 35.5f);
            zoo.addAnimal(dauphin);
            System.out.println("Dauphin ajouté avec succes. Nombre d'animaux: " + zoo.getNbrCages());
            
            Terrestrial lion = new Terrestrial("Lion", "Felin", 5, true, 4);
            zoo.addAnimal(lion);
            System.out.println("Lion ajoute avec succes. Nombre d'animaux: " + zoo.getNbrCages());
            
            Penguin pingouin = new Penguin("Tux", "Oiseau", 2, false, "antarctique", 150.0f);
            zoo.addAnimal(pingouin);
            System.out.println("Pingouin ajoute avec succes. Nombre d'animaux: " + zoo.getNbrCages());
            
        } catch (ZooFullException e) {
            System.out.println("erreur - Zoo plein: " + e.getMessage());
            System.out.println("Nombre d'animaux actuel: " + zoo.getNbrCages());
        } catch (InvalidAgeException e) {
            System.out.println(" erreur - age invalide: " + e.getMessage());
            System.out.println("Nombre d'animaux actuel: " + zoo.getNbrCages());
        }
        
        try {
            Animal animalInvalide = new Animal("Animal Invalide", "Test", -3, true);
            zoo.addAnimal(animalInvalide);
            System.out.println("Animal ajoute. Nombre d'animaux: " + zoo.getNbrCages());
        } catch (ZooFullException e) {
            System.out.println("erreur - Zoo plein: " + e.getMessage());
            System.out.println("Nombre d'animaux actuel: " + zoo.getNbrCages());
        } catch (InvalidAgeException e) {
            System.out.println("eErreur -   age invalide: " + e.getMessage());
            System.out.println("Nombre d'animaux actuel: " + zoo.getNbrCages());
        }
        
        try {
            Terrestrial elephant = new Terrestrial("Elephant", "Mammifere", 15, true, 4);
            zoo.addAnimal(elephant);
            System.out.println("Elephant ajoute. Nombre d'animaux: " + zoo.getNbrCages());
        } catch (ZooFullException e) {
            System.out.println("erreur - Zoo plein: " + e.getMessage());
            System.out.println("Nombre d'animaux actuel: " + zoo.getNbrCages());
        } catch (InvalidAgeException e) {
            System.out.println("erreur - age invalide: " + e.getMessage());
            System.out.println("Nombre d'animaux actuel: " + zoo.getNbrCages());
        }
        
        System.out.println(zoo);
        zoo.displayAnimals();
        
        Animal[] animaux = zoo.getAnimals();
        for (int i = 0; i < zoo.getNbrCages(); i++) {
            if (animaux[i] instanceof Aquatic) {
                System.out.print(animaux[i].getNom() + ": ");
                ((Aquatic) animaux[i]).swim();
            }
        }
    }
}
