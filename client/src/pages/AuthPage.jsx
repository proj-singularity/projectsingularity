import { useState } from "react";
import { Input, Button } from "@nextui-org/react";
import { Eye, EyeSlash } from "@phosphor-icons/react";

import googleIcon from "/icons/google-icon.svg";
import githubIcon from "/icons/github-icon.svg";
import singularity from "/singularity.svg";

function AuthPage() {
  const [isVisible, setIsVisible] = useState(false);

  const toggleVisibility = () => setIsVisible(!isVisible);

  return (
    <div className="w-full min-h-screen h-full flex justify-center items-center">
      <div className="w-1/2 h-full min-h-screen bg-[#0D001A] flex justify-center items-center">
        <p className="text-xl">Add something here please</p>
      </div>
      <div className="w-1/2 h-full min-h-screen bg-white flex justify-center items-center ">
        <div className="w-[360px] flex justify-center items-center flex-col text-black gap-6">
          <div className="flex justify-center items-center flex-col gap-2">
            <h1 className="text-4xl font-[Satoshi-Bold]">Create an account</h1>
            <p>Already have an account? Log In</p>
          </div>
          <div className="w-full flex justify-center items-center flex-col gap-2">
            <Input
              type="email"
              variant="bordered"
              label="Enter email address"
              isClearable
              fullWidth
              classNames={{
                inputWrapper:
                  "border-1 border-accent rounded-md rounded-[12px] group-data-[focus=true]:border-alternate h-[50px]",
                label: "group-data-[focus=true]:text-alternate",
              }}
            />
            <Input
              variant="bordered"
              label="Enter password"
              isClearable
              fullWidth
              classNames={{
                inputWrapper:
                  "border-1 border-accent rounded-md rounded-[12px] group-data-[focus=true]:border-alternate h-[50px]",
                label: "group-data-[focus=true]:text-alternate",
              }}
              endContent={
                <button
                  className="focus:outline-none"
                  type="button"
                  onClick={toggleVisibility}
                >
                  {isVisible ? (
                    <EyeSlash
                      size={20}
                      color="#630099"
                      weight="duotone"
                      className="text-2xl text-default-400 pointer-events-none"
                    />
                  ) : (
                    <Eye
                      size={20}
                      color="#630099"
                      weight="duotone"
                      className="text-2xl text-default-400 pointer-events-none"
                    />
                  )}
                </button>
              }
              type={isVisible ? "text" : "password"}
            />
            <Button
              size="md"
              className="bg-primary w-full h-[50px] text-lg font-[Satoshi-Medium] mt-1"
            >
              Next
            </Button>
          </div>
          <div className="w-full relative flex justify-center">
            <div className="bg-accent w-full min-w-full h-[1px] rounded-md" />
            <div className="text-alternate absolute bg-white min-w-[60px] -top-3 text-center">
              OR
            </div>
          </div>
          <div className="w-full flex justify-center items-center flex-col gap-2">
            <Button
              size="sm"
              className="bg-white text-black border-1 border-accent w-full h-[50px] text-md font-[Satoshi-Medium] flex items-center justify-center gap-4 rounded-[12px]"
              startContent={
                <img src={googleIcon} alt="google icon" className="w-[20px]" />
              }
            >
              Sign in with Google
            </Button>
            <Button
              size="sm"
              className="bg-white text-black border-1 border-accent w-full h-[50px] text-md font-[Satoshi-Medium] flex items-center justify-center gap-4 rounded-[12px]"
              startContent={
                <img src={githubIcon} alt="github icon" className="w-[20px]" />
              }
            >
              Sign in with Github
            </Button>
          </div>
          <p className="text-sm text-center">
            By signing up, you agree to the Terms of Use and <br /> Privacy
            Policy
          </p>
        </div>
      </div>
      <img
        src={singularity}
        alt="singularity"
        className="absolute bottom-6 right-6 w-[100px]"
      />
    </div>
  );
}

export default AuthPage;
