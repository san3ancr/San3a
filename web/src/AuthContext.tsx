import { createContext, useContext, useEffect, useState } from 'react';
import { onAuthStateChanged, signInWithPopup, GoogleAuthProvider, signOut as fbSignOut, type User } from 'firebase/auth';
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
    try {
      const snap = await getDoc(doc(db, 'users', u.uid));
      if (snap.exists()) {
        setProfile(snap.data() as UserProfile);
      } else {
        setProfile(null);
      }
    } catch (e: any) {
      console.error('loadProfile error', e);
      // Do not block auth; allow setup flow
      setProfile(null);
    }
  }

  useEffect(() => {
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
      const result = await signInWithPopup(auth, provider);
      const u = result.user;
      let snap;
      try {
        snap = await getDoc(doc(db, 'users', u.uid));
      } catch (e) {
        console.error('getDoc error', e);
        snap = null;
      }
      if (!snap || !snap.exists()) {
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
      } else {
        setProfile(snap.data() as UserProfile);
      }
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
