import React, { useState } from "react";
import { motion } from "framer-motion";
import { useNavigate, Link } from "react-router-dom";
import { api } from "../utils/api";
import "../styles/Auth.css";
import PageIntro from "../components/PageIntro";

const cardVariants = {
  hidden: { opacity: 0, y: 30, scale: 0.97 },
  visible: {
    opacity: 1,
    y: 0,
    scale: 1,
    transition: { duration: 0.45, ease: "easeOut" },
  },
};

function Login() {
  const [email, setEmail] = useState("admin@isga.ma");
  const [password, setPassword] = useState("admin123");
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const res = await api.post("/api/admin/auth/login", {
        email,
        password,
      });

      if (res.data?.token) {
        localStorage.setItem("adminToken", res.data.token);

        if (res.data.email) {
          localStorage.setItem("adminEmail", res.data.email);
        }

        navigate("/admin/dashboard");
      } else if (res.data?.status === "success") {
        localStorage.setItem("adminToken", "connected");
        navigate("/admin/dashboard");
      } else {
        alert(res.data?.message || "Identifiants incorrects.");
      }
    } catch (err) {
      console.error("Erreur login :", err);
      alert(err.response?.data?.message || "Erreur serveur. Vérifiez le backend.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="auth-page">
      <PageIntro pageName="Connexion admin" />
      <motion.form
        className="auth-card"
        onSubmit={handleLogin}
        variants={cardVariants}
        initial="hidden"
        animate="visible"
      >
        <img src="/logo.png" alt="ISGA" className="auth-logo" />

        <span className="auth-badge">Administration ISGA</span>

        <h2>Connexion admin</h2>

        <p>
          Accédez au tableau de bord pour gérer les documents, le RAG,
          l’historique et les statistiques du chatbot.
        </p>

        <label>
          Email
          <input
            type="email"
            value={email}
            placeholder="admin@isga.ma"
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </label>

        <label>
          Mot de passe
          <input
            type="password"
            value={password}
            placeholder="••••••••"
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </label>

        <button type="submit" disabled={loading}>
          {loading ? "Connexion..." : "Se connecter"}
        </button>

        <Link to="/" className="back-home">
          Retour à l’accueil
        </Link>
      </motion.form>
    </main>
  );
}

export default Login;