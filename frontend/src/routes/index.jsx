import { createFileRoute } from "@tanstack/react-router";
import HomePage from "src/pages/HomePage";

export const Route = createFileRoute("/")({
  component: HomePage,
});
