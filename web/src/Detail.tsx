import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { doc, collection, query, orderBy, onSnapshot, addDoc, updateDoc } from 'firebase/firestore';
import { db } from './firebase';
import { useAuth } from './AuthContext';
import type { UserProfile, Review } from './types';

export default function Detail() {
  const { uid } = useParams();
  const navigate = useNavigate();
  const { user, profile: me } = useAuth();
  const [p, setP] = useState<UserProfile | null>(null);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [stars, setStars] = useState(5);
  const [comment, setComment] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!uid) return;
    const unsubP = onSnapshot(doc(db, 'users', uid), (snap) => {
      if (snap.exists()) setP(snap.data() as UserProfile);
    });
    const q = query(collection(db, 'users', uid, 'reviews'), orderBy('createdAt', 'desc'));
    const unsubR = onSnapshot(q, (snap) => {
      setReviews(snap.docs.map((d) => d.data() as Review));
    });
    return () => { unsubP(); unsubR(); };
  }, [uid]);

  async function submit() {
    if (!user || !uid || user.uid === uid) return;
    setSubmitting(true);
    try {
      await addDoc(collection(db, 'users', uid, 'reviews'), {
        userId: user.uid,
        userName: me?.name || 'مستخدم',
        rating: stars,
        comment: comment.trim(),
        createdAt: Date.now(),
      });
      // update aggregate (client-side; for production use transaction/Cloud Function)
      const sum = reviews.reduce((a, r) => a + r.rating, 0) + stars;
      const count = reviews.length + 1;
      await updateDoc(doc(db, 'users', uid), {
        ratingSum: sum,
        ratingCount: count,
      });
      setComment('');
      setStars(5);
    } finally {
      setSubmitting(false);
    }
  }

  if (!p) return <div className="page">جارٍ التحميل...</div>;

  return (
    <div className="page">
      <button className="link" onClick={() => navigate(-1)}>← رجوع</button>
      <div className="card profile">
        <div className="avatar lg">{p.name.trim().slice(0, 1)}</div>
        <h2>{p.name}</h2>
        <p>{p.craft} • {p.region}</p>
        <p>⭐ {p.ratingCount ? (p.ratingSum / p.ratingCount).toFixed(1) : 'لا تقييمات'} ({p.ratingCount})</p>
        <span className={p.available ? 'badge ok' : 'badge busy'}>{p.available ? 'متاح' : 'مشغول'}</span>
        <div className="actions">
          <a href={`tel:${p.phone}`} className="btn call">اتصال</a>
          <a href={`https://wa.me/${(p.whatsapp || p.phone).replace(/[^0-9]/g, '')}`} target="_blank" rel="noreferrer" className="btn wa">واتساب</a>
        </div>
      </div>

      <h3>التقييمات</h3>
      {user && user.uid !== uid && (
        <div className="card">
          <p>قيّم هذا الحرفي</p>
          <div className="stars">
            {[1,2,3,4,5].map((n) => (
              <span key={n} onClick={() => setStars(n)} style={{ cursor: 'pointer', color: n <= stars ? '#F5A623' : '#ccc' }}>★</span>
            ))}
          </div>
          <textarea value={comment} onChange={(e) => setComment(e.target.value)} placeholder="تعليق (اختياري)" />
          <button className="btn primary" disabled={submitting} onClick={submit}>{submitting ? 'جارٍ...' : 'إرسال التقييم'}</button>
        </div>
      )}
      {reviews.length === 0 ? <p>لا توجد مراجعات.</p> : reviews.map((r, i) => (
        <div key={i} className="card">
          <strong>{r.userName}</strong>
          <span> {'★'.repeat(r.rating)}</span>
          {r.comment && <p>{r.comment}</p>}
        </div>
      ))}
    </div>
  );
}
