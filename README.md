# API REST — Gestion de Commandes

Le Projet Spring Boot consiste à concevoir une API REST de Gestion de commande dans le cadre du module Services WEB.

## Technologies utilisées
- Java 17
- Spring Boot 
- Spring Security + JWT 
- Spring Data JPA + Hibernate
- MySQL 
- Swagger / OpenAPI 
- Lombok
- Maven


## Prérequis
- Java 17 
- MySQL en cours d'exécution
- Maven installé (ou utiliser le wrapper `mvnw`)


## Lancement de l'application

### 1. Cloner le projet
```bash
git clone https://github.com/AntaDiagne01/commandes.git
cd commandes
```

### 2. Configurer la base de données
Créer une base de données MySQL ( mais c'est optionnel) :
```sql
CREATE DATABASE commandes_db;
```

### 3. Définir les variables d'environnement

#### Option A — Variables système (recommandé en production)
```bash
export SPRING_PROFILES_ACTIVE=dev
export DB_URL=jdbc:mysql://localhost:3306/commandes_db?createDatabaseIfNotExist=true
export DB_USERNAME=root
export DB_PASSWORD=ton_mot_de_passe
export SERVER_PORT=8081
```

#### Option B — Modifier directement `application-dev.yml`
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/commandes_db?createDatabaseIfNotExist=true
    username: root
    password: ton_mot_de_passe
```

### 4. Lancer l'application
```bash
mvn spring-boot:run
```

Ou depuis IntelliJ : clic droit sur `CommandesApplication.java` → **Run**.

L'application démarre sur **http://localhost:8081**


## Activation des profils Spring
Le profil actif est défini dans `application.yml` :
```yaml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
```

### Changer de profil

#### Via variable d'environnement
```bash
export SPRING_PROFILES_ACTIVE=prod
mvn spring-boot:run
```

#### Via ligne de commande Maven
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### Via IntelliJ
`Run → Edit Configurations → Environment Variables → SPRING_PROFILES_ACTIVE=dev`

### Comportement par profil

| Profil | Base de données | Tables | SQL affiché | Données de test |
|--------|----------------|--------|-------------|-----------------|
| `dev`  | MySQL local    | Créées/mises à jour automatiquement | Oui | Oui (DataInitializer) |
| `test` | H2 en mémoire  | Créées et supprimées à chaque test | Non | Non |
| `prod` | MySQL (variables d'env obligatoires) | Validées uniquement | Non | Non |

---

## Accès à Swagger UI

Une fois l'application lancée en profil `dev` :

http://localhost:8081/swagger-ui.html Interface graphique Swagger
http://localhost:8081/v3/api-docs Documentation JSON brute


> Mais Swagger est désactivé en profil `prod` pour des raisons de sécurité.

### Comment tester avec Swagger

1. Ouvrir **http://localhost:8081/swagger-ui.html**
2. Appeler `POST /api/auth/login` pour obtenir un token JWT
3. Cliquer sur le bouton **Authorize** en haut de la page
4. Coller le token JWT **sans guillemets** dans le champ `bearerAuth`
5. Cliquer **Authorize** puis **Close**
6. Tous les endpoints protégés enverront automatiquement le token

---

## Comptes disponibles au démarrage (profil dev)

| Username | Password | Rôle |
|----------|----------|------|
| `admin`  | `admin123` | ROLE_ADMIN |
| `user1`  | `user123`  | ROLE_USER  |
| `user2`  | `user123`  | ROLE_USER  |

---

## Exemples d'appels API

### Authentification

#### Inscription
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "nouveluser",
  "email": "user@test.sn",
  "password": "password123",
  "nom": "Nouveau User"
}
```

**Réponse 200 :**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "username": "nouveluser",
  "role": "ROLE_USER"
}
```

#### Connexion
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

#### Déconnexion
```http
POST /api/auth/logout
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

---

### Produits (ROLE_ADMIN requis pour POST/PUT/DELETE)

#### Créer un produit
```http
POST /api/produits
Authorization: Bearer <token_admin>
Content-Type: application/json

{
  "nom": "Ordinateur Dell Latitude",
  "prix": 450000.00,
  "stock": 15
}
```

**Réponse 201 :**
```json
{
  "id": 1,
  "nom": "Ordinateur Dell Latitude",
  "prix": 450000.00,
  "stock": 15
}
```

#### Lister tous les produits
```http
GET /api/produits
Authorization: Bearer <token>
```

#### Modifier un produit
```http
PUT /api/produits/1
Authorization: Bearer <token_admin>
Content-Type: application/json

{
  "nom": "Ordinateur Dell Latitude",
  "prix": 420000.00,
  "stock": 20
}
```

#### Supprimer un produit
```http
DELETE /api/produits/1
Authorization: Bearer <token_admin>
```

### Clients (ROLE_ADMIN requis)

#### Lister tous les clients
```http
GET /api/clients
Authorization: Bearer <token_admin>
```

**Réponse 200 :**
```json
[
  { "id": 1, "nom": "Administrateur", "email": "admin@polytech.sn" },
  { "id": 2, "nom": "Moussa Diallo",  "email": "user1@test.sn" }
]
```

### Commandes

#### Créer une commande
```http
POST /api/commandes
Authorization: Bearer <token>
Content-Type: application/json

{
  "clientId": 2,
  "lignes": [
    { "produitId": 1, "quantite": 2 },
    { "produitId": 2, "quantite": 1 }
  ]
}
```

**Réponse 201 :**
```json
{
  "id": 1,
  "dateCommande": "2026-06-10T10:30:00",
  "status": "CREATED",
  "client": { "id": 2, "nom": "Moussa Diallo", "email": "user1@test.sn" },
  "lignes": [
    {
      "id": 1,
      "produit": { "id": 1, "nom": "Ordinateur Dell Latitude", "prix": 450000.00, "stock": 15 },
      "quantite": 2,
      "prixUnitaire": 450000.00
    }
  ],
  "total": 900000.00
}
```

#### Valider une commande
```http
PATCH /api/commandes/1/valider
Authorization: Bearer <token_admin>
```

#### Annuler une commande
```http
PATCH /api/commandes/1/annuler
Authorization: Bearer <token_admin>
```

#### Commandes d'un client
```http
GET /api/commandes/client/2
Authorization: Bearer <token_admin>
```

## Auteur
Projet réalisé par **Anta Diagne** 
