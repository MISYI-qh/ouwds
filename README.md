# ouwds
Catur yang tenang. Produk Misyi (lahir 18-11-2026).

- `web/`       situs satu file `index.html` (tanpa PWA/service worker) -> Vercel, Root Directory: `web`
- `android/`   aplikasi Android (Kotlin) yang memuat `web/index.html` dari dalam aplikasi
- `.circleci/` cek web + build APK debug (artifact `ouwds-debug.apk`)

Build lokal Android: `cp web/index.html android/app/src/main/assets/` lalu `cd android && gradle assembleDebug`.

Situs: https://ouwds.my.id
