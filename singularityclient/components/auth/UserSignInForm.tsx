"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { set, useForm } from "react-hook-form";
import { z } from "zod";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormMessage,
} from "../ui/form";
import { Button, Input } from "@nextui-org/react";
import httpClient from "@/utils/httpClient";
import { toast } from "sonner";
import Link from "next/link";
import { useState } from "react";
import { HttpErrorResponse } from "@/models/http/HttpErrorResponse";
import { useAuthGuard } from "@/utils/auth/use-auth";

const formSchema = z.object({
  email: z.string().email({ message: "Invalid email" }),
  password: z
    .string()
    .min(8, { message: "Password must be at least 8 characters" }),
});

const UserSignInForm = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [errors, setErrors] = useState<HttpErrorResponse | undefined>(
    undefined
  );
  const { login } = useAuthGuard({
    middleware: "guest",
    redirectIfAuthenticated: "/",
  });

  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      email: "",
      password: "",
    },
  });

  async function onSubmit(values: z.infer<typeof formSchema>) {
    const res = await httpClient.post<HttpErrorResponse>(
      "http://localhost:8091/api/auth/login",
      values
    );
    console.log(res);
  }
  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4 w-full">
        <FormField
          control={form.control}
          name="email"
          render={({ field }) => (
            <FormItem>
              <FormControl>
                <Input
                  type="email"
                  variant="bordered"
                  label="Email Address"
                  {...field}
                  classNames={{
                    inputWrapper:
                      "border-1 border-accent text-black rounded-md rounded-[12px] group-data-[focus=true]:border-alternate",
                    label:
                      "group-data-[focus=true]:text-alternate group-data-[filled=true]:text-alternate text-alternate",
                  }}
                  isRequired
                  isClearable
                />
              </FormControl>

              <FormMessage />
            </FormItem>
          )}
        />
        <FormField
          control={form.control}
          name="password"
          render={({ field }) => (
            <FormItem>
              <FormControl>
                <Input
                  type="password"
                  variant="bordered"
                  label="Password"
                  {...field}
                  classNames={{
                    inputWrapper:
                      "border-1 border-accent text-black rounded-md rounded-[12px] group-data-[focus=true]:border-alternate",
                    label:
                      "group-data-[focus=true]:text-alternate group-data-[filled=true]:text-alternate text-alternate",
                  }}
                  isRequired
                  isClearable
                />
              </FormControl>

              <FormMessage />
            </FormItem>
          )}
        />

        <Button
          type="submit"
          className="bg-primary w-full h-[50px] text-md text-white"
        >
          Log In
        </Button>
      </form>
    </Form>
  );
};

export default UserSignInForm;
