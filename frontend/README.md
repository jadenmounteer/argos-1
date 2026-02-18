## ARGOS-1 Tactical Console (frontend)

This `frontend` app is the LCARS-inspired tactical console for ARGOS-1. It is a **UI sidecar** that talks to the Spring Boot kernel over HTTP/SSE but contains no backend or domain logic.

### Getting started

- Install dependencies:
  - `cd frontend`
  - `npm install`
- Run the dev server:
  - `npm run dev`

The console will start on the default Vite dev port.

### UI architecture

- **LCARS frame** – `src/components/lcars/LcarsFrame.tsx`
- **Sidebar** – `src/components/lcars/LeftSidebar.tsx`
- **Main viewport** – `src/components/lcars/MainViewport.tsx`
- **Status bar** – `src/components/lcars/StatusBar.tsx`
- **Shell** – `src/components/layout/AppShell.tsx`
- **Tactical log** – `src/components/log/TacticalLog.tsx`

Tailwind CSS is used primarily for layout, spacing, and a small set of LCARS-flavored color tokens. As the `@starfleet-technology/lcars-react` library stabilizes in this environment, its components can be layered into these structural wrappers without changing the overall layout.

### Module resolution and `lcars-react`

`@starfleet-technology/lcars-react` uses an extensionless ESM re-export (`./components/stencil-generated/components`). **Node’s ESM loader** requires explicit extensions, so importing the package in a Node script (e.g. `node -e "import ..."`) can fail with "Cannot find module …/components". **Vite** resolves that path when bundling, so `npm run dev` and `npm run build` work without changes. If you need the package to load in Node (e.g. tests or scripts), use `patch-package` to add `.js` to that re-export and run `patch-package` in `postinstall`.

# React + TypeScript + Vite

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Babel](https://babeljs.io/) (or [oxc](https://oxc.rs) when used in [rolldown-vite](https://vite.dev/guide/rolldown)) for Fast Refresh
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/) for Fast Refresh

## React Compiler

The React Compiler is not enabled on this template because of its impact on dev & build performances. To add it, see [this documentation](https://react.dev/learn/react-compiler/installation).

## Expanding the ESLint configuration

If you are developing a production application, we recommend updating the configuration to enable type-aware lint rules:

```js
export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      // Other configs...

      // Remove tseslint.configs.recommended and replace with this
      tseslint.configs.recommendedTypeChecked,
      // Alternatively, use this for stricter rules
      tseslint.configs.strictTypeChecked,
      // Optionally, add this for stylistic rules
      tseslint.configs.stylisticTypeChecked,

      // Other configs...
    ],
    languageOptions: {
      parserOptions: {
        project: ['./tsconfig.node.json', './tsconfig.app.json'],
        tsconfigRootDir: import.meta.dirname,
      },
      // other options...
    },
  },
])
```

You can also install [eslint-plugin-react-x](https://github.com/Rel1cx/eslint-react/tree/main/packages/plugins/eslint-plugin-react-x) and [eslint-plugin-react-dom](https://github.com/Rel1cx/eslint-react/tree/main/packages/plugins/eslint-plugin-react-dom) for React-specific lint rules:

```js
// eslint.config.js
import reactX from 'eslint-plugin-react-x'
import reactDom from 'eslint-plugin-react-dom'

export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      // Other configs...
      // Enable lint rules for React
      reactX.configs['recommended-typescript'],
      // Enable lint rules for React DOM
      reactDom.configs.recommended,
    ],
    languageOptions: {
      parserOptions: {
        project: ['./tsconfig.node.json', './tsconfig.app.json'],
        tsconfigRootDir: import.meta.dirname,
      },
      // other options...
    },
  },
])
```
