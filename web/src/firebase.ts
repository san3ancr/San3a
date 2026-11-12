import { initializeApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';
import { getFirestore } from 'firebase/firestore';

const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY || 'AIzaSyBu-LPoVV8I_wcC3GYMqXWfV2w8jL73QB4',
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN || 'san3a-98fdb.firebaseapp.com',
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID || 'san3a-98fdb',
  storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET || 'san3a-98fdb.firebasestorage.app',
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID || '673930513703',
  appId: import.meta.env.VITE_FIREBASE_APP_ID || '',
};

export const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const db = getFirestore(app);
