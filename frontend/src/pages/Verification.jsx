import { useEffect, useState } from "react";
import { Spinner } from "@nextui-org/react";

import { Route } from "src/routes/verification";
import { useNavigate } from "@tanstack/react-router";

export default function Verification() {
  const { token } = Route.useSearch();
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState(
    "Hang tight! We'll redirect you once you have been verified"
  );
  const navigate = useNavigate();

  useEffect(() => {
    const verifyToken = async () => {
      try {
        await new Promise((resolve) => setTimeout(resolve, 3000));
        const response = await fetch(
          `http://localhost:8091/api/auth/verify?token=${token}`
        );
        const data = await response.json();

        if (!response.ok) {
          setMessage(data.message);
          await new Promise((resolve) => setTimeout(resolve, 3000));
          navigate({ to: "/signup" });
        }

        setMessage(data.message);

        await new Promise((resolve) => setTimeout(resolve, 3000));
        navigate({ to: "/login" });
      } catch (error) {
        setMessage(error.message);
        await new Promise((resolve) => setTimeout(resolve, 3000));
        navigate({ to: "/signup" });
      } finally {
        setLoading(false);
      }
    };

    verifyToken();
  });

  return (
    <div className="flex items-center justify-center h-screen w-screen bg-[#0D001A]">
      <div className="flex items-center gap-4">
        {loading && <Spinner size="md" />}

        <h1 className="text-2xl">{message}</h1>
      </div>
    </div>
  );
}
