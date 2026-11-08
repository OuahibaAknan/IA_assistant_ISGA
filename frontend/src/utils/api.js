import axios from "axios";

export const API_BASE_URL =
  process.env.REACT_APP_API_URL || "http://localhost:8080";

export const api = axios.create({
  baseURL: API_BASE_URL,
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("adminToken");

  if (token && token !== "connected") {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});