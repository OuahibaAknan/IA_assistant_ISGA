import React, { useEffect, useState } from "react";
import axios from "axios";

function Admin() {
  const [documents, setDocuments] = useState([]);
  const [name, setName] = useState("");
  const [type, setType] = useState("");
  const [content, setContent] = useState("");
  const [loading, setLoading] = useState(false);

  // 🔹 1️⃣ Charger tous les documents au montage
  const fetchDocuments = async () => {
    setLoading(true);
    try {
      const res = await axios.get("http://localhost:8080/api/admin/documents");
      setDocuments(res.data);
    } catch (error) {
      console.error("Erreur lors du chargement:", error);
      alert("Impossible de charger les documents 😔");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDocuments();
  }, []);

  // 🔹 2️⃣ Ajouter un document
  const handleAddDocument = async () => {
    if (!name || !type || !content) {
      alert("Veuillez remplir tous les champs !");
      return;
    }

    try {
      const res = await axios.post("http://localhost:8080/api/admin/documents", {
        name,
        type,
        content
      });

      setDocuments((prev) => [...prev, res.data]);
      setName("");
      setType("");
      setContent("");
    } catch (error) {
      console.error("Erreur ajout document:", error);
      alert("Impossible d'ajouter le document 😔");
    }
  };

  // 🔹 3️⃣ Supprimer un document
  const handleDelete = async (id) => {
    if (!window.confirm("Supprimer ce document ?")) return;

    try {
      await axios.delete(`http://localhost:8080/api/admin/documents/${id}`);
      setDocuments((prev) => prev.filter((doc) => doc.id !== id));
    } catch (error) {
      console.error("Erreur suppression:", error);
      alert("Impossible de supprimer 😔");
    }
  };

  return (
    <div style={{ padding: "20px", maxWidth: "800px", margin: "auto" }}>
      <h1>Admin - Gestion des documents ISGA</h1>

      {/* Formulaire ajout */}
      <div style={{ marginBottom: "20px", border: "1px solid #ccc", padding: "10px" }}>
        <h3>Ajouter un document</h3>
        <input
          type="text"
          placeholder="Nom"
          value={name}
          onChange={(e) => setName(e.target.value)}
          style={{ width: "100%", marginBottom: "5px" }}
        />
        <input
          type="text"
          placeholder="Type"
          value={type}
          onChange={(e) => setType(e.target.value)}
          style={{ width: "100%", marginBottom: "5px" }}
        />
        <textarea
          placeholder="Contenu"
          value={content}
          onChange={(e) => setContent(e.target.value)}
          style={{ width: "100%", marginBottom: "5px" }}
        />
        <button onClick={handleAddDocument}>Ajouter</button>
      </div>

      {/* Liste des documents */}
      <div>
        <h3>Documents existants</h3>
        {loading ? (
          <p>Chargement...</p>
        ) : documents.length === 0 ? (
          <p>Aucun document trouvé</p>
        ) : (
          <ul>
            {documents.map((doc) => (
              <li key={doc.id} style={{ marginBottom: "10px" }}>
                <b>{doc.name}</b> ({doc.type}) <br />
                {doc.content} <br />
                <button onClick={() => handleDelete(doc.id)}>Supprimer</button>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}

export default Admin;