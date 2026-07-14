# Stroke-MLOps

Projet de préparation et de prétraitement du jeu de données **ISLES 2022** pour l'entraînement de modèles de segmentation des lésions d'AVC ischémique à partir d'images IRM.

---

# Présentation du projet

Ce projet s'inscrit dans le cadre d'une plateforme **MLOps** dédiée au déploiement et à l'orchestration d'un modèle de segmentation des lésions d'AVC ischémique.

Cette première étape consiste à :

- Charger les images IRM au format **NIfTI (.nii.gz)**.
- Explorer les métadonnées du jeu de données.
- Visualiser les différentes modalités IRM et les masques de segmentation.
- Vérifier la qualité et la cohérence des données.
- Prétraiter les images avant l'entraînement.
- Diviser le jeu de données en ensembles d'entraînement, de validation et de test.

Les données prétraitées serviront ensuite à entraîner un modèle de segmentation tel que **U-Net** ou **nnU-Net**.

---

# Télécharger le jeu de données **ISLES 2022** depuis Zenodo :

Le projet utilise le jeu de données **ISLES 2022**, disponible publiquement sur Zenodo.

Télécharger le jeu de données depuis :

https://doi.org/10.5281/zenodo.7960856

Après le téléchargement, extraire les fichiers et placer le dossier **ISLES-2022** dans le répertoire `data` du projet afin d'obtenir l'arborescence suivante :

```text
Stroke-MLOps/
│
├── data/
│   ├── ISLES-2022/
│   │   ├── rawdata/
│   │   └── derivatives/
│   │
│   ├── preprocessed/
│   └── splits/
│
├── src/
└── ...
```

Le dossier **rawdata** contient les images IRM originales (ADC, DWI, FLAIR), tandis que le dossier **derivatives** contient les masques de segmentation utilisés comme vérité terrain (Ground Truth).

---

# Structure du projet

```
Stroke-MLOps/

├── data/
│   ├── ISLES-2022/
│   ├── preprocessed/
│   └── splits/
│       ├── train.csv
│       ├── validation.csv
│       └── test.csv
│
├── reports/
│
├── src/
│   ├── load_data.py
│   ├── metadata.py
│   ├── visualize.py
│   ├── quality_check.py
│   ├── preprocessing.py
│   ├── verify_preprocessing.py
│   ├── split_dataset.py
│   └── main.py
│
├── requirements.txt
├── README.md
└── .gitignore
```

---

# Fonctionnalités

## Chargement des données

Le script permet de :

- Vérifier la structure du jeu de données.
- Découvrir automatiquement les dossiers des patients.
- Identifier les modalités IRM disponibles.
- Associer les masques de segmentation correspondants.

Script :

```text
src/load_data.py
```

---

## Extraction des métadonnées

Extraction des informations contenues dans les fichiers NIfTI :

- Dimensions des images
- Résolution spatiale (voxel spacing)
- Type de données
- Matrice affine

Les résultats sont enregistrés sous forme de rapport CSV.

Script :

```text
src/metadata.py
```

---

## Visualisation des images IRM

Permet d'afficher :

- les images ADC ;
- les images DWI ;
- les images FLAIR ;
- les masques de segmentation superposés aux images.

Script :

```text
src/visualize.py
```

---

## Contrôle qualité

Le contrôle qualité vérifie notamment :

- la présence des fichiers attendus ;
- les dimensions des images ;
- la cohérence entre les images et les masques ;
- les valeurs NaN ou infinies ;
- les plages d'intensité ;
- les matrices affines.

Un rapport de contrôle qualité est généré automatiquement.

Script :

```text
src/quality_check.py
```

---

## Prétraitement des images

Le pipeline de prétraitement comprend :

- Chargement des images IRM ;
- Rééchantillonnage sur la grille de référence ADC ;
- Conversion des images en tableaux NumPy ;
- Vérification de la cohérence des dimensions ;
- Recadrage du cerveau (suppression du fond) ;
- Normalisation des intensités ;
- Redimensionnement des images.

Les données prétraitées sont enregistrées dans :

```
data/preprocessed/
```

Script :

```text
src/preprocessing.py
```

---

## Vérification du prétraitement

Ce script permet de vérifier que le prétraitement a été correctement appliqué :

- dimensions finales ;
- normalisation ;
- cohérence des images sauvegardées.

Script :

```text
src/verify_preprocessing.py
```

---

## Division du jeu de données

Le jeu de données est réparti en trois ensembles :

- Entraînement (Training)
- Validation
- Test

Les fichiers générés sont :

```
data/splits/train.csv
data/splits/validation.csv
data/splits/test.csv
```

Script :

```text
src/split_dataset.py
```

---

# Prérequis

- Python **3.10**
- pip

---

# Installation

Cloner le dépôt :

```bash
git clone <url_du_repository>

cd Stroke-MLOps
```

Créer un environnement virtuel :

```bash
python3.10 -m venv .venv
```

L'activer :

### Linux / macOS

```bash
source .venv/bin/activate
```

### Windows

```bash
.venv\Scripts\activate
```

Installer les dépendances :

```bash
pip install -r requirements.txt
```

---

# Exécution

Exécuter les scripts depuis la racine du projet.

Chargement des données :

```bash
python src/main.py
```

---

# Dépendances principales

Les principales bibliothèques utilisées sont :

- nibabel
- SimpleITK
- NumPy
- Pandas
- Matplotlib
- scikit-image
- scikit-learn
- tqdm

La liste complète est disponible dans le fichier **requirements.txt**.

---

# Évolutions prévues

Les prochaines étapes du projet comprennent :

- l'entraînement d'un modèle U-Net ;
- l'intégration de nnU-Net ;
- le suivi des expériences avec MLflow ;
- le développement d'une API REST avec FastAPI ;
- la conteneurisation avec Docker ;
- le déploiement sur Kubernetes ;
- la mise en place d'une chaîne CI/CD ;
- l'orchestration complète de la plateforme MLOps.

---

# Auteur

**Hazem Hadda**

Étudiant en cycle ingénieur – ESPRIT

Projet : **Stroke-MLOps**