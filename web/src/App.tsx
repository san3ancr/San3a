import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './AuthContext';
import Home from './Home';
import Setup from './Setup';
import Detail from './Detail';
import Profile from './Profile';

function Protected({ children }: { children: React.ReactNode }) {
  const { user, profile, loading } = useAuth();
  if (loading) return <div className="page">جارٍ التحميل...</div>;
  if (!user) return <Navigate to="/login" replace />;
  if (!profile || !profile.name) return <Navigate to="/setup" replace />;
  return <>{children}</>;
}

function Login() {
  const { signInWithGoogle } = useAuth();
  return (
    <div className="page center">
      <h1>صنعة</h1>
      <p>سجّل الدخول للمتابعة</p>
      <button className="btn google" onClick={() => signInWithGoogle()}>المتابعة بحساب Google</button>
    </div>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/setup" element={<Setup />} />
          <Route path="/" element={<Protected><Home /></Protected>} />
          <Route path="/detail/:uid" element={<Protected><Detail /></Protected>} />
          <Route path="/profile" element={<Protected><Profile /></Protected>} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
