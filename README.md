# ⚙️ GearData - Pipeline ETL (Java/SQL)

Ce dépôt contient le moteur de traitement de données du projet GearData. Il permet d'automatiser l'ingestion de données brutes vers une infrastructure PostgreSQL.

### 🏗️ Structure du Projet
* **/scripts** : `IngestionVoiture.java` - Script principal gérant l'extraction (CSV) et le chargement (SQL) via JDBC.
* **/data** : Fichiers sources (`data_voiture.csv`) servant de base à l'ingestion.
* **/lib** : Dépendances externes (Driver PostgreSQL JDBC).

### 🚀 Points Clés Techniques
* **Performance :** Gestion des insertions en mode "Batch" (500 lignes par transaction).
* **Robustesse :** Gestion des flux d'erreurs et des formats de caractères spéciaux lors du parsing.
* **Architecture :** Séparation claire entre la donnée source, la logique de traitement et les dépendances.

**Lien du projet Front-end :** [geardata-engine.vercel.app](https://geardata-engine.vercel.app)

# Project: Car Mats Data Pipeline 🚗 (Ultimate Edition)
## 📌 Présentation
Ce projet réalise l'ingestion, le nettoyage et la normalisation d'un catalogue de **3 046 références** de tapis automobile. L'enjeu était de transformer une source Excel "sale" (erreurs de saisie, dates impossibles, encodage corrompu) en une base de données PostgreSQL fiable.
> **Performance :** Le pipeline atteint un taux de succès de **100%** sur la transformation des données, garantissant un catalogue prêt pour l'exploitation commerciale.
> 
## 🛠 Stack Technique
 * **Java** : Automatisation de l'ingestion du flux CSV vers PostgreSQL.
 * **PostgreSQL** : Architecture de données (Staging & Production).
 * **SQL Avancé** : Nettoyage par expressions régulières (Regex) et logique de réparation de données.
## 🚀 Défis Techniques & Solutions
### 1. Correction des Erreurs de Saisie (Data Scrubbing)
Le fichier source contenait des mois saisis avec trois chiffres par erreur (ex: 101 pour octobre, 012 pour décembre).
**Solution SQL :** Utilisation de remplacements imbriqués pour restaurer l'intégrité des dates.
```sql
-- Normalisation des mois erronés
REPLACE(REPLACE(year_end, '/101/', '/10/'), '/012/', '/12/')

```
### 2. Gestion de l'Intégrité Calendaire
Certaines lignes présentaient des dates physiquement impossibles (ex: **31/11/1992** ou **31/04/2014**).
**Solution SQL :** Une logique de "fallback" ramène ces dates au dernier jour valide du mois (le 30) pour éviter l'échec de la conversion.
```sql
-- Exemple de bascule du 31 au 30 pour les mois de 30 jours
REPLACE(REPLACE(year_end, '31/04/', '30/04/'), '31/11/', '30/11/')

```
### 3. Résilience de l'Encodage et Bornes Ouvertes
Les produits "toujours en vente" étaient marqués "À ce jour", mais l'accent était souvent corrompu lors de l'export.
**Solution SQL :** Utilisation de recherches floues pour capturer toutes les variantes.
```sql
-- Capture des bornes ouvertes malgré l'encodage
WHEN year_end ILIKE '%jour%' OR year_end ILIKE '%pres%' THEN CURRENT_DATE

```
### 4. Normalisation des Modèles
Le champ modèle incluait souvent la marque de manière redondante.
**Solution SQL :** Extraction dynamique du nom du modèle propre.
```sql
-- Extraction : "RENAULT CLIO" -> "CLIO"
TRIM(SUBSTRING(modele_voiture FROM POSITION(' ' IN modele_voiture) + 1)) AS modele_propre

```
## 🏗 Architecture des données
J'ai appliqué une architecture à deux niveaux pour garantir la traçabilité :
 1. **Table Raw (staging.car_mats_raw)** : Zone de dépôt (Données brutes "vierges" telles qu'importées comportant des erreurs).
 2. **Table Final (staging.car_mats_final)** : Zone de production (Données nettoyées, typées en DATE et indexées pour la production).
## 📊 Audit de Qualité Final
| Indicateur | Valeur |
|---|---|
| **Total lignes traitées** | 3 046 |
| **Dates de début valides** | 3 045 |
| **Dates de fin valides** | 3 046 |
| **Taux de succès global** | **100.00%** |
*Ce projet démontre une capacité à gérer le cycle de vie de la donnée, de la source brute jusqu'à une table de production certifiée.*