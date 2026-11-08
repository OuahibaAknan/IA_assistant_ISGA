import React, { useEffect, useRef, useState } from "react";
import { motion } from "framer-motion";
import { api } from "../utils/api";
import "../styles/Chatbot.css";

const chatVariants = {
  hidden: { opacity: 0, y: 22, scale: 0.96 },
  visible: {
    opacity: 1,
    y: 0,
    scale: 1,
    transition: { duration: 0.35, ease: "easeOut" },
  },
};

const toggleVariants = {
  hover: { scale: 1.06, y: -2 },
  tap: { scale: 0.96 },
};

function Chatbot() {

  const [isOpen, setIsOpen] = useState(false);

  const [messages, setMessages] = useState([
    {
      sender: "bot",
      text:
        "Bonjour et bienvenue à l’ISGA. Je suis votre assistant virtuel. Comment puis-je vous aider aujourd’hui ?",
      sources: [],
      streaming: false,
    },
  ]);

  const [input, setInput] = useState("");

  const [loading, setLoading] = useState(false);

  const chatboxRef = useRef(null);

  const headerRef = useRef(null);

  const messagesEndRef = useRef(null);

  /*
   * Auto scroll
   */
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({
      behavior: "smooth",
    });
  }, [messages]);

  /*
   * Drag chatbot
   */
  useEffect(() => {

    if (!isOpen) return;

    const box = chatboxRef.current;
    const header = headerRef.current;

    if (!box || !header) return;

    let isDragging = false;

    let offsetX = 0;
    let offsetY = 0;

    const onMouseDown = (e) => {

      if (e.target.closest("button")) return;

      isDragging = true;

      offsetX = e.clientX - box.offsetLeft;
      offsetY = e.clientY - box.offsetTop;

      header.style.cursor = "grabbing";
    };

    const onMouseMove = (e) => {

      if (!isDragging) return;

      box.style.left = `${e.clientX - offsetX}px`;
      box.style.top = `${e.clientY - offsetY}px`;

      box.style.right = "auto";
      box.style.bottom = "auto";
    };

    const onMouseUp = () => {
      isDragging = false;
      header.style.cursor = "grab";
    };

    header.addEventListener("mousedown", onMouseDown);

    document.addEventListener("mousemove", onMouseMove);

    document.addEventListener("mouseup", onMouseUp);

    return () => {

      header.removeEventListener(
        "mousedown",
        onMouseDown
      );

      document.removeEventListener(
        "mousemove",
        onMouseMove
      );

      document.removeEventListener(
        "mouseup",
        onMouseUp
      );
    };

  }, [isOpen]);

  /*
   * Animation streaming type ChatGPT
   */
  const streamBotMessage = async (
    fullText,
    sources = []
  ) => {

    const messageId = Date.now();

    setMessages((prev) => [
      ...prev,
      {
        id: messageId,
        sender: "bot",
        text: "",
        sources,
        streaming: true,
      },
    ]);

    let currentText = "";

    const words = fullText.split(" ");
    const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

    for (let i = 0; i < words.length; i++) {

      currentText +=
        (i === 0 ? "" : " ")
        + words[i];

      // eslint-disable-next-line no-loop-func
      setMessages((prev) =>
        prev.map((msg) =>
          msg.id === messageId
            ? {
                ...msg,
                text: currentText,
              }
            : msg
        )
      );

      await delay(18);
    }

    setMessages((prev) =>
      prev.map((msg) =>
        msg.id === messageId
          ? {
              ...msg,
              streaming: false,
            }
          : msg
      )
    );
  };

  /*
   * Envoi message
   */
  const sendMessage = async () => {

    if (!input.trim() || loading) return;

    const userMessage = input.trim();

    const userMsgObject = {
      sender: "user",
      text: userMessage,
      sources: [],
    };

    const updatedMessages = [
      ...messages,
      userMsgObject,
    ];

    setMessages(updatedMessages);

    setInput("");

    setLoading(true);

    try {

      /*
       * Historique conversationnel
       */
      const conversationHistory =
        updatedMessages

          .slice(-6)

          .map((msg) => ({
            role:
              msg.sender === "bot"
                ? "bot"
                : "user",

            content: msg.text,
          }));

      /*
       * Appel backend
       */
      const response =
        await api.post("/api/rag/ask", {
          question: userMessage,
          history: conversationHistory,
        });

      /*
       * Réponse streaming
       */
      await streamBotMessage(
        response.data.answer ||
          "Je n'ai pas trouvé de réponse précise.",

        response.data.sources || []
      );

    } catch (error) {

      console.error(
        "Erreur chatbot :",
        error
      );

      await streamBotMessage(
        "Une erreur serveur est survenue. Veuillez vérifier que le backend est bien démarré."
      );

    } finally {

      setLoading(false);
    }
  };

  /*
   * Reset conversation
   */
  const clearChat = () => {

    setMessages([
      {
        sender: "bot",
        text:
          "Conversation réinitialisée. Comment puis-je vous aider ?",
        sources: [],
      },
    ]);
  };

  return (
    <>

      <motion.button
        className="chat-toggle"
        onClick={() => setIsOpen(!isOpen)}
        variants={toggleVariants}
        whileHover="hover"
        whileTap="tap"
        aria-label={isOpen ? "Fermer le chat" : "Ouvrir le chat"}
      >
        {isOpen ? (
          "×"
        ) : (
          <img
            src="/isga.png"
            alt="ISGA"
            className="toggle-logo"
          />
        )}
      </motion.button>

      {isOpen && (
        <motion.div
          className="chatbox"
          ref={chatboxRef}
          variants={chatVariants}
          initial="hidden"
          animate="visible"
        >

          <div
            className="chat-header"
            ref={headerRef}
          >

            <div className="chat-header-left">

              <img
                src="/isga.png"
                alt="Logo ISGA"
                className="header-logo"
              />

              <div>
                <h3>Assistant ISGA</h3>
                <p>
                  Intelligence documentaire
                </p>
              </div>

            </div>

            <div className="chat-actions">

              <button
                onClick={clearChat}
                title="Réinitialiser"
              >
                ↺
              </button>

              <button
                onClick={() =>
                  setIsOpen(false)
                }
                title="Fermer"
              >
                ×
              </button>

            </div>

          </div>

          <div className="chat-messages">

            <div className="chat-watermark">
              <img
                src="/isga.png"
                alt="ISGA"
              />
            </div>

            {messages.map((msg, index) => (

              <div
                key={msg.id || index}
                className={`message-row ${msg.sender}-row`}
              >

                {msg.sender === "bot" && (

                  <img
                    src="/isga.png"
                    alt="ISGA"
                    className="bot-avatar"
                  />

                )}

                <div
                  className={`message-bubble ${msg.sender}`}
                >

                  <div className="message-text">
                    {msg.text}
                  </div>

                  {msg.streaming && (
                    <span className="stream-cursor">
                      ▋
                    </span>
                  )}

                  {msg.sender === "bot" &&
                    msg.sources &&
                    msg.sources.length > 0 && (

                    <div className="rag-sources">

                      <div className="rag-title">
                        Sources documentaires
                      </div>

                      {msg.sources.map(
                        (source, i) => (

                        <div
                          key={i}
                          className="rag-source-card"
                        >

                          <div className="rag-source-top">

                            <strong>
                              {source.sourceName ||
                                "Document"}
                            </strong>

                            <span>
                              {(source.hybridScore ??
                                source.score ??
                                0
                              ).toFixed(3)}
                            </span>

                          </div>

                          <p>
                            {source.preview}
                          </p>

                        </div>
                      ))}
                    </div>
                  )}

                </div>

                {msg.sender === "user" && (
                  <div className="user-avatar">
                    U
                  </div>
                )}

              </div>
            ))}

            {loading && (

              <div className="message-row bot-row">

                <img
                  src="/isga.png"
                  alt="ISGA"
                  className="bot-avatar"
                />

                <div className="message-bubble bot typing">

                  <span></span>
                  <span></span>
                  <span></span>

                </div>

              </div>
            )}

            <div ref={messagesEndRef}></div>

          </div>

          <div className="quick-questions">

            <button
              onClick={() =>
                setInput(
                  "Quelles sont les formations disponibles ?"
                )
              }
            >
              Formations
            </button>

            <button
              onClick={() =>
                setInput(
                  "Quels sont les frais d’inscription ?"
                )
              }
            >
              Frais
            </button>

            <button
              onClick={() =>
                setInput(
                  "Quels documents sont nécessaires pour l’admission ?"
                )
              }
            >
              Admission
            </button>

          </div>

          <div className="chat-input">

            <input
              value={input}

              onChange={(e) =>
                setInput(e.target.value)
              }

              onKeyDown={(e) => {
                if (e.key === "Enter") {
                  sendMessage();
                }
              }}

              placeholder="Posez votre question sur l’ISGA..."
            />

            <button
              onClick={sendMessage}
              disabled={loading}
            >
              ➤
            </button>

          </div>

        </motion.div>
      )}
    </>
  );
}

export default Chatbot;