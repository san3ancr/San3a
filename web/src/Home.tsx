import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { collection, query, where, onSnapshot } from 'firebase/firestore';
import { db } from './firebase';
import type { UserProfile } from './types';
import { CRAFTS, REGIONS } from './types';

export default function Home() {
  const navigate = useNavigate();
  const [items, setItems] = useState<UserProfile[]>([]);
  const [loading, setLoading] = useState(true);
  const [q, setQ] = useState('');
  const [craft, setCraft] = useState<string | null>(null);
  const [region, setRegion] = useState<string | null>(null);

  useEffect(() => {
    const qy = query(collection(db, 'users'), where('role', '==', 'craftsman'));
    const unsub = onSnapshot(qy, (snap) => {
      setItems(snap.docs.map((d) => d.data() as UserProfile));
      setLoading(false);
    });
    return () => unsub();
  }, []);

  const filtered = items.filter((p) => {
    if (craft && p.craft !== craft) return false;
    if (region && p.region !== region) return false;
    if (q) {
      const needle = q.toLowerCase();
      if (![p.name, p.craft, p.region].some((v) => v.toLowerCase().includes(needle))) return false;
    }
    return true;
  }).sort((a, b) => {
    const ra = a.ratingCount ? a.ratingSum / a.ratingCount : 0;
    const rb = b.ratingCount ? b.ratingSum / b.ratingCount : 0;
    return (b.available ? 1 : 0) - (a.available ? 1 : 0) || rb - ra || a.name.localeCompare(b.name);
  });

  return (
    <div className="page">
      <header className="top">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h1>صنعة</h1>
          <button className="link" onClick={() => navigate('/profile')}>حسابي</button>
        </div>
        <p>ابحث عن حرفي وتواصل مباشرة</p>
      </header>
      <div className="filters">
        <input value={q} onChange={(e) => setQ(e.target.value)} placeholder="ابحث بالاسم/الحرفة/المنطقة" />
        <select value={craft || ''} onChange={(e) => setCraft(e.target.value || null)}>
          <option value="">كل الحرف</option>
          {CRAFTS.map((c) => <option key={c} value={c}>{c}</option>)}
        </select>
        <select value={region || ''} onChange={(e) => setRegion(e.target.value || null)}>
          <option value="">كل المناطق</option>
          {REGIONS.map((r) => <option key={r} value={r}>{r}</option>)}
        </select>
      </div>
      {loading ? <p>جارٍ التحميل...</p> : (
        <div className="grid">
          {filtered.map((p) => (
            <div key={p.uid} className="card" onClick={() => navigate(`/detail/${p.uid}`)} style={{ cursor: 'pointer' }}>
              <div className="avatar">{p.name.trim().slice(0, 1)}</div>
              <div className="info">
                <h3>{p.name}</h3>
                <p>{p.craft} • {p.region}</p>
                <p className="rating">⭐ {p.ratingCount ? (p.ratingSum / p.ratingCount).toFixed(1) : 'لا تقييمات'} ({p.ratingCount})</p>
                <span className={p.available ? 'badge ok' : 'badge busy'}>{p.available ? 'متاح' : 'مشغول'}</span>
              </div>
              <div className="actions">
                <a href={`tel:${p.phone}`} className="btn call">اتصال</a>
                <a href={`https://wa.me/${p.whatsapp || p.phone}`.replace(/[^0-9]/g, '')} target="_blank" rel="noreferrer" className="btn wa">واتساب</a>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}


