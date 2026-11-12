import { useAuth } from './AuthContext';
import { doc, updateDoc } from 'firebase/firestore';
import { db } from './firebase';

export default function Profile() {
  const { profile, signOut, refreshProfile } = useAuth();

  async function toggleAvailability() {
    if (!profile) return;
    await updateDoc(doc(db, 'users', profile.uid), { available: !profile.available });
    await refreshProfile();
  }

  if (!profile) return <div className="page">جارٍ التحميل...</div>;

  return (
    <div className="page">
      <h1>حسابي</h1>
      <div className="card profile">
        <div className="avatar lg">{profile.name.trim().slice(0, 1)}</div>
        <h2>{profile.name}</h2>
        <p>{profile.role === 'craftsman' ? 'حساب حرفي' : 'حساب زبون'}</p>
        {profile.role === 'craftsman' && (
          <>
            <p>الحرفة: {profile.craft}</p>
            <p>المنطقة: {profile.region}</p>
            <p>⭐ {profile.ratingCount ? (profile.ratingSum / profile.ratingCount).toFixed(1) : 'لا تقييمات'} ({profile.ratingCount})</p>
            <button className="btn" onClick={toggleAvailability}>
              {profile.available ? 'متاح (اضغط لإيقاف)' : 'مشغول (اضغط لتفعيل)'}
            </button>
          </>
        )}
        <p>الهاتف: {profile.phone}</p>
        {profile.whatsapp && <p>واتساب: {profile.whatsapp}</p>}
      </div>
      <button className="btn danger" onClick={() => signOut()}>تسجيل الخروج</button>
    </div>
  );
}
