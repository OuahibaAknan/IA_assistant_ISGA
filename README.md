# IA Assistant ISGA

Dépôt du projet assistant IA ISGA. Le backend Spring Boot se trouve dans `backend/` et le frontend React dans `frontend/`.

## Backend

Prérequis : Java 17+ et MongoDB.

Depuis `backend/`, configurez `OPEN_AI_KEY` et `JWT_SECRET` dans l'environnement, puis lancez :

```powershell
$env:OPEN_AI_KEY = "votre-cle-openai"
$env:JWT_SECRET = "une-cle-secrete-aleatoire-d-au-moins-32-caracteres"
.\mvnw.cmd spring-boot:run
```

MongoDB doit être disponible à `mongodb://localhost:27017/chatbot_db` (modifiable dans `backend/src/main/resources/application.properties`).

Les fichiers téléversés et les fichiers de compilation ne sont pas versionnés.

## Frontend

Prérequis : Node.js et npm. Depuis `frontend/` :

```powershell
npm install
npm start
```

Par défaut, le frontend appelle le backend sur `http://localhost:8080`. Pour utiliser une autre URL, définissez `REACT_APP_API_URL` avant de démarrer ou compiler l'application.
