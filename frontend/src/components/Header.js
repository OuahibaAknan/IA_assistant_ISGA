// src/components/Header.js
import React from "react";
import { Link, useNavigate } from "react-router-dom";

function Header() {
  const navigate = useNavigate();
  const token = localStorage.getItem("adminToken");

  const handleLogout = () => {
    localStorage.removeItem("adminToken");
    navigate("/login");
  };

  return (
    <header
      style={{
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        padding: "10px 20px",
        minHeight: "56px",
        background: "#f2f2f2",
        borderBottom: "1px solid rgba(0, 0, 0, 0.08)",
      }}
    >
      <div>
        <Link to="/" style={{ fontWeight: 700, color: "#111", textDecoration: "none" }}>
          ISGA
        </Link>
      </div>
      <div>
        {!token ? (
          <button style={{ padding: "8px 16px", borderRadius: "999px" }} onClick={() => navigate("/login")}>Connexion</button>
        ) : (
          <button style={{ padding: "8px 16px", borderRadius: "999px" }} onClick={handleLogout}>Déconnexion</button>
        )}
      </div>
    </header>
  );
}

export default Header;