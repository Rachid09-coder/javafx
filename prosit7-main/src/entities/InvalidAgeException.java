package entities;

public class InvalidAgeException extends Exception {
    public InvalidAgeException() {
        super("age d'animal invalide : l'age ne peut pas etre negatif.");
    }
    
    public InvalidAgeException(String message) {
        super(message);
    }
    
    public InvalidAgeException(int age) {
        super("age d'animal invalide : l'age ne peut pas être negatif. age fourni : " + age);
    }
}