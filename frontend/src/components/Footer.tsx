import React from 'react';
import { Car, Phone, Mail, MapPin } from 'lucide-react';

const Footer: React.FC = () => {
  return (
    <footer className="bg-gradient-to-r from-green-800 to-green-900 text-white">
      <div className="container mx-auto px-4 py-8">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          {/* Company Info */}
          <div>
            <div className="flex items-center space-x-2 mb-4">
              <Car size={24} className="text-green-300" />
              <span className="text-xl font-bold text-green-300">Rentello</span>
            </div>
            <p className="text-green-100">
              Güvenilir araç kiralama hizmetleri ile yolculuklarınızı konforlu hale getiriyoruz.
            </p>
          </div>

          {/* Quick Links */}
          <div>
            <h3 className="text-lg font-semibold mb-4 text-green-300">Hızlı Linkler</h3>
            <ul className="space-y-2">
              <li><a href="/" className="text-green-100 hover:text-white transition">Ana Sayfa</a></li>
              <li><a href="/vehicles" className="text-green-100 hover:text-white transition">Araçlar</a></li>
              <li><a href="/about" className="text-green-100 hover:text-white transition">Hakkımızda</a></li>
              <li><a href="/contact" className="text-green-100 hover:text-white transition">İletişim</a></li>
            </ul>
          </div>

          {/* Services */}
          <div>
            <h3 className="text-lg font-semibold mb-4 text-green-300">Hizmetlerimiz</h3>
            <ul className="space-y-2">
              <li><span className="text-green-100">Günlük Kiralama</span></li>
              <li><span className="text-green-100">Haftalık Kiralama</span></li>
              <li><span className="text-green-100">Aylık Kiralama</span></li>
              <li><span className="text-green-100">Kurumsal Çözümler</span></li>
            </ul>
          </div>

          {/* Contact Info */}
          <div>
            <h3 className="text-lg font-semibold mb-4 text-green-300">İletişim</h3>
            <div className="space-y-2">
              <div className="flex items-center space-x-2">
                <Phone size={16} className="text-green-300" />
                <span className="text-green-100">+90 212 555 0123</span>
              </div>
              <div className="flex items-center space-x-2">
                <Mail size={16} className="text-green-300" />
                <span className="text-green-100">info@rentello.com</span>
              </div>
              <div className="flex items-center space-x-2">
                <MapPin size={16} className="text-green-300" />
                <span className="text-green-100">İstanbul, Türkiye</span>
              </div>
            </div>
          </div>
        </div>

        <div className="border-t border-green-600 mt-8 pt-4 text-center">
          <p className="text-green-100">
            © 2025 Rentello. Tüm hakları saklıdır.
          </p>
        </div>
      </div>
    </footer>
  );
};

export default Footer; 