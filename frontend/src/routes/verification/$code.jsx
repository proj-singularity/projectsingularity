import { createFileRoute } from '@tanstack/react-router'

export const Route = createFileRoute('/verification/$code')({
  component: () => <div>Hello /verification/$code!</div>
})