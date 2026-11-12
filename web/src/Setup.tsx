import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from './AuthContext';
import { doc, setDoc } from 'firebase/firestore';
import { db } from './firebase';
import { CRAFTS, REGIONS } from './types';
import type { UserProfile, UserRole } from './types';

export default function Setup() {
  const { user, profile, refreshProfile } = useAuth();
  const navigate = useNavigate();
  const [role, setRole] = useState<UserRole>(profile?.role || 'customer');
  const [name, setName] = useState(profile?.name || '');
  const [phone, setPhone] = useState(profile?.phone || '');
  const [whatsapp, setWhatsapp] = useState(profile?.whatsapp || '');
  const [craft, setCraft] = useState(profile?.craft || '');
  const [region, setRegion] = useState(profile?.region || '');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function save() {
    if (!user) return;
    if (!name.trim()) { setError('أدخل الاسم'); return; }
    const authPhone = user.phoneNumber || '';
    const finalPhone = authPhone || phone;
    if (!finalPhone.trim()) { setError('أدخل رقم الجوال'); return; }
    if (role === 'craftsman' && !craft) { setError('اختر الحرفة'); return; }
    if (role === 'craftsman' && !region) { setError('اختر منطقة العمل'); return; }
    setSaving(true); setError(null);
    const newProfile: UserProfile = {
      uid: user.uid,
      name: name.trim(),
      phone: finalPhone.trim(),
      whatsapp: (whatsapp || finalPhone).trim(),
      role,
      craft: role === 'craftsman' ? craft : '',
      region: role === 'craftsman' ? region : '',
      available: true,
      createdAt: profile?.createdAt || Date.now(),
      ratingSum: profile?.ratingSum || 0,
      ratingCount: profile?.ratingCount || 0,
      latitude: profile?.latitude || 0,
      longitude: profile?.longitude || 0,
    };
    try {
      await setDoc(doc(db, 'users', user.uid), newProfile);
      await refreshProfile();
      navigate('/');
    } catch (e: any) {
      setError(e?.message || 'تعذّر الحفظ');
    } finally {
      setSaving(false);
    }
  }

  const showPhone = !user?.phoneNumber;

  return (
    <div className="page">
      <h1>أكمل ملفك</h1>
      <div className="form">
        <div className="role-row">
          <button className={role === 'craftsman' ? 'role active' : 'role'} onClick={() => setRole('craftsman')}>حرفي / مقدّم خدمة</button>
          <button className={role === 'customer' ? 'role active' : 'role'} onClick={() => setRole('customer')}>زبون</button>
        </div>
        {showPhone && (
          <input value={phone} onChange={(e) => setPhone(e.target.value)} placeholder="رقم الجوال" />
        )}
        <input value={name} onChange={(e) => setName(e.target.value)} placeholder="الاسم" />
        <input value={whatsapp} onChange={(e) => setWhatsapp(e.target.value)} placeholder="رقم الواتساب (اختياري)" />
        {role === 'craftsman' && (
          <>
            <select value={craft} onChange={(e) => setCraft(e.target.value)}>
              <option value="">اختر الحرفة</option>
              {CRAFTS.map((c) => <option key={c} value={c}>{c}</option>)}
            </select>
            <select value={region} onChange={(e) => setRegion(e.target.value)}>
              <option value="">اختر منطقة العمل</option>
              {REGIONS.map((r) => <option key={r} value={r}>{r}</option>)}
            </select>
          </>
        )}
        {error && <p className="error">{error}</p>}
        <button className="btn primary" disabled={saving} onClick={save}>{saving ? 'جارٍ الحفظ...' : 'حفظ ومتابعة'}</button>
      </div>
    </div>
  );
}
