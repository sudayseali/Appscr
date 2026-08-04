import os
import json

base_strings = {
    "app_name": "NoxScreen Pro",
    "eco_screen_optimizer": "ECO SCREEN OPTIMIZER",
    "energy_saved": "Energy Saved",
    "screen_off": "Screen Off",
    "display_settings": "Display Settings",
    "always_on_display": "Always-On Display",
    "oled_pixel_shift": "OLED Pixel Shift",
    "privacy_tint": "Privacy Tint",
    "smart_triggers": "Smart Triggers",
    "pocket_mode": "Pocket Mode",
    "sleep_timer": "Sleep Timer (Battery Saver)",
    "floating_action_button": "Floating Action Button",
    "floating_lock_style": "Floating Lock Style",
    "floating_lock_size": "Floating Lock Size",
    "wake_gesture": "Wake Gesture (Taps)",
    "security": "Security",
    "enable_biometric": "Enable Biometric Authentication",
    "usage_limits": "Usage Limits (Focus Mode)",
    "overlay_permission": "Overlay Permission Required",
    "tap_to_grant": "Tap to grant access for background functionality.",
    "start": "Start",
    "stop": "Stop"
}

translations = {
    "ar": {
        "app_name": "NoxScreen Pro", "eco_screen_optimizer": "مُحسِّن الشاشة البيئي", "energy_saved": "الطاقة الموفرة", "screen_off": "إيقاف الشاشة", "display_settings": "إعدادات العرض", "always_on_display": "العرض الدائم", "oled_pixel_shift": "إزاحة بكسل OLED", "privacy_tint": "تظليل الخصوصية", "smart_triggers": "المشغلات الذكية", "pocket_mode": "وضع الجيب", "sleep_timer": "مؤقت النوم", "floating_action_button": "زر الإجراء العائم", "floating_lock_style": "نمط القفل العائم", "floating_lock_size": "حجم القفل العائم", "wake_gesture": "إيماءة الاستيقاظ", "security": "الأمان", "enable_biometric": "تمكين المصادقة الحيوية", "usage_limits": "حدود الاستخدام", "overlay_permission": "مطلوب إذن التراكب", "tap_to_grant": "انقر لمنح إذن الخلفية.", "start": "ابدأ", "stop": "توقف"
    },
    "bn": {
        "app_name": "NoxScreen Pro", "eco_screen_optimizer": "ইকো স্ক্রিন অপ্টিমাইজার", "energy_saved": "শক্তি সঞ্চিত", "screen_off": "স্ক্রিন বন্ধ", "display_settings": "ডিসপ্লে সেটিংস", "always_on_display": "সর্বদা অন ডিসপ্লে", "oled_pixel_shift": "ওএলইডি পিক্সেল শিফট", "privacy_tint": "গোপনীয়তা টিন্ট", "smart_triggers": "স্মার্ট ট্রিগার", "pocket_mode": "পকেট মোড", "sleep_timer": "স্লিপ টাইমার", "floating_action_button": "ভাসমান অ্যাকশন বোতাম", "floating_lock_style": "ভাসমান লক স্টাইল", "floating_lock_size": "ভাসমান লক আকার", "wake_gesture": "জাগানোর ভঙ্গি", "security": "নিরাপত্তা", "enable_biometric": "বায়োমেট্রিক সক্ষম করুন", "usage_limits": "ব্যবহারের সীমা", "overlay_permission": "ওভারলে অনুমতি প্রয়োজন", "tap_to_grant": "অনুমতি দিতে আলতো চাপুন।", "start": "শুরু", "stop": "বন্ধ"
    },
    "zh": {
        "app_name": "NoxScreen Pro", "eco_screen_optimizer": "生态屏幕优化器", "energy_saved": "节省的能源", "screen_off": "关闭屏幕", "display_settings": "显示设置", "always_on_display": "息屏显示", "oled_pixel_shift": "OLED 像素偏移", "privacy_tint": "隐私色调", "smart_triggers": "智能触发器", "pocket_mode": "口袋模式", "sleep_timer": "睡眠定时器", "floating_action_button": "悬浮按钮", "floating_lock_style": "悬浮锁样式", "floating_lock_size": "悬浮锁大小", "wake_gesture": "唤醒手势", "security": "安全", "enable_biometric": "启用生物识别", "usage_limits": "使用限制", "overlay_permission": "需要悬浮窗权限", "tap_to_grant": "点击以授予后台访问权限。", "start": "开始", "stop": "停止"
    },
    "es": {
        "app_name": "NoxScreen Pro", "eco_screen_optimizer": "OPTIMIZADOR DE PANTALLA ECO", "energy_saved": "Energía Ahorrada", "screen_off": "Pantalla Apagada", "display_settings": "Configuración de pantalla", "always_on_display": "Pantalla siempre encendida", "oled_pixel_shift": "Desplazamiento de píxeles OLED", "privacy_tint": "Tinte de privacidad", "smart_triggers": "Disparadores inteligentes", "pocket_mode": "Modo de bolsillo", "sleep_timer": "Temporizador de sueño", "floating_action_button": "Botón de acción flotante", "floating_lock_style": "Estilo de bloqueo flotante", "floating_lock_size": "Tamaño de bloqueo flotante", "wake_gesture": "Gesto de activación", "security": "Seguridad", "enable_biometric": "Habilitar autenticación biométrica", "usage_limits": "Límites de uso", "overlay_permission": "Se requiere permiso de superposición", "tap_to_grant": "Toca para conceder acceso en segundo plano.", "start": "Iniciar", "stop": "Detener"
    },
    "fr": {
        "app_name": "NoxScreen Pro", "eco_screen_optimizer": "OPTIMISEUR D'ÉCRAN ÉCO", "energy_saved": "Énergie économisée", "screen_off": "Écran éteint", "display_settings": "Paramètres d'affichage", "always_on_display": "Écran toujours allumé", "oled_pixel_shift": "Décalage de pixels OLED", "privacy_tint": "Teinte de confidentialité", "smart_triggers": "Déclencheurs intelligents", "pocket_mode": "Mode poche", "sleep_timer": "Minuterie de sommeil", "floating_action_button": "Bouton d'action flottant", "floating_lock_style": "Style de verrouillage flottant", "floating_lock_size": "Taille du verrouillage flottant", "wake_gesture": "Geste de réveil", "security": "Sécurité", "enable_biometric": "Activer l'authentification biométrique", "usage_limits": "Limites d'utilisation", "overlay_permission": "Permission de superposition requise", "tap_to_grant": "Appuyez pour accorder l'accès en arrière-plan.", "start": "Démarrer", "stop": "Arrêter"
    },
    "de": {
        "app_name": "NoxScreen Pro", "eco_screen_optimizer": "ÖKO-BILDSCHIRMOPTIMIERER", "energy_saved": "Gesparte Energie", "screen_off": "Bildschirm aus", "display_settings": "Anzeigeeinstellungen", "always_on_display": "Immer eingeschaltetes Display", "oled_pixel_shift": "OLED-Pixelverschiebung", "privacy_tint": "Datenschutztönung", "smart_triggers": "Intelligente Auslöser", "pocket_mode": "Taschenmodus", "sleep_timer": "Sleep-Timer", "floating_action_button": "Schwebende Aktionstaste", "floating_lock_style": "Schwebender Sperrstil", "floating_lock_size": "Schwebende Sperrgröße", "wake_gesture": "Aufweck-Geste", "security": "Sicherheit", "enable_biometric": "Biometrische Authentifizierung aktivieren", "usage_limits": "Nutzungsbeschränkungen", "overlay_permission": "Overlay-Berechtigung erforderlich", "tap_to_grant": "Tippen, um Hintergrundzugriff zu gewähren.", "start": "Start", "stop": "Stopp"
    },
    "hi": {
        "app_name": "NoxScreen Pro", "eco_screen_optimizer": "इको स्क्रीन ऑप्टिमाइज़र", "energy_saved": "बचाई गई ऊर्जा", "screen_off": "स्क्रीन बंद", "display_settings": "डिस्प्ले सेटिंग्स", "always_on_display": "ऑलवेज-ऑन डिस्प्ले", "oled_pixel_shift": "OLED पिक्सेल शिफ्ट", "privacy_tint": "गोपनीयता टिंट", "smart_triggers": "स्मार्ट ट्रिगर", "pocket_mode": "पॉकेट मोड", "sleep_timer": "स्लीप टाइमर", "floating_action_button": "फ्लोटिंग एक्शन बटन", "floating_lock_style": "फ्लोटिंग लॉक स्टाइल", "floating_lock_size": "फ्लोटिंग लॉक साइज", "wake_gesture": "वेक जेस्चर", "security": "सुरक्षा", "enable_biometric": "बायोमेट्रिक प्रमाणीकरण सक्षम करें", "usage_limits": "उपयोग सीमाएं", "overlay_permission": "ओवरले अनुमति आवश्यक है", "tap_to_grant": "पृष्ठभूमि तक पहुंच प्रदान करने के लिए टैप करें।", "start": "शुरू", "stop": "रोकें"
    },
    "id": {
        "app_name": "NoxScreen Pro", "eco_screen_optimizer": "PENGOPTIMAL LAYAR ECO", "energy_saved": "Energi yang Dihemat", "screen_off": "Layar Mati", "display_settings": "Pengaturan Tampilan", "always_on_display": "Layar Selalu Aktif", "oled_pixel_shift": "Pergeseran Piksel OLED", "privacy_tint": "Warna Privasi", "smart_triggers": "Pemicu Pintar", "pocket_mode": "Mode Saku", "sleep_timer": "Pengatur Waktu Tidur", "floating_action_button": "Tombol Aksi Mengambang", "floating_lock_style": "Gaya Kunci Mengambang", "floating_lock_size": "Ukuran Kunci Mengambang", "wake_gesture": "Gestur Bangun", "security": "Keamanan", "enable_biometric": "Aktifkan Otentikasi Biometrik", "usage_limits": "Batas Penggunaan", "overlay_permission": "Izin Hamparan Diperlukan", "tap_to_grant": "Ketuk untuk memberikan akses latar belakang.", "start": "Mulai", "stop": "Berhenti"
    },
    "it": {
        "app_name": "NoxScreen Pro", "eco_screen_optimizer": "OTTIMIZZATORE DELLO SCHERMO ECO", "energy_saved": "Energia Risparmiata", "screen_off": "Schermo Spento", "display_settings": "Impostazioni dello schermo", "always_on_display": "Schermo sempre acceso", "oled_pixel_shift": "Spostamento dei pixel OLED", "privacy_tint": "Tinta per la privacy", "smart_triggers": "Trigger intelligenti", "pocket_mode": "Modalità tasca", "sleep_timer": "Timer di spegnimento", "floating_action_button": "Pulsante di azione flottante", "floating_lock_style": "Stile di blocco flottante", "floating_lock_size": "Dimensione del blocco flottante", "wake_gesture": "Gesto di risveglio", "security": "Sicurezza", "enable_biometric": "Abilita autenticazione biometrica", "usage_limits": "Limiti di utilizzo", "overlay_permission": "È richiesta l'autorizzazione in sovrimpressione", "tap_to_grant": "Tocca per concedere l'accesso in background.", "start": "Inizia", "stop": "Ferma"
    },
    "ja": {
        "app_name": "NoxScreen Pro", "eco_screen_optimizer": "エコ画面オプティマイザ", "energy_saved": "節約されたエネルギー", "screen_off": "画面オフ", "display_settings": "表示設定", "always_on_display": "常時表示ディスプレイ", "oled_pixel_shift": "OLEDピクセルシフト", "privacy_tint": "プライバシーティント", "smart_triggers": "スマートトリガー", "pocket_mode": "ポケットモード", "sleep_timer": "スリープタイマー", "floating_action_button": "フローティングアクションボタン", "floating_lock_style": "フローティングロックスタイル", "floating_lock_size": "フローティングロックサイズ", "wake_gesture": "ウェイクジェスチャー", "security": "セキュリティ", "enable_biometric": "生体認証を有効にする", "usage_limits": "使用制限", "overlay_permission": "オーバーレイ権限が必要です", "tap_to_grant": "タップしてバックグラウンドアクセスを許可します。", "start": "開始", "stop": "停止"
    },
    "ko": {
        "app_name": "NoxScreen Pro", "eco_screen_optimizer": "에코 화면 최적화", "energy_saved": "절약된 에너지", "screen_off": "화면 끄기", "display_settings": "디스플레이 설정", "always_on_display": "항상 표시 (AOD)", "oled_pixel_shift": "OLED 픽셀 시프트", "privacy_tint": "프라이버시 틴트", "smart_triggers": "스마트 트리거", "pocket_mode": "포켓 모드", "sleep_timer": "수면 타이머", "floating_action_button": "플로팅 액션 버튼", "floating_lock_style": "플로팅 잠금 스타일", "floating_lock_size": "플로팅 잠금 크기", "wake_gesture": "깨우기 제스처", "security": "보안", "enable_biometric": "생체 인증 활성화", "usage_limits": "사용 제한", "overlay_permission": "오버레이 권한 필요", "tap_to_grant": "탭하여 백그라운드 액세스 허용.", "start": "시작", "stop": "중지"
    },
    "mr": {
        "app_name": "NoxScreen Pro", "eco_screen_optimizer": "इको स्क्रीन ऑप्टिमायझर", "energy_saved": "वाचवलेली ऊर्जा", "screen_off": "स्क्रीन बंद", "display_settings": "डिस्प्ले सेटिंग्ज", "always_on_display": "नेहमी सुरू असलेली स्क्रीन", "oled_pixel_shift": "OLED पिक्सेल शिफ्ट", "privacy_tint": "गोपनीयता टिंट", "smart_triggers": "स्मार्ट ट्रिगर्स", "pocket_mode": "पॉकेट मोड", "sleep_timer": "स्लीप टायमर", "floating_action_button": "फ्लोटिंग ॲक्शन बटण", "floating_lock_style": "फ्लोटिंग लॉक शैली", "floating_lock_size": "फ्लोटिंग लॉक आकार", "wake_gesture": "वेक जेश्चर", "security": "सुरक्षा", "enable_biometric": "बायोमेट्रिक प्रमाणीकरण सक्षम करा", "usage_limits": "वापर मर्यादा", "overlay_permission": "ओव्हरले परवानगी आवश्यक आहे", "tap_to_grant": "पार्श्वभूमी प्रवेश देण्यासाठी टॅप करा.", "start": "सुरू करा", "stop": "थांबवा"
    },
    "pa": {
        "app_name": "NoxScreen Pro", "eco_screen_optimizer": "ਈਕੋ ਸਕ੍ਰੀਨ ਆਪਟੀਮਾਈਜ਼ਰ", "energy_saved": "ਬਚਾਈ ਗਈ ਊਰਜਾ", "screen_off": "ਸਕ੍ਰੀਨ ਬੰਦ", "display_settings": "ਡਿਸਪਲੇ ਸੈਟਿੰਗਾਂ", "always_on_display": "ਹਮੇਸ਼ਾ-ਆਨ ਡਿਸਪਲੇ", "oled_pixel_shift": "OLED ਪਿਕਸਲ ਸ਼ਿਫਟ", "privacy_tint": "ਪਰਦੇਦਾਰੀ ਟਿੰਟ", "smart_triggers": "ਸਮਾਰਟ ਟ੍ਰਿਗਰਸ", "pocket_mode": "ਪਾਕੇਟ ਮੋਡ", "sleep_timer": "ਸਲੀਪ ਟਾਈਮਰ", "floating_action_button": "ਫਲੋਟਿੰਗ ਐਕਸ਼ਨ ਬਟਨ", "floating_lock_style": "ਫਲੋਟਿੰਗ ਲੌਕ ਸਟਾਈਲ", "floating_lock_size": "ਫਲੋਟਿੰਗ ਲੌਕ ਆਕਾਰ", "wake_gesture": "ਵੇਕ ਜੈਸਚਰ", "security": "ਸੁਰੱਖਿਆ", "enable_biometric": "ਬਾਇਓਮੀਟ੍ਰਿਕ ਪ੍ਰਮਾਣਿਕਤਾ ਯੋਗ ਕਰੋ", "usage_limits": "ਵਰਤੋਂ ਸੀਮਾਵਾਂ", "overlay_permission": "ਓਵਰਲੇਅ ਇਜਾਜ਼ਤ ਦੀ ਲੋੜ ਹੈ", "tap_to_grant": "ਪਿਛੋਕੜ ਤੱਕ ਪਹੁੰਚ ਦੇਣ ਲਈ ਟੈਪ ਕਰੋ।", "start": "ਸ਼ੁਰੂ ਕਰੋ", "stop": "ਰੋਕੋ"
    },
    "pt": {
        "app_name": "NoxScreen Pro", "eco_screen_optimizer": "OTIMIZADOR DE TELA ECO", "energy_saved": "Energia Economizada", "screen_off": "Tela Apagada", "display_settings": "Configurações de Exibição", "always_on_display": "Tela sempre ativa", "oled_pixel_shift": "Deslocamento de pixels OLED", "privacy_tint": "Tonalidade de privacidade", "smart_triggers": "Gatilhos inteligentes", "pocket_mode": "Modo de bolso", "sleep_timer": "Temporizador de sono", "floating_action_button": "Botão de ação flutuante", "floating_lock_style": "Estilo de bloqueio flutuante", "floating_lock_size": "Tamanho do bloqueio flutuante", "wake_gesture": "Gesto de despertar", "security": "Segurança", "enable_biometric": "Ativar autenticação biométrica", "usage_limits": "Limites de uso", "overlay_permission": "Permissão de sobreposição necessária", "tap_to_grant": "Toque para conceder acesso em segundo plano.", "start": "Iniciar", "stop": "Parar"
    },
    "ru": {
        "app_name": "NoxScreen Pro", "eco_screen_optimizer": "ЭКО ОПТИМИЗАТОР ЭКРАНА", "energy_saved": "Сохраненная энергия", "screen_off": "Экран выключен", "display_settings": "Настройки дисплея", "always_on_display": "Всегда на экране (AOD)", "oled_pixel_shift": "Сдвиг пикселей OLED", "privacy_tint": "Оттенок конфиденциальности", "smart_triggers": "Умные триггеры", "pocket_mode": "Карманный режим", "sleep_timer": "Таймер сна", "floating_action_button": "Плавающая кнопка", "floating_lock_style": "Стиль плавающего замка", "floating_lock_size": "Размер плавающего замка", "wake_gesture": "Жест пробуждения", "security": "Безопасность", "enable_biometric": "Включить биометрическую аутентификацию", "usage_limits": "Лимиты использования", "overlay_permission": "Требуется разрешение на наложение", "tap_to_grant": "Нажмите, чтобы разрешить фоновый доступ.", "start": "Начать", "stop": "Остановить"
    },
    "te": {
        "app_name": "NoxScreen Pro", "eco_screen_optimizer": "ఎకో స్క్రీన్ ఆప్టిమైజర్", "energy_saved": "ఆదా అయిన శక్తి", "screen_off": "స్క్రీన్ ఆఫ్‌లో ఉంది", "display_settings": "ప్రదర్శన సెట్టింగ్‌లు", "always_on_display": "ఎల్లప్పుడూ ఆన్‌లో ఉండే ప్రదర్శన", "oled_pixel_shift": "OLED పిక్సెల్ షిఫ్ట్", "privacy_tint": "గోప్యతా రంగు", "smart_triggers": "స్మార్ట్ ట్రిగ్గర్‌లు", "pocket_mode": "పాకెట్ మోడ్", "sleep_timer": "స్లీప్ టైమర్", "floating_action_button": "ఫ్లోటింగ్ యాక్షన్ బటన్", "floating_lock_style": "ఫ్లోటింగ్ లాక్ శైలి", "floating_lock_size": "ఫ్లోటింగ్ లాక్ పరిమాణం", "wake_gesture": "వేక్ సంజ్ఞ", "security": "భద్రత", "enable_biometric": "బయోమెట్రిక్ ప్రమాణీకరణను ప్రారంభించండి", "usage_limits": "వినియోగ పరిమితులు", "overlay_permission": "ఓవర్‌లే అనుమతి అవసరం", "tap_to_grant": "నేపథ్య ప్రాప్యతను మంజూరు చేయడానికి నొక్కండి.", "start": "ప్రారంభించు", "stop": "ఆపు"
    },
    "tr": {
        "app_name": "NoxScreen Pro", "eco_screen_optimizer": "EKO EKRAN OPTİMİZATÖRÜ", "energy_saved": "Tasarruf Edilen Enerji", "screen_off": "Ekran Kapalı", "display_settings": "Ekran Ayarları", "always_on_display": "Her Zaman Açık Ekran", "oled_pixel_shift": "OLED Piksel Kaydırma", "privacy_tint": "Gizlilik Tonu", "smart_triggers": "Akıllı Tetikleyiciler", "pocket_mode": "Cep Modu", "sleep_timer": "Uyku Zamanlayıcısı", "floating_action_button": "Kayan İşlem Düğmesi", "floating_lock_style": "Kayan Kilit Stili", "floating_lock_size": "Kayan Kilit Boyutu", "wake_gesture": "Uyandırma Hareketi", "security": "Güvenlik", "enable_biometric": "Biyometrik Kimlik Doğrulamayı Etkinleştir", "usage_limits": "Kullanım Sınırları", "overlay_permission": "Görünüm Üste Çizme İzni Gerekli", "tap_to_grant": "Arka plan erişimi vermek için dokunun.", "start": "Başlat", "stop": "Durdur"
    },
    "ur": {
        "app_name": "NoxScreen Pro", "eco_screen_optimizer": "ایکو سکرین آپٹیمائزر", "energy_saved": "بچائی گئی توانائی", "screen_off": "سکرین بند", "display_settings": "ڈسپلے سیٹنگز", "always_on_display": "ہمیشہ آن ڈسپلے", "oled_pixel_shift": "OLED پکسل شفٹ", "privacy_tint": "رازداری کا رنگ", "smart_triggers": "سمارٹ ٹرگرز", "pocket_mode": "پاکٹ موڈ", "sleep_timer": "سلیپ ٹائمر", "floating_action_button": "فلوٹنگ ایکشن بٹن", "floating_lock_style": "فلوٹنگ لاک اسٹائل", "floating_lock_size": "فلوٹنگ لاک سائز", "wake_gesture": "ویک اشارہ", "security": "سیکیورٹی", "enable_biometric": "بایومیٹرک تصدیق کو فعال کریں", "usage_limits": "استعمال کی حدود", "overlay_permission": "اوورلے کی اجازت درکار ہے", "tap_to_grant": "بیک گراؤنڈ تک رسائی کے لیے ٹیپ کریں۔", "start": "شروع کریں", "stop": "روکیں"
    },
    "vi": {
        "app_name": "NoxScreen Pro", "eco_screen_optimizer": "TRÌNH TỐI ƯU HÓA MÀN HÌNH ECO", "energy_saved": "Năng lượng đã tiết kiệm", "screen_off": "Màn hình Tắt", "display_settings": "Cài đặt hiển thị", "always_on_display": "Màn hình luôn bật", "oled_pixel_shift": "Dịch chuyển pixel OLED", "privacy_tint": "Sắc thái bảo mật", "smart_triggers": "Trình kích hoạt thông minh", "pocket_mode": "Chế độ bỏ túi", "sleep_timer": "Hẹn giờ ngủ", "floating_action_button": "Nút hành động nổi", "floating_lock_style": "Kiểu khóa nổi", "floating_lock_size": "Kích thước khóa nổi", "wake_gesture": "Cử chỉ đánh thức", "security": "Bảo mật", "enable_biometric": "Bật xác thực sinh trắc học", "usage_limits": "Giới hạn sử dụng", "overlay_permission": "Cần quyền hiển thị trên các ứng dụng khác", "tap_to_grant": "Nhấn để cấp quyền truy cập nền.", "start": "Bắt đầu", "stop": "Dừng"
    },
    "sw": {
        "app_name": "NoxScreen Pro", "eco_screen_optimizer": "KIBoreshaji Skrini cha ECO", "energy_saved": "Nishati Iliyohifadhiwa", "screen_off": "Skrini Imezimwa", "display_settings": "Mipangilio ya Kuonyesha", "always_on_display": "Onyesho la Kila Wakati", "oled_pixel_shift": "Shift ya Pixel ya OLED", "privacy_tint": "Tint ya Faragha", "smart_triggers": "Vichochezi Mahiri", "pocket_mode": "Hali ya Mfukoni", "sleep_timer": "Kipima Muda cha Kulala", "floating_action_button": "Kitufe cha Kitendo Kielea", "floating_lock_style": "Mtindo wa Kufunga Kielea", "floating_lock_size": "Saizi ya Kufunga Kielea", "wake_gesture": "Ishara ya Kuamka", "security": "Usalama", "enable_biometric": "Washa Uthibitishaji wa Kibayometriki", "usage_limits": "Mipaka ya Matumizi", "overlay_permission": "Ruhusa ya Uwekeleaji Inahitajika", "tap_to_grant": "Gusa ili kutoa ufikiaji wa chinichini.", "start": "Anza", "stop": "Simama"
    },
    "fa": {
        "app_name": "NoxScreen Pro", "eco_screen_optimizer": "بهینه‌ساز صفحه نمایش اکو", "energy_saved": "انرژی ذخیره شده", "screen_off": "صفحه نمایش خاموش", "display_settings": "تنظیمات نمایش", "always_on_display": "نمایشگر همیشه روشن", "oled_pixel_shift": "تغییر پیکسل OLED", "privacy_tint": "رنگ حریم خصوصی", "smart_triggers": "محرک‌های هوشمند", "pocket_mode": "حالت جیب", "sleep_timer": "تایمر خواب", "floating_action_button": "دکمه اقدام شناور", "floating_lock_style": "سبک قفل شناور", "floating_lock_size": "اندازه قفل شناور", "wake_gesture": "ژست بیداری", "security": "امنیت", "enable_biometric": "فعال‌سازی احراز هویت بیومتریک", "usage_limits": "محدودیت‌های استفاده", "overlay_permission": "مجوز هم‌پوشانی الزامی است", "tap_to_grant": "برای اعطای دسترسی پس‌زمینه ضربه بزنید.", "start": "شروع", "stop": "توقف"
    },
    "ta": {
        "app_name": "NoxScreen Pro", "eco_screen_optimizer": "சுற்றுச்சூழல் திரை உகப்பாக்கி", "energy_saved": "சேமிக்கப்பட்ட ஆற்றல்", "screen_off": "திரை ஆஃப்", "display_settings": "காட்சி அமைப்புகள்", "always_on_display": "எப்போதும் ஆன் டிஸ்ப்ளே", "oled_pixel_shift": "OLED பிக்சல் மாற்றம்", "privacy_tint": "தனியுரிமை டின்ட்", "smart_triggers": "ஸ்மார்ட் தூண்டுதல்கள்", "pocket_mode": "பாக்கெட் பயன்முறை", "sleep_timer": "ஸ்லீப் டைமர்", "floating_action_button": "மிதக்கும் செயல் பொத்தான்", "floating_lock_style": "மிதக்கும் பூட்டு பாணி", "floating_lock_size": "மிதக்கும் பூட்டு அளவு", "wake_gesture": "எழுப்புதல் சைகை", "security": "பாதுகாப்பு", "enable_biometric": "பயோமெட்ரிக் அங்கீகாரத்தை இயக்கு", "usage_limits": "பயன்பாட்டு வரம்புகள்", "overlay_permission": "மேலடுக்கு அனுமதி தேவை", "tap_to_grant": "பின்னணி அணுகலை வழங்க தட்டவும்.", "start": "தொடங்கு", "stop": "நிறுத்து"
    },
    "gu": {
        "app_name": "NoxScreen Pro", "eco_screen_optimizer": "ઇકો સ્ક્રીન ઑપ્ટિમાઇઝર", "energy_saved": "બચાવેલ ઊર્જા", "screen_off": "સ્ક્રીન બંધ", "display_settings": "પ્રદર્શન સેટિંગ્સ", "always_on_display": "હંમેશા-ચાલુ પ્રદર્શન", "oled_pixel_shift": "OLED પિક્સેલ શિફ્ટ", "privacy_tint": "ગોપનીયતા ટિન્ટ", "smart_triggers": "સ્માર્ટ ટ્રિગર્સ", "pocket_mode": "પોકેટ મોડ", "sleep_timer": "સ્લીપ ટાઈમર", "floating_action_button": "ફ્લોટિંગ એક્શન બટન", "floating_lock_style": "ફ્લોટિંગ લૉક શૈલી", "floating_lock_size": "ફ્લોટિંગ લૉક કદ", "wake_gesture": "વેક જેસ્ચર", "security": "સુરક્ષા", "enable_biometric": "બાયોમેટ્રિક પ્રમાણીકરણ સક્ષમ કરો", "usage_limits": "વપરાશ મર્યાદાઓ", "overlay_permission": "ઓવરલે પરવાનગી જરૂરી છે", "tap_to_grant": "પૃષ્ઠભૂમિ ઍક્સેસ આપવા માટે ટેપ કરો.", "start": "શરૂઆત", "stop": "થોભો"
    }
}

# Creating values-xx directories and strings.xml
for code, translations_dict in translations.items():
    dir_path = f"app/src/main/res/values-{code}"
    os.makedirs(dir_path, exist_ok=True)
    with open(f"{dir_path}/strings.xml", "w", encoding="utf-8") as f:
        f.write('<?xml version="1.0" encoding="utf-8"?>\n<resources>\n')
        for key, val in translations_dict.items():
            val = val.replace("'", "\\'")
            f.write(f'    <string name="{key}">{val}</string>\n')
        f.write('</resources>\n')
