import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "tailwindcss";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  css: {
    postcss: {
      plugins: [tailwindcss()],
    },
  },
  resolve: {
    alias: {
      src: "/src",
      components: "/src/components",
      pages: "/src/pages",
      assets: "/src/assets",
      utils: "/src/utils",
    },
  },
});
