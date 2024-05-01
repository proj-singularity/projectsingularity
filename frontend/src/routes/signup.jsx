import { createFileRoute } from "@tanstack/react-router";
import SignUp from "src/pages/SignUp";

export const Route = createFileRoute("/signup")({
  component: SignUp,
});
