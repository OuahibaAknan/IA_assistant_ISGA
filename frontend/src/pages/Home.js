import React, { useState } from "react";
import { motion } from "framer-motion";
import Chatbot from "../components/Chatbot";
import "../styles/Homee.css";
import PageIntro from "../components/PageIntro";

const sectionAnimation = {
  hidden: { opacity: 0, y: 40 },
  visible: {
    opacity: 1,
    y: 0,
    transition: { duration: 0.85, ease: [0.22, 1, 0.36, 1] },
  },
};

const staggerContainer = {
  hidden: {},
  visible: {
    transition: { staggerChildren: 0.14, delayChildren: 0.12 },
  },
};

const fadeInUp = {
  hidden: { opacity: 0, y: 28 },
  visible: {
    opacity: 1,
    y: 0,
    transition: { duration: 0.72, ease: [0.22, 1, 0.36, 1] },
  },
};

function Home() {

  const [activeCampus, setActiveCampus] = useState("");
  const [currentTestimonial, setCurrentTestimonial] = useState(0);

  const campuses = [
    {
      name: "Casablanca",
      img: "/campuscasa.png",
      text: "Au cœur de la capitale économique, le campus de Casablanca offre un environnement dynamique.",
    },
    {
      name: "Rabat",
      img: "/campusrabat.png",
      text: "Situé dans la capitale administrative, le campus propose un cadre académique structuré.",
    },
    {
      name: "Marrakech",
      img: "/campusmarrakech.png",
      text: "Un environnement moderne et stimulant pour les étudiants ambitieux.",
    },
    {
      name: "Fès",
      img: "/campusfes.png",
      text: "Un cadre académique riche au cœur de la capitale intellectuelle du Maroc.",
    },
  ];

  const engineering = [
    "Ingénierie du Développement Web et Mobile",
    "Intelligence artificielle et Big Data",
    "Réseaux, Systèmes et Sécurité",
    "Systèmes industriels intelligents",
    "Génie civil",
  ];

  const management = [
    "Finance & Contrôle",
    "Marketing Digital",
    "Management des RH",
    "Ingénierie financière",
  ];

  const testimonials = [
    {
      text:
        "Réussir son entrée en Management demande de la pratique, et c'est exactement ce que l'ISGA m'a apporté. Je recommande vivement cet environnement stimulant qui transforme la théorie en une réelle force de performance.",
      name: "LAHWASNI Najwa",
      field: "Comptabilité et Finance",
    },

    {
      text:
        "Plus qu'une formation en Réseaux, mon cursus à l'ISGA a forgé mon leadership. Présider le BDE m'a appris à concrétiser mes idées et à gérer le terrain, faisant de cette école un vrai tremplin professionnel.",
      name: "SAHEL Aya",
      field: "Ingénierie des Réseaux Systèmes et Sécurité",
    },

    {
      text:
        "Ma formation en finance et gestion m’a apporté des compétences solides et une réelle autonomie. Grâce aux stages et au soutien des enseignants, j’ai pu bâtir un profil prêt pour le marché du travail. Une école que je recommande sans hésiter.",
      name: "CHAHIB Haitam",
      field: "Comptabilité et Finance",
    },
  ];

  const nextTestimonial = () => {
    setCurrentTestimonial((prev) =>
      prev === testimonials.length - 1 ? 0 : prev + 1
    );
  };

  const prevTestimonial = () => {
    setCurrentTestimonial((prev) =>
      prev === 0 ? testimonials.length - 1 : prev - 1
    );
  };

  return (
     <main className="isga-page">

    <PageIntro pageName="Page d'accueil" />
<motion.div
  className="isga-page"
  initial="hidden"
  animate="visible"
  variants={staggerContainer}
>
      {/* NAVBAR */}
      <header className="isga-navbar">

        <img
          src="/logo.png"
          alt="Logo ISGA"
          className="isga-logo"
        />

        <nav className="nav-links">
          <a href="#campus">Campus</a>
          <a href="#about">Qui sommes-nous</a>
          <a href="#formations">Formations</a>
          <a href="#temoignages">Nos témoignages</a>
        </nav>

        {/* <div className="nav-actions">
          {localStorage.getItem("adminToken") ? (
            <Link
              to="/admin/dashboard"
              className="nav-admin-btn"
            >
              Dashboard
            </Link>
          ) : (
            <Link
              to="/login"
              className="login-btn"
            >
              Se connecter
            </Link>
          )} */}
        

      </header>

      {/* HERO */}
      <motion.section
        className="hero-section"
        initial="hidden"
        animate="visible"
        variants={sectionAnimation}
        style={{
          backgroundImage: `linear-gradient(120deg, rgba(0,0,0,0.80), rgba(0,0,0,0.28)), url(${process.env.PUBLIC_URL}/imgpr.jpg)`,
        }}
      >
        <motion.div className="hero-content" variants={fadeInUp}>
          <span>Reconnue par l’État</span>

          <h1>L’école de référence en ingénierie et management</h1>

          <p>
            ISGA forme depuis plus de 40 ans des profils orientés entreprise,
            innovation et compétences métiers.
          </p>

          <a href="#campus" className="hero-btn">
            Découvrir les campus
          </a>
        </motion.div>
      </motion.section>

      {/* CAMPUS */}
      <motion.section
        className="campus-zone"
        id="campus"
        initial="hidden"
        whileInView="visible"
        viewport={{ once: true, amount: 0.25 }}
        variants={staggerContainer}
      >
        <motion.div className="campus-cards">
          {campuses.map((campus, index) => (
            <motion.div
              key={campus.name}
              className={`campus-card ${
                activeCampus === campus.name ? "active-card" : ""
              }`}
              style={{
                "--campus-bg": `url(${campus.img})`,
              }}
              onClick={() => setActiveCampus(campus.name)}
              variants={fadeInUp}
              transition={{ delay: index * 0.08 }}
              whileHover={{ y: -10, scale: 1.01 }}
              whileTap={{ scale: 0.99 }}
            >
              <div className="pin">📍</div>
              <h3>{campus.name}</h3>
              <p>{campus.text}</p>
            </motion.div>
          ))}
        </motion.div>
      </motion.section>

      {/* ABOUT */}
      <motion.section
        className="about-section"
        id="about"
        initial="hidden"
        whileInView="visible"
        viewport={{ once: true, amount: 0.25 }}
        variants={staggerContainer}
      >
        <motion.div className="about-image" variants={fadeInUp}>
          <img src="/isga.png" alt="Qui sommes-nous" className="about-image-img" />
        </motion.div>

        <motion.div className="about-text" variants={fadeInUp}>
          <span>QUI SOMMES NOUS ?</span>

          <h2>
            Depuis <strong>1981</strong>
          </h2>

          <p>
            L’ISGA est un établissement reconnu par l’État spécialisé en ingénierie
            et management avec plus de 40 ans d’expérience.
          </p>

          <ul>
            <li>Meilleur Élan Éducatif</li>
            <li>4 Palmes d’Excellence</li>
            <li>Top 10 Business Schools africaines</li>
          </ul>
        </motion.div>
      </motion.section>

      {/* FORMATIONS */}
      <motion.section
        className="formations-section"
        id="formations"
        initial="hidden"
        whileInView="visible"
        viewport={{ once: true, amount: 0.25 }}
        variants={staggerContainer}
      >
        <motion.h2 variants={fadeInUp}>Nos formations</motion.h2>

        <motion.div className="formations-grid" variants={staggerContainer}>
          <motion.div className="formation-box" variants={fadeInUp}>
            <h3>Ingénierie</h3>
            {engineering.map((item) => (
              <p key={item}>{item}</p>
            ))}
          </motion.div>

          <motion.div className="formation-box" variants={fadeInUp}>
            <h3>Management</h3>
            {management.map((item) => (
              <p key={item}>{item}</p>
            ))}
          </motion.div>
        </motion.div>
      </motion.section>



 
      {/* TÉMOIGNAGES */}
      <motion.section
        className="modern-testimonials"
        id="temoignages"
        initial="hidden"
        whileInView="visible"
        viewport={{ once: true, amount: 0.25 }}
        variants={staggerContainer}
      >
        <motion.div className="modern-testimonials-image" variants={fadeInUp}>
          <img src="/campusmarrakech.png" alt="Etudiants ISGA" />
        </motion.div>

        <motion.div className="modern-testimonials-content" variants={fadeInUp}>
          <span className="modern-badge">NOS TEMOIGNAGES</span>

          <h2>Ce que disent nos étudiants</h2>

          <div className="animated-line"></div>

          <motion.div className="testimonial-card-modern" variants={fadeInUp}>
            <div className="quote-mark">❝</div>

            <p className="modern-testimonial-text">
              {testimonials[currentTestimonial].text}
            </p>

            <div className="modern-author">
              <div className="modern-avatar">
                {testimonials[currentTestimonial].name.charAt(0)}
              </div>

              <div>
                <h3>{testimonials[currentTestimonial].name}</h3>
                <span>{testimonials[currentTestimonial].field}</span>
              </div>
            </div>
          </motion.div>

          <div className="modern-buttons">
            <button onClick={prevTestimonial}>←</button>
            <button onClick={nextTestimonial}>→</button>
          </div>
        </motion.div>
      </motion.section>
      {/* PARTENAIRES */}
      <section className="partners-section">

        <h2>
          Nos partenaires <strong>académiques</strong>
        </h2>

        <div className="partners-grid">

          <div className="partner-card">
            <img
              src="/lemans.png"
              alt="Le Mans"
              className="partner-logo"
            />
          </div>

          <div className="partner-card">
            <img
              src="/aivan.png"
              alt="Aivancity"
              className="partner-logo"
            />
          </div>

          <div className="partner-card">
            <img
              src="/insa.png"
              alt="INSA"
              className="partner-logo"
            />
          </div>

          <div className="partner-card">
            <img
              src="/excelia.png"
              alt="Excelia"
              className="partner-logo"
            />
          </div>

        </div>

      </section>

      {/* FOOTER */}
      <footer className="footer-section">

        <div>

          <h3>Ingénierie</h3>

          {engineering.map((item) => (
            <p key={item}>{item}</p>
          ))}

        </div>

        <div>

          <h3>Management</h3>

          {management.map((item) => (
            <p key={item}>{item}</p>
          ))}

        </div>

        <div>

          <h3>Liens utiles</h3>

          <p>Contact</p>
          <p>FAQ</p>
          <p>Mentions légales</p>

        </div>

      </footer>

      <Chatbot />

    </motion.div>
    </main> 

  );
}
export default Home;