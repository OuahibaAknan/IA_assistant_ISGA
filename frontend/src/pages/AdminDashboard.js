import React, { useCallback, useEffect, useMemo, useState } from "react";
import { motion } from "framer-motion";
import { Link, useNavigate } from "react-router-dom";
import { api } from "../utils/api";
import "../styles/AdminDashboard.css";
import PageIntro from "../components/PageIntro";
import {
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Tooltip,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
} from "recharts";

const dashboardVariants = {
  hidden: { opacity: 0, y: 24 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.45, ease: "easeOut" } },
};

function AdminDashboard() {
  const navigate = useNavigate();

  const [documents, setDocuments] = useState([]);
  const [stats, setStats] = useState(null);
  const [history, setHistory] = useState([]);
  const [selectedFile, setSelectedFile] = useState(null);
  const [uploadMessage, setUploadMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const [reindexLoading, setReindexLoading] = useState(false);
  const [selectedQuestions, setSelectedQuestions] = useState([]);

  const [testQuestion, setTestQuestion] = useState("");
  const [testResult, setTestResult] = useState(null);
  const [testLoading, setTestLoading] = useState(false);

  const [selectedHistoryItem, setSelectedHistoryItem] = useState(null);
  const [exportOpen, setExportOpen] = useState(false);
  const [introDone, setIntroDone] = useState(false);

  const isConnected = !!localStorage.getItem("adminToken");

  const logout = useCallback(() => {
    localStorage.removeItem("adminToken");
    localStorage.removeItem("adminEmail");
    navigate("/login");
  }, [navigate]);

  const fetchDocuments = useCallback(async () => {
    try {
      const res = await api.get("/api/admin/documents");
      setDocuments(res.data || []);
    } catch (err) {
      if (err.response?.status === 401 || err.response?.status === 403) {
        logout();
      }
    }
  }, [logout]);

  const fetchStats = useCallback(async () => {
    try {
      const res = await api.get("/api/admin/stats");
      setStats(res.data);
    } catch (err) {
      console.error("Erreur stats :", err);
    }
  }, []);

  const fetchHistory = useCallback(async () => {
    try {
      const res = await api.get("/api/admin/history/last");
      setHistory(res.data || []);
      setSelectedQuestions([]);
    } catch (err) {
      console.error("Erreur historique :", err);
    }
  }, []);

  useEffect(() => {
    if (!isConnected) {
      navigate("/login");
      return;
    }

    fetchDocuments();
    fetchStats();
    fetchHistory();
  }, [isConnected, navigate, fetchDocuments, fetchStats, fetchHistory]);

  const getRealStatus = (item) => {
    const status = (item?.status || "").toLowerCase();
    const answer = (item?.answer || "").toLowerCase();

    if (
      status.includes("not_found") ||
      status.includes("no_indexed_document") ||
      status.includes("out_of_scope") ||
      status.includes("error") ||
      answer.includes("information n’est pas disponible") ||
      answer.includes("information n'est pas disponible") ||
      answer.includes("cette information n’est pas disponible") ||
      answer.includes("cette information n'est pas disponible") ||
      answer.includes("information non disponible") ||
      answer.includes("pas disponible dans les documents") ||
      answer.includes("absente des documents") ||
      answer.includes("aucun extrait fiable") ||
      answer.includes("aucun document") ||
      answer.includes("je suis spécialisé") ||
      answer.includes("hors sujet")
    ) {
      return "not_found_in_documents";
    }

    return "success";
  };

  const handleUpload = async () => {
    if (!selectedFile) {
      setUploadMessage("Veuillez sélectionner un document.");
      return;
    }

    const formData = new FormData();
    formData.append("file", selectedFile);

    setLoading(true);
    setUploadMessage("");

    try {
      await api.post("/api/admin/documents/upload", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });

      setUploadMessage("Document ajouté et indexé avec succès.");
      setSelectedFile(null);
      await fetchDocuments();
      await fetchStats();
    } catch (err) {
      setUploadMessage("L’ajout du document a échoué.");
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteDocument = async (id) => {
    if (!window.confirm("Supprimer ce document ?")) return;

    try {
      await api.delete(`/api/admin/documents/${id}`);
      setUploadMessage("Document supprimé. La base documentaire a été mise à jour.");
      await fetchDocuments();
      await fetchStats();
    } catch (err) {
      setUploadMessage("La suppression du document a échoué.");
    }
  };

  const handleReindex = async () => {
    setReindexLoading(true);
    setUploadMessage("");

    try {
      await api.post("/api/admin/documents/reindex");
      setUploadMessage("Base documentaire réindexée avec succès.");
      await fetchDocuments();
      await fetchStats();
    } catch (err) {
      setUploadMessage("La réindexation a échoué.");
    } finally {
      setReindexLoading(false);
    }
  };

  const handleTestRag = async () => {
    if (!testQuestion.trim()) return;

    setTestLoading(true);
    setTestResult(null);

    try {
      const start = performance.now();

      const res = await api.post("/api/rag/ask", {
        question: testQuestion,
        history: [],
      });

      const end = performance.now();

      const result = {
        answer: res.data.answer,
        status: res.data.status,
        sources: res.data.sources || [],
        responseTime: Math.round(end - start),
      };

      setTestResult({
        ...result,
        status: getRealStatus(result),
      });

      await fetchStats();
      await fetchHistory();
    } catch (err) {
      setTestResult({
        answer: "Erreur lors du test RAG.",
        status: "not_found_in_documents",
        sources: [],
        responseTime: null,
      });
    } finally {
      setTestLoading(false);
    }
  };

  const toggleQuestionSelection = (id) => {
    if (!id) return;

    setSelectedQuestions((prev) =>
      prev.includes(id) ? prev.filter((itemId) => itemId !== id) : [...prev, id]
    );
  };

  const toggleSelectAllQuestions = () => {
    const allIds = history.map((item) => item.id || item._id).filter(Boolean);
    setSelectedQuestions(selectedQuestions.length === allIds.length ? [] : allIds);
  };

  const handleDeleteSelectedQuestions = async () => {
    if (selectedQuestions.length === 0) return;

    if (!window.confirm(`Supprimer ${selectedQuestions.length} question(s) ?`)) {
      return;
    }

    try {
      await Promise.all(
        selectedQuestions.map((id) => api.delete(`/api/admin/history/${id}`))
      );

      setHistory((prev) =>
        prev.filter((item) => !selectedQuestions.includes(item.id || item._id))
      );

      setSelectedQuestions([]);
      await fetchStats();
    } catch (err) {
      alert("Erreur lors de la suppression.");
    }
  };

  const downloadSelectedFile = async (url, fileName) => {
    if (selectedQuestions.length === 0) {
      alert("Veuillez sélectionner au moins une question à exporter.");
      return;
    }

    try {
      const res = await api.post(
        url,
        { ids: selectedQuestions },
        { responseType: "blob" }
      );

      const blobUrl = window.URL.createObjectURL(new Blob([res.data]));
      const link = document.createElement("a");

      link.href = blobUrl;
      link.setAttribute("download", fileName);
      document.body.appendChild(link);
      link.click();

      link.remove();
      window.URL.revokeObjectURL(blobUrl);
      setExportOpen(false);
    } catch (err) {
      console.error("Erreur export :", err);
      alert("Erreur lors de l’export. Vérifiez le backend.");
    }
  };

  const computedUnanswered = useMemo(() => {
    return history.filter((item) => getRealStatus(item) === "not_found_in_documents").length;
  }, [history]);

  const unansweredQuestions =
    stats?.unansweredQuestions && stats.unansweredQuestions > 0
      ? stats.unansweredQuestions
      : computedUnanswered;

  const questionCategories = useMemo(() => {
    const categories = [
      { name: "Admission", keywords: ["admission", "inscription", "concours", "dossier"] },
      { name: "Frais", keywords: ["frais", "prix", "paiement", "coût", "scolarité"] },
      { name: "Campus", keywords: ["campus", "fès", "fes", "casablanca", "rabat", "marrakech"] },
      { name: "Formations", keywords: ["formation", "filière", "filiere", "master", "cycle"] },
    ];

    return categories.map((category) => {
      const count = history.filter((item) => {
        const question = (item.question || "").toLowerCase();
        return category.keywords.some((keyword) => question.includes(keyword));
      }).length;

      return { name: category.name, count };
    });
  }, [history]);

  const statusData = [
    { name: "Réussies", value: history.filter((item) => getRealStatus(item) === "success").length },
    { name: "Sans réponse", value: unansweredQuestions },
    { name: "Erreurs", value: stats?.statusStats?.error || 0 },
  ];

  const STATUS_COLORS = ["#00c853", "#ff1744", "#ffb300"];

  const topDocumentsChart =
    stats?.topDocuments?.length > 0
      ? stats.topDocuments.map((doc) => ({
          name:
            doc.document?.length > 18
              ? doc.document.substring(0, 18) + "..."
              : doc.document,
          value: doc.count,
        }))
      : documents.map((doc) => ({
          name:
            (doc.originalFileName || doc.fileName || "Document").length > 18
              ? (doc.originalFileName || doc.fileName || "Document").substring(0, 18) + "..."
              : doc.originalFileName || doc.fileName || "Document",
          value: doc.indexed ? 1 : 0,
        }));

  return (
    <>
      {!introDone && (
        <PageIntro
          pageName="Dashboard admin"
          onFinish={() => setIntroDone(true)}
        />
      )}

      <motion.main
        className={`admin-shell ${!introDone ? "dashboard-hidden" : ""}`}
        variants={dashboardVariants}
        initial="hidden"
        animate="visible"
      >
        <aside className="admin-sidebar">
        <div>
          <div className="brand-block">
            <img src="/logo.png" alt="ISGA" />
            <div>
              <h2>ISGA</h2>
              <p>Administration IA</p>
            </div>
          </div>

          <nav className="admin-menu">
            <a href="#overview">Tableau de bord</a>
            <a href="#documents">Documents</a>
            <a href="#test-rag">Test RAG</a>
            <a href="#analytics">Analyse IA</a>
            <a href="#history">Historique</a>
          </nav>
        </div>

        <div className="sidebar-actions">
          <Link to="/" className="secondary-action">
            Retour à la vitrine
          </Link>

          <button onClick={logout} className="danger-action">
            Déconnexion
          </button>
        </div>
      </aside>

      <section className="admin-main">
        <header className="admin-topbar">
          <div>
            <span className="eyebrow">Plateforme intelligente ISGA</span>
            <h1>Centre de supervision du chatbot</h1>
           
          </div>

          <div className="status-card">
            <span>Système</span>
            <strong>Opérationnel</strong>
            
          </div>
        </header>

        <section className="metrics-grid" id="overview">
          <article className="metric-card">
            <span>Documents</span>
            <strong>{stats?.totalDocuments ?? documents.length}</strong>
            <p>Total des fichiers ajoutés</p>
          </article>

          <article className="metric-card">
            <span>Documents indexés</span>
            <strong>{stats?.indexedDocuments ?? 0}</strong>
            <p>Disponibles pour le chatbot</p>
          </article>

          <article className="metric-card">
            <span>Total questions</span>
            <strong>{stats?.totalQuestions ?? history.length}</strong>
            <p>Interactions enregistrées</p>
          </article>

          <article className="metric-card danger">
            <span>Questions sans réponse</span>
            <strong>{unansweredQuestions}</strong>
            <p>Informations absentes ou non trouvées</p>
          </article>

          <article className="metric-card">
            <span>Temps moyen IA</span>
            <strong>{stats?.averageResponseTime ?? 0} ms</strong>
            <p>Temps moyen de génération</p>
          </article>

          <article className="metric-card">
            <span>Score moyen chunks</span>
            <strong>{stats?.averageChunkScore ?? 0}</strong>
            <p>Pertinence moyenne RAG</p>
          </article>

          <article className="metric-card">
            <span>Total chunks</span>
            <strong>{stats?.totalChunks ?? 0}</strong>
            <p>Extraits indexés dans MongoDB</p>
          </article>

          <article className="metric-card success">
            <span>Réponses réussies</span>
            <strong>{history.filter((item) => getRealStatus(item) === "success").length}</strong>
            <p>Questions correctement traitées</p>
          </article>
        </section>

        <section className="content-grid">
          <article className="panel panel-large" id="documents">
            <div className="panel-header">
              <div>
                <h2>Base documentaire</h2>
                <p>Documents exploités comme source de connaissance par le chatbot.</p>
              </div>
            </div>

            <div className="upload-row">
              <input
                type="file"
                accept=".pdf,.docx,.txt"
                onChange={(e) => {
                  setSelectedFile(e.target.files[0]);
                  setUploadMessage("");
                }}
              />

              <button onClick={handleUpload} disabled={loading}>
                {loading ? "Ajout en cours" : "Ajouter"}
              </button>

              <button onClick={handleReindex} disabled={reindexLoading} className="outline-btn">
                {reindexLoading ? "Réindexation" : "Réindexer"}
              </button>
            </div>

            {uploadMessage && <div className="notice">{uploadMessage}</div>}

            <div className="table-scroll">
              <table>
                <thead>
                  <tr>
                    <th>Document</th>
                    <th>Type</th>
                    <th>Indexation</th>
                    <th>Date</th>
                    <th></th>
                  </tr>
                </thead>

                <tbody>
                  {documents.map((doc) => (
                    <tr key={doc.id || doc._id}>
                      <td>{doc.originalFileName || doc.fileName}</td>
                      <td>{doc.fileType || "-"}</td>
                      <td>
                        <span className={doc.indexed ? "pill success" : "pill warning"}>
                          {doc.indexed ? "Indexé" : "En attente"}
                        </span>
                      </td>
                      <td>{doc.uploadedAt ? new Date(doc.uploadedAt).toLocaleString() : "-"}</td>
                      <td>
                        <button
                          className="text-danger"
                          onClick={() => handleDeleteDocument(doc.id || doc._id)}
                        >
                          Supprimer
                        </button>
                      </td>
                    </tr>
                  ))}

                  {documents.length === 0 && (
                    <tr>
                      <td colSpan="5" className="empty-cell">
                        Aucun document disponible.
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </article>

          <article className="panel" id="analytics">
            <div className="panel-header">
              <div>
                <h2>Top questions par genre</h2>
                <p>Répartition automatique des demandes les plus fréquentes.</p>
              </div>
            </div>

            <div className="category-list">
              {questionCategories.map((cat) => (
                <div className="category-item" key={cat.name}>
                  <div>
                    <strong>{cat.name}</strong>
                    <span>{cat.count} question(s)</span>
                  </div>

                  <div className="bar">
                    <span
                      style={{
                        width: `${
                          history.length ? Math.max((cat.count / history.length) * 100, 5) : 5
                        }%`,
                      }}
                    ></span>
                  </div>
                </div>
              ))}
            </div>
          </article>
        </section>

        <section className="panel test-rag-panel" id="test-rag">
          <div className="panel-header">
            <div>
              <h2>Test RAG avancé</h2>
              <p>Tester une question, analyser la réponse, les sources, les scores et les chunks utilisés.</p>
            </div>
          </div>

          <div className="test-rag-box">
            <textarea
              value={testQuestion}
              onChange={(e) => setTestQuestion(e.target.value)}
              placeholder="Exemple : Quels sont les frais d’inscription à l’ISGA ?"
            />

            <button onClick={handleTestRag} disabled={testLoading}>
              {testLoading ? "Analyse en cours..." : "Tester le chatbot"}
            </button>
          </div>

          {testResult && (
            <div className="test-result">
              <div className="test-meta">
                <span className={`status-chip ${testResult.status}`}>{testResult.status}</span>

                {testResult.responseTime && <span>{testResult.responseTime} ms</span>}

                <span>{testResult.sources.length} source(s)</span>
              </div>

              <h3>Réponse générée</h3>
              <p>{testResult.answer}</p>

              <h3>Sources et chunks utilisés</h3>

              {testResult.sources.length === 0 ? (
                <p className="empty-history">Aucune source utilisée.</p>
              ) : (
                <div className="source-grid">
                  {testResult.sources.map((source, index) => (
                    <article className="source-card" key={index}>
                      <div className="source-top">
                        <strong>{source.sourceName || "Document"}</strong>
                        <span>{source.hybridScore ?? source.score ?? 0}</span>
                      </div>

                      <small>Chunk #{source.chunkId}</small>
                      <p>{source.preview}</p>
                    </article>
                  ))}
                </div>
              )}
            </div>
          )}
        </section>

        <section className="charts-grid">
          <div className="panel chart-panel">
            <div className="panel-header">
              <div>
                <h2>Répartition des réponses IA</h2>
                <p>Analyse des réponses générées par le moteur RAG.</p>
              </div>
            </div>

            <div className="chart-box">
              <ResponsiveContainer width="100%" height={330}>
                <PieChart>
                  <Pie
                    data={statusData}
                    dataKey="value"
                    nameKey="name"
                    outerRadius={112}
                    innerRadius={58}
                    paddingAngle={5}
                    label
                  >
                    {statusData.map((entry, index) => (
                      <Cell key={index} fill={STATUS_COLORS[index]} />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            </div>
          </div>

          <div className="panel chart-panel">
            <div className="panel-header">
              <div>
                <h2>Documents les plus utilisés</h2>
                <p>Documents les plus sollicités par le chatbot.</p>
              </div>
            </div>

            <div className="chart-box">
              <ResponsiveContainer width="100%" height={330}>
                <BarChart data={topDocumentsChart}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="name" />
                  <YAxis />
                  <Tooltip />
                  <Bar dataKey="value" fill="#b3072f" radius={[8, 8, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>
        </section>

        <section className="panel history-panel" id="history">
          <div className="panel-header history-header">
            <div>
              <h2>Historique des questions</h2>
              <p>Cliquez sur une question pour consulter la réponse complète.</p>
            </div>

            <div className="history-actions">
              <div className="export-dropdown">
                <button
                  onClick={() => setExportOpen((prev) => !prev)}
                  className="export-main-btn"
                >
                  Exporter
                </button>

                {exportOpen && (
                  <div className="export-menu">
                    <button
                      onClick={() =>
                        downloadSelectedFile(
                          "/api/admin/history/export/pdf/selected",
                          "historique-selection-isga.pdf"
                        )
                      }
                    >
                      PDF
                    </button>

                    <button
                      onClick={() =>
                        downloadSelectedFile(
                          "/api/admin/history/export/word/selected",
                          "historique-selection-isga.doc"
                        )
                      }
                    >
                      Word
                    </button>
                  </div>
                )}
              </div>

              <button className="outline-action" onClick={toggleSelectAllQuestions}>
                {selectedQuestions.length === history.filter((h) => h.id || h._id).length &&
                history.length > 0
                  ? "Tout désélectionner"
                  : "Tout sélectionner"}
              </button>

              <button
                className="delete-selection-btn"
                disabled={selectedQuestions.length === 0}
                onClick={handleDeleteSelectedQuestions}
              >
                Supprimer la sélection
              </button>
            </div>
          </div>

          <div className="history-scroll">
            {history.length === 0 ? (
              <p className="empty-history">Aucune question enregistrée.</p>
            ) : (
              history.map((item, index) => {
                const id = item.id || item._id;
                const checked = selectedQuestions.includes(id);
                const realStatus = getRealStatus(item);

                return (
                  <article
                    className={`question-card ${checked ? "selected" : ""}`}
                    key={id || index}
                    onClick={() => setSelectedHistoryItem(item)}
                  >
                    <label className="question-check" onClick={(e) => e.stopPropagation()}>
                      <input
                        type="checkbox"
                        checked={checked}
                        onChange={() => toggleQuestionSelection(id)}
                      />
                      <span></span>
                    </label>

                    <div className="question-content">
                      <div className="question-head">
                        <strong>{item.question}</strong>
                        <span className={`mini-status ${realStatus}`}>{realStatus}</span>
                      </div>

                      <p>
                        {item.answer?.length > 180
                          ? item.answer.substring(0, 180) + "..."
                          : item.answer}
                      </p>

                      <span className="question-date">
                        {item.createdAt ? new Date(item.createdAt).toLocaleString() : "Date non disponible"}
                      </span>
                    </div>
                  </article>
                );
              })
            )}
          </div>
        </section>
      </section>

      {selectedHistoryItem && (
        <div className="modal-backdrop" onClick={() => setSelectedHistoryItem(null)}>
          <div className="history-modal" onClick={(e) => e.stopPropagation()}>
            <button className="modal-close" onClick={() => setSelectedHistoryItem(null)}>
              ×
            </button>

            <span className="eyebrow modal-eyebrow">Détail interaction</span>
            <h2>{selectedHistoryItem.question}</h2>

            <p className="modal-answer">{selectedHistoryItem.answer}</p>

            <div className="modal-meta">
              <span>Statut : {getRealStatus(selectedHistoryItem)}</span>
              <span>Score : {selectedHistoryItem.averageScore ?? "-"}</span>
              <span>Temps : {selectedHistoryItem.responseTimeMs ?? 0} ms</span>
              <span>
                Date :{" "}
                {selectedHistoryItem.createdAt
                  ? new Date(selectedHistoryItem.createdAt).toLocaleString()
                  : "-"}
              </span>
            </div>
          </div>
        </div>
      )}
    </motion.main>    </>  );
}

export default AdminDashboard;