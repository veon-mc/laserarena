# LaserArena – Gestion de sessions de lasergame avec QR Code

## Auteurs

- Groupe C131
- g64889 Lepomme Michael
- g66076 Ali Kadhem

## Description du Projet

Ce projet consiste en une application Java dédiée à la gestion de sessions de lasergame, utilisant des QR codes pour simplifier l'enregistrement des joueurs et le suivi des parties.

L'objectif est de permettre à une salle de lasergame de créer et gérer facilement des sessions de jeu (arènes, équipes, durées, scores…), tout en assurant un contrôle fluide lors de l'accès des joueurs à l'arène.

Les sessions sont créées par un opérateur (par exemple : "Session de lasergame – 30 min – 2 équipes – 10 joueurs"). Chaque joueur reçoit un billet unique associé à un QR code, qu'il peut présenter à l'entrée de l'arène (sur smartphone ou imprimé).

Lors de l'accès à la salle, le QR code est scanné à l'aide d'une douchette. L'application vérifie alors instantanément la validité du billet du joueur.

L'application permet à un opérateur de :
- créer des sessions de jeu (type de partie, durée, nombre de joueurs, équipes, etc.)
- consulter la liste des sessions et des billets générés.
- générer des billets uniques pour les joueurs d'une session.
- associer à chaque billet un QR code.
- scanner le billet d'un joueur lors de son entrée dans l'arène avec une douchette QR code.
- vérifier si le billet est valide, expiré, déjà utilisé ou inexistant.
- consulter et afficher les scores des joueurs à l'issue d'une session.
- enregistrer toutes les informations dans une base de données SQLite.

## Diagramme de Classe

/

## Choix de l'Architecture

/

## Plan de Tests Fonctionnels

/

## Calendrier hebdomadaire des tâches

/

## Installation et utilisation

/

## Problèmes connus de l'application

/

## Retrospective

/