import UserSignUpForm from "@/components/auth/UserSignUpForm";
import Link from "next/link";

import React from "react";

const SignUpPage = () => {
  return (
    <>
      <div className="flex justify-center items-center flex-col gap-2">
        <h1 className="text-3xl md:text-4xl font-[Satoshi-Bold]">
          Create an account
        </h1>
        <p>
          Already have an account?{" "}
          <Link href="/login" className="text-primary underline">
            Log In
          </Link>
        </p>
      </div>
      <UserSignUpForm />
    </>
  );
};

export default SignUpPage;
