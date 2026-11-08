import React, { useEffect, useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import "../styles/PageIntro.css";

function PageIntro({ pageName = "Page", onFinish }) {
  const [showIntro, setShowIntro] = useState(true);

  useEffect(() => {
    const timer = setTimeout(() => {
      setShowIntro(false);
      if (onFinish) {
        onFinish();
      }
    }, 3000);

    return () => clearTimeout(timer);
  }, [onFinish]);

  return (
    <AnimatePresence>
      {showIntro && (
        <motion.div
          className="page-intro"
          initial={{ opacity: 1 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0, scale: 1.02 }}
          transition={{ duration: 0.55, ease: "easeInOut" }}
        >
          <motion.div
            className="intro-container"
            initial={{ opacity: 0, y: 30, scale: 0.94 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: -20, scale: 0.96 }}
            transition={{ duration: 0.7, ease: "easeOut" }}
          >
            <div className="intro-logo-wrapper">
              <img src="/logo.png" alt="ISGA" className="intro-logo" />
            </div>

            <h1>
              ISGA<span>|</span>{pageName}
            </h1>

            <p>Institut Supérieur d’Ingénierie et des Affaires</p>

            <div className="intro-loading-line">
              <motion.div
                className="intro-loading-progress"
                initial={{ width: "0%" }}
                animate={{ width: "100%" }}
                transition={{ duration: 1.45, ease: "easeInOut" }}
              />
            </div>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}

export default PageIntro;