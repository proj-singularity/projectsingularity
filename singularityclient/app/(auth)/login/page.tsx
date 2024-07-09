import UserSignInForm from "@/components/auth/UserSignInForm";
import Link from "next/link";
import React from "react";

const LoginPage = () => {
  return (
    <>
      <div className="flex justify-center items-center flex-col gap-2">
        <h1 className="text-3xl md:text-4xl font-[Satoshi-Bold]">Log In</h1>
        <p>
          Don't have an account?{" "}
          <Link href="/signup" className="text-primary underline">
            Sign Up
          </Link>
        </p>
      </div>
      <UserSignInForm />
    </>
  );
};

export default LoginPage;
