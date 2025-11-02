/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_DEV_MODE: string
  readonly VITE_API_BASE_URL: string
  readonly VITE_NLP_BASE_URL: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}