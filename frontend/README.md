# Shortly Frontend

Production-ready React + TypeScript frontend for the URL Shortener platform.

## Stack

- React
- TypeScript
- Vite
- Tailwind CSS
- Axios
- React Router
- React Query
- Recharts

## Setup

```bash
cd frontend
npm install
npm run dev
```

## Environment

Optional:

```bash
VITE_API_BASE_URL=http://localhost:8080
```

## Pages

- Landing page
- Login
- Register
- Dashboard
- Analytics
- Admin
- 404

## Notes

- The UI falls back to mock data when the backend is unavailable.
- JWT is read from local storage and sent with requests.
- Google OAuth uses the backend `/oauth2/authorization/google` route.
