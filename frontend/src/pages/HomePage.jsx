import { Link } from "@tanstack/react-router";

function HomePage() {
  return (
    <div>
      HomePage
      <br />
      <Link to="/auth">
        <button className="bg-white text-black cursor-pointer w-fit text-center m-20 p-6 rounded-lg">
          Go to Auth Page
        </button>
      </Link>
    </div>
  );
}

export default HomePage;
