import { createVercelConfig } from './deployment/vercelConfig.js'

// Vercel evaluates this file at deployment time, so BACKEND_ORIGIN stays out of the browser bundle.
export const config = createVercelConfig(process.env)
