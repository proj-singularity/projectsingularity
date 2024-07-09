"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
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

const formSchema = z.object({
  email: z.string().email({ message: "Invalid email" }),
  password: z
    .string()
    .min(8, { message: "Password must be at least 8 characters" }),
  firstName: z.string().min(2, { message: "First name is required" }),
  lastName: z.string().min(2, { message: "Last name is required" }),
});

const UserSignUpForm = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [apiResponse, setApiResponse] = useState<HttpErrorResponse | null>(
    null
  );

  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      email: "",
      password: "",
      firstName: "",
      lastName: "",
    },
  });

  async function onSubmit(values: z.infer<typeof formSchema>) {
    setIsLoading(true);
    setApiResponse(null);

    try {
      const res = await httpClient.post<HttpErrorResponse>(
        "http://localhost:8091/api/user/register",
        values
      );

      setApiResponse(res.data);

      if (res.data.success) {
        toast.success(
          res.data.message ||
            "Account created successfully. Please check your email for a verification link.",
          {
            style: {
              background: "#4caf50",
              color: "#fff",
            },
          }
        );
      } else {
        toast.error(
          res.data.message || "An error occurred during registration."
        );
      }
    } catch (error: any) {
      if (error.response && error.response.data) {
        setApiResponse(error.response.data);
        toast.error(
          error.response.data.message ||
            "An error occurred during registration."
        );
      } else {
        setApiResponse({
          message: "An unexpected error occurred. Please try again.",
          status: 500,
          success: false,
        });
        toast.error("An unexpected error occurred. Please try again.");
      }
    } finally {
      setIsLoading(false);
    }
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
        <FormField
          control={form.control}
          name="firstName"
          render={({ field }) => (
            <FormItem>
              <FormControl>
                <Input
                  variant="bordered"
                  label="First Name"
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
          name="lastName"
          render={({ field }) => (
            <FormItem>
              <FormControl>
                <Input
                  variant="bordered"
                  label="Last Name"
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
          isLoading={isLoading}
        >
          Sign up
        </Button>
      </form>
      <p className="text-sm text-center">
        By signing up, you agree to the{" "}
        <Link href="" className="text-primary underline">
          Terms of Use
        </Link>{" "}
        and <br />{" "}
        <Link href="" className="text-primary underline">
          Privacy Policy
        </Link>
      </p>
    </Form>
  );
};

export default UserSignUpForm;
