# 🤖 Projet Machine Learning — Adult Census Income

Ce dépôt contient le projet complet d'analyse et de modélisation de Machine Learning sur le dataset **Adult Census Income** (prédiction de tranche de revenu `>50K` vs `<=50K`).

---

## 👥 À l'attention de l'équipe et des collègues

Ce projet est découpé en deux notebooks pour répondre aux exigences des séances de travaux pratiques et de la présentation finale :

| Fichier Notebook | Description & Contenu | Usage recommandé |
| :--- | :--- | :--- |
| **`seance1_nettoyage.ipynb`** | **Séance 1 — Nettoyage & Exploration** : Compréhension du métier et des données, traitement des valeurs manquantes (`?`), suppression des doublons et gestion des valeurs aberrantes (*outliers*). | **Présentation professeur (Séance 1)** |
| **`ml_adult_pipeline.ipynb`** | **Pipeline Complet ML** : Exploration approfondie (EDA), Feature Engineering, Sélection de variables, ACP (PCA), Entraînement et comparaison de 5+ modèles ML. | **Rapport final & Modélisation** |

---

## 📌 1. Objectif du Projet (Compréhension Métier)

* **Problème :** Prédire si le revenu annuel d'un individu dépasse 50 000 \$ (`>50K`) ou non (`<=50K`) à partir de ses caractéristiques socio-démographiques (âge, niveau d'étude, secteur d'activité, statut marital, heures de travail par semaine, etc.).
* **Cas d'usage réel :** Évaluation du risque de crédit bancaire, ciblage publicitaire haut de gamme, analyse des facteurs socio-économiques d'inégalité des revenus.

---

## 🛠️ 2. Structure et Étapes du Projet

### Étape 1 : Compréhension des Données & EDA
* Inspection des variables numériques (`age`, `capital-gain`, `capital-loss`, `hours-per-week`) et catégorielles (`workclass`, `education`, `occupation`, `marital-status`, etc.).
* Analyse bivariée de la cible `income`.

### Étape 2 : Nettoyage des Données (*Data Cleaning*)
1. **Valeurs manquantes :** Détection des symboles `?` dans `workclass`, `occupation` et `native-country`, puis imputation par la valeur la plus fréquente (**Mode**).
2. **Redondance / Doublons :** Identification et suppression des lignes strictement identiques (`drop_duplicates`).
3. **Valeurs aberrantes (*Outliers*) :** Visualisation par Boxplots et filtrage/écrêtage des heures de travail et capital extrême.

### Étape 3 : Feature Engineering & Preprocessing
* **Encoding :** Convertir le texte en valeurs numériques (*One-Hot Encoding* pour le catégoriel nominal, *Label Encoding* pour les variables ordinales).
* **Création de variables :** Calcul du gain financier net `net_capital_gain = capital-gain - capital-loss`.
* **Standardisation :** Application de `StandardScaler` pour ramener toutes les caractéristiques numériques à la même échelle.

### Étape 4 : Sélection de Variables & ACP (PCA)
* **Feature Selection :** Évaluation des variables les plus pertinentes via le score d'*Information Mutuelle* (Mutual Information) et l'importance des forêts aléatoires.
* **Réduction de dimension (ACP/PCA) :** Projection en composantes principales pour visualiser la séparabilité des classes en 2D.

### Étape 5 : Entraînement & Comparaison des Modèles
Séparation des données (80% Train / 20% Test) et évaluation sur plusieurs algorithmes :
* **Logistic Regression**
* **Decision Tree**
* **Random Forest**
* **XGBoost**
* **LightGBM / Gradient Boosting**

Metrics d'évaluation calculées : **Accuracy**, **F1-Score**, **AUC-ROC** et **Matrice de Confusion**.

---

## 🚀 3. Comment exécuter le projet localement ?

### Prérequis
Assurez-vous d'avoir Python 3.8+ et les packages requis installés :

```bash
pip install pandas numpy matplotlib seaborn scikit-learn xgboost lightgbm jupyterlab
```

### Lancement des Notebooks
Ouvrez Jupyter Lab ou VS Code dans ce dossier :

```bash
jupyter lab
```

Vous pouvez ensuite ouvrir et exécuter séquentiellement :
1. `seance1_nettoyage.ipynb`
2. `ml_adult_pipeline.ipynb`

---

## 📊 4. Graphiques Générés

Lors de l'exécution, les visualisations graphiques sont enregistrées sous forme d'images PNG dans le répertoire :
* `01_target_distribution.png` à `05_categorical_bivariate.png` (EDA)
* `07_missing_values.png` & `08_outliers_detection.png` (Cleaning)
* `12_pca_scree.png` à `14_pca_loadings.png` (ACP)
* `15_model_comparison.png` à `17_confusion_matrix.png` (Évaluation Modèles)

---
*Projet réalisé avec succès dans le cadre du cours de Machine Learning.*
