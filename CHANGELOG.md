# Changelog

## Unreleased
### Added
- Unit tests (Geo, Models, PhoneUtils).
- GitHub Actions CI (tests + assembleDebug).
- Account deletion flow (in-app).
- Fastlane metadata (ar-SA).
- Predictive back support.

### Fixed
- Google Sign-In config via Gradle property.
- Firestore `saveProfile` now merges (preserves fcmToken).
- OTP validation (6 digits).
- Phone auth auto-verify error handling.
- Craftsman detail loading timeout + retry.
- Reviews submission error reporting.
- Location permission denial feedback (Snackbar).
- Various UX/robustness improvements.

### Security
- `.gitignore` for secrets/build artifacts.
- `allowBackup=false`.
- Removed plaintext secrets from tracked files (via ignore).