/// <reference types="vite/client" />

declare global {
  interface Window {
    __regionCodeMap?: Map<string, string>
  }
}

export {}
