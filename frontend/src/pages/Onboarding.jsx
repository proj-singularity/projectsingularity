import { useForm } from "@tanstack/react-form";
import React from "react";
import ProfileCard from "src/components/user/ProfileCard";

export default function Onboarding() {
  const handleOnboarding = async (values) => {
    // await mutation.mutateAsync(values);
  };

  const form = useForm({
    initialValues: {
      username: "",
      bio: "",
    },

    onSubmit: handleOnboarding,
  });

  return (
    <div className="h-screen w-screen flex items-center justify-center bg-[#0D001A]">
      <div className="flex-1 h-full flex flex-col items-center justify-center">
        <div className="flex flex-col items-start gap-1">
          <h1 className="text-3xl font-[Satoshi-Bold]">
            Welcome, Varun Panyam!
          </h1>
          <p className="text-2xl">
            Let&apos;s get started by setting up your profile.
          </p>
        </div>
      </div>
      <div className="flex-1 h-full flex items-center justify-center flex-col gap-10">
        <h1 className="text-2xl font-[Satoshi-Medium]">
          Here&apos;s what your profile card looks like!
        </h1>
        <ProfileCard />
      </div>
    </div>
  );
}
