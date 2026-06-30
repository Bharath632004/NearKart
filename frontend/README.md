# NearKart Frontend

React.js web application for the NearKart hyperlocal delivery platform.

## Tech Stack
- React 18
- Redux Toolkit (state management)
- React Router v6 (routing)
- Axios (API calls)
- Material UI v5 (UI components)

## Getting Started

```bash
# Install dependencies
npm install

# Start development server
npm start

# Build for production
npm run build
```

## Environment Variables

Create a `.env` file in the `frontend/` root:
```
REACT_APP_API_BASE_URL=http://localhost:8081/api
```

## Folder Structure

```
frontend/
├── public/
├── src/
│   ├── api/          ← Axios API service files
│   ├── components/   ← Reusable UI components
│   ├── pages/        ← Page-level components
│   ├── redux/        ← Redux store, slices
│   ├── utils/        ← Helper functions
│   └── App.jsx
└── package.json
```
