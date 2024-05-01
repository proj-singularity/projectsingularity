import { createFileRoute } from "@tanstack/react-router";
import Verification from "src/pages/Verification";

export const Route = createFileRoute("/verification")({
  validateSearch: (search) => {
    return {
      token: search.token,
    };
  },
  component: Verification,
});
