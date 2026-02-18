import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import '@starfleet-technology/lcars/dist/lcars/lcars.esm.js'
import './index.css'
import App from './App.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
