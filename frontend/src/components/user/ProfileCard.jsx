import { Chip } from "@nextui-org/react";
import { Link } from "@tanstack/react-router";
export default function ProfileCard() {
  return (
    <div className="bg-primary h-[400px] w-[300px] rounded-md border border-purple-600 flex flex-col justify-between p-4">
      <div className="flex items-center justify-between">
        <h1 className="font-[Satoshi-Bold]">@milkpacket</h1>
        <Chip color="secondary" variant="shadow">
          Admin
        </Chip>
      </div>

      <div className="flex flex-col gap-4">
        <h1 className="font-[Satoshi-Bold] text-4xl">
          Varun
          <br />
          Panyam
        </h1>
        <h3 className="font-[Satoshi]">- Ding Ding Ding!</h3>
      </div>

      <div className="flex items-center justify-between">
        <Link to="/profile" className="text-white text-md">
          Visit Profile
        </Link>
      </div>
    </div>
  );
}
