# Projet BooqIn

## Présentation

Le site BooqIn est un réseau social dédié aux livres.

Un des concepts centraux du site est celui des collections de livres. Les utilisateurs peuvent créer leurs propres collections de livres, y compris des **smart collections** : des collections basées sur des filtres qui se mettent à jour automatiquement. Certaines collections sont également générées automatiquement par le site.

Le site offre toutes les fonctionnalités d'un réseau social. Les utilisateurs peuvent **commenter** et **voter** sur les livres, les commentaires des autres, ainsi que sur les collections. Ils peuvent également **suivre** des collections, des livres et d'autres utilisateurs. Ils recevront des notifications en cas d'activité sur l'élément qu'ils suivent.

## Mes contributions

- Récupération des données des APIs Google et Openlibrary
- Planificateur de tâches pour les opérations récurrentes
- Vote sur les livres et commentaires
- Barre de recherche
- Choix des filtres
- Création & mise à jour des smart collections
- Une collection publique par auteur
- Une collection des livres votés
- Une collection des livres ajoutés au cours des 30 derniers jours

## Captures d'écran
<p align="center">
  <img src="https://github.com/user-attachments/assets/9467d010-5ddf-4de7-8715-906521dc6703" width="350" />
  <img src="https://github.com/user-attachments/assets/686ae3a9-72bc-42cd-98c8-9961fe727046" width="350" />
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/16216ca3-6fa9-4711-93c4-b6990f3f6967" width="350" />
  <img src="https://github.com/user-attachments/assets/d99c6ed4-8cc9-44db-a0ab-a4a6cfc1c6b6" width="350" />
  <img src="https://github.com/user-attachments/assets/b396af3b-d0dd-4244-bd10-1b6ab99f1d90" width="350" />
</p>

## Membres

| nom     | prenom         | email                                   |
|---------|----------------|-----------------------------------------|
| Antoine | BASTOS         | antoine.bastos@edu.univ-eiffel.fr       |
| Hakim   | AOUDIA         | hakim.aoudia@edu.univ-eiffel.fr         |
| Walid   | BAKHTI         | walid.bakhti@edu.univ-eiffel.fr         |
| Yazan   | ALJANNOUD      | yazan.aljannoud@edu.univ-eiffel.fr      |
| Nafis   | BASHEER AHAMED | nafis.basheer-ahamed@edu.univ-eiffel.fr |

## Stack

- **Backend**: 
  - Spring Boot
  - Spring Data JPA
- **Base de données**: 
  - H2
  - **Frontend**:
  - Vite React, Tailwind CSS
  - JTE (Java Template Engine) pour le rendu côté serveur

## Architecture

L'application utilise la 'Clean Architecture' avec les couches suivantes :

- **App** : Contrôleurs, Services
- **Domain** : contient les règles métier et le modèle indépendants du framework
- **Infrastructure** : contient le code spécifique au framework (Spring, H2, etc.), la persistance, les adaptateurs d'API externes, etc.

Quelques compromis ont été faits pour simplifier l'architecture en raison des contraintes de temps et de la taille du projet :

- Abstraction réduite, omission de certaines interfaces pour éviter la verbosité entre service et dépôt
- Utilisation de certains objets du domaine dans les entités (principalement des enums)

## Développement

### Il est préférable d'utiliser IntelliJ pour exécuter l'application

- Installer Java 21 ou une version supérieure
- Installer Maven
- Installer Node.js
- Ajouter un fichier `resources/secret.properties` avec la propriété `booqin.jwt.secret`
- Exécuter l'application depuis l'IDE
- Vous pouvez aller sur http://localhost:8080 pour voir le front JTE
- Pour développer le front React, allez dans le dossier `react/` et exécutez `yarn install`
- Lancez le front React en mode développement avec rechargement live en exécutant `yarn dev` dans le dossier `react/`
- Vous pouvez aller sur http://localhost:5173 pour voir le front React

Si vous souhaitez vérifier que Spring sert le React, il doit être compilé et les sources copiées dans le dossier `resources/static/React`. Cela est fait lors de la compilation. Vous n'avez donc rien d'autre à faire que :

```bash
mvn compile
```

Ensuite, l'application React sera disponible sur http://localhost:8080/React. Vous pouvez exécuter la commande ci-dessus **à tout moment**, même lorsque Spring est en cours d'exécution.

## Exécution après clonage

L'objectif est d'empaqueter l'application dans un fichier jar unique et de l'exécuter avec le profil *prod*. Lors de l'empaquetage, le frontend React sera construit et inclus dans le fichier jar afin que l'application Spring le serve.

Ajoutez un secret Spring dans `resources/secret.properties` avec les propriétés suivantes :

```properties
booqin.jwt.secret='AGPlY!61rSMBv__CHANGE_THIS_SECRET__RtdyotqDg1fdIF9u!#Z'
stripe.api.key=sk_test_<add_your_key>
```

```bash
mvn package -Pproduction -DskipTests
```

```bash
java -jar target/booqin-x.x.x.jar --spring.profiles.active=prod --booqin.fixtures.enabled=true
```

(`--booqin.fixtures.enabled=true` pour générer des données fictives)

ou

```bash
export SPRING_PROFILES_ACTIVE=prod && java -jar booqin-x.x.x.jar
```

Ensuite :

- http://localhost:8080/spa (front React)
- http://localhost:8080 (front léger JTE)

## Astuces

- Utilisez `--booqin.fixtures.enabled=true`, ou mettez-le à true dans `app.properties`, pour exécuter les fixtures
- En dev, n'utilisez pas `mvn package` (cela lance les tests, c'est lent), utilisez plutôt `mvn compile`
- Utilisez `mvn <goal> -DskipTests` pour ignorer les tests
- Utilisez `mvn <goal> -Pnoreact` pour zapper la construction du frontend. `-P` sert à définir le profil. Le `pom.xml` contient un profil nommé `noReact`.

