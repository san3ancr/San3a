export type UserRole = 'craftsman' | 'customer';

export interface UserProfile {
  uid: string;
  name: string;
  phone: string;
  whatsapp: string;
  role: UserRole;
  craft: string;
  region: string;
  available: boolean;
  createdAt: number;
  ratingSum: number;
  ratingCount: number;
  latitude: number;
  longitude: number;
}

export interface Review {
  userId: string;
  userName: string;
  rating: number;
  comment: string;
  createdAt: number;
}

export const CRAFTS = [
  'سبّاك','كهربائي','نجّار','حداد','خيّاط','حلاق','ميكانيكي سيارات','دهّان','بنّاء',
  'فني تكييف وتبريد','فني أجهزة كهربائية','تنظيف منازل','نقل أثاث','جبس وديكور',
  'تركيب كاميرات مراقبة','فني كمبيوتر وشبكات'
];

export const REGIONS = [
  'الرياض','جدة','مكة المكرمة','المدينة المنورة','الدمام','الخبر','الظهران','الطائف',
  'تبوك','أبها','خميس مشيط','بريدة','عنيزة','حائل','الجبيل','ينبع','نجران','جيزان'
];
