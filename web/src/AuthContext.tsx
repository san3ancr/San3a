import { createContext, useContext, useEffect, useState } from 'react';
import { onAuthStateChanged, signInWithRedirect, getRedirectResult, GoogleAuthProvider, signOut as fbSignOut, type User } from 'firebase/auth';
import { doc, getDoc, setDoc } from 'firebase/firestore';
import { auth, db } from './firebase';
import type { UserProfile } from './types';

interface AuthContextValue {
  user: User | null;
  profile: UserProfile | null;
  loading: boolean;
  authError: string | null;
  signInWithGoogle: () => Promise<void>;
  signOut: () => Promise<void>;
  refreshProfile: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [authError, setAuthError] = useState<string | null>(null);

  async function loadProfile(u: User) {
    const snap = await getDoc(doc(db, 'users', u.uid));
    if (snap.exists()) {
      setProfile(snap.data() as UserProfile);
    } else {
      setProfile(null);
    }
  }

  useEffect(() => {
    (async () => {
      try {
        console.log('getRedirectResult start');
        const result = await getRedirectResult(auth);
        console.log('getRedirectResult result', result?.user?.uid);
        if (result?.user) {
          const u = result.user;
          const snap = await getDoc(doc(db, 'users', u.uid));
          if (!snap.exists()) {
            const newProfile: UserProfile = {
              uid: u.uid,
              name: u.displayName || '',
              phone: u.phoneNumber || '',
              whatsapp: '',
              role: 'customer',
              craft: '',
              region: '',
              available: true,
              createdAt: Date.now(),
              ratingSum: 0,
              ratingCount: 0,
              latitude: 0,
              longitude: 0,
            };
            await setDoc(doc(db, 'users', u.uid), newProfile);
            setProfile(newProfile);
          }
        }
      } catch (e: any) {
        console.error(e);
        const msg = e?.message || 'تعذّر تسجيل الدخول';
        setAuthError(msg);
        try { window.alert(msg); } catch {}
      }
    })();

    const unsub = onAuthStateChanged(auth, async (u) => {
      setUser(u);
      if (u) await loadProfile(u);
      else setProfile(null);
      setLoading(false);
    });
    return () => unsub();
  }, []);

  async function signInWithGoogle() {
    setAuthError(null);
    try {
      const provider = new GoogleAuthProvider();
      await signInWithRedirect(auth, provider);
    } catch (e: any) {
      console.error('signInWithGoogle error', e);
      const msg = e?.message || 'تعذّر تسجيل الدخول';
      setAuthError(msg);
      try { window.alert(msg); } catch {}
    }
  }

  async function signOut() {
    await fbSignOut(auth);
    setProfile(null);
  }

  async function refreshProfile() {
    if (user) await loadProfile(user);
  }

  return (
    <AuthContext.Provider value={{ user, profile, loading, authError, signInWithGoogle, signOut, refreshProfile }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth outside provider');
  return ctx;
}
