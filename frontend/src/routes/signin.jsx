import { createFileRoute } from "@tanstack/react-router";
import SignIn from "src/pages/SignIn";

export const Route = createFileRoute("/signin")({
  component: SignIn,
});
