import { logo } from "@/utils/fonts";
import { Button } from "@nextui-org/react";
import { HomeIcon } from "@heroicons/react/24/outline";
import Link from "next/link";
import UserSignUpForm from "@/components/auth/UserSignUpForm";

const AuthLayout = ({ children }: { children: React.ReactNode }) => {
  return (
    <div className="w-full min-h-screen h-full flex flex-col md:flex-row">
      <div className="w-full md:w-1/2 h-full min-h-screen bg-[#0D001A] justify-center items-center hidden md:flex">
        <p className="text-xl">Add something here please</p>
      </div>
      <div className="w-full md:w-1/2 h-full min-h-screen bg-white flex justify-center items-center p-4">
        <div className="w-full md:w-[360px] flex justify-center items-center flex-col text-black gap-6">
          {children}
        </div>
      </div>
    </div>
  );
};

export default AuthLayout;
