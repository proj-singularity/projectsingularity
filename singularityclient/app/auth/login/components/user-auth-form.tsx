"use client";

import * as React from "react";

import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { toast } from "sonner";
import { useAuthGuard } from "@/utils/auth/use-auth";
import { HttpErrorResponse } from "@/models/http/HttpErrorResponse";

import Link from "next/link";
import { FaGithub, FaGoogle } from "react-icons/fa";

interface UserAuthFormProps extends React.HTMLAttributes<HTMLDivElement> {}

const loginFormSchema = z.object({
  email: z.string().email(),
  password: z.string().min(8),
});

type Schema = z.infer<typeof loginFormSchema>;
export function UserAuthForm({ className, ...props }: UserAuthFormProps) {
  const [isLoading, setIsLoading] = React.useState<boolean>(false);
  const { login } = useAuthGuard({
    middleware: "guest",
    redirectIfAuthenticated: "/profile",
  });
  const [errors, setErrors] = React.useState<HttpErrorResponse | undefined>(
    undefined
  );

  async function onSubmit(data: Schema) {
    login({
      onError: (errors) => {
        setErrors(errors);
        if (errors) {
          toast.error("Authentication failed");
        }
      },
      props: data,
    });
  }

  const { register, handleSubmit, formState } = useForm<Schema>({
    resolver: zodResolver(loginFormSchema),
    reValidateMode: "onSubmit",
  });

  function getProviderLoginUrl(
    provider: "google" | "facebook" | "github" | "okta"
  ) {
    return (
      process.env.NEXT_PUBLIC_BASE_URL + `/oauth2/authorization/${provider}`
    );
  }

  return;
}
