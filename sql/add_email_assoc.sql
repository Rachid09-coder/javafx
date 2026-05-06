-- Script pour ajouter la colonne email_assoc nécessaire pour la récupération de mot de passe
ALTER TABLE user ADD COLUMN email_assoc VARCHAR(255) DEFAULT NULL;
