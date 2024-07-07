import Axios from "axios";

const httpClient = Axios.create({
  baseURL: "http://localhost:8091/api",
  headers: {
    "X-Requested-With": "XMLHttpRequest",
    "Content-Type": "application/json",
    Accept: "application/json",
  },
  withCredentials: true,
  xsrfCookieName: "XSRF-TOKEN",
  withXSRFToken: true,
});

export default httpClient;
