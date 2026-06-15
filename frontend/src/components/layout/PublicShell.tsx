import { Outlet } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';

export function PublicShell() {
  return (
    <div className="min-h-screen bg-[radial-gradient(circle_at_top_left,_rgba(100,112,255,0.12),_transparent_30%),linear-gradient(to_bottom,_#ffffff,_#f8fafc)] text-slate-900">
      <Toaster position="top-right" />
      <Outlet />
    </div>
  );
}
