import { createFileRoute } from "@tanstack/react-router";
import LogIn from "src/pages/Login";

export const Route = createFileRoute("/login")({
  component: LogIn,
});
