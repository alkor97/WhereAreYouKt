# WhereAreYouKt

Personal phone locator reimplemented in Kotlin.

## Release procedure:
- Commit all changes
- Tag sources with `git tag <tag>` (tag format is `vX.Y.Z`)
- Push created tag to repository (`git push origin vX.Y.Z`)
- Call _Rebuild Project_ from Android Studio for `release` variant
  -- APK file is produced to `.../WhereAreYouKt/app/build/outputs/apk/release/WhereAreYouKt-X.Y.Z-release.apk`
- push changes to repository (`git push`)
- Create a new release in repository:
  -- Call _Draft a new release_ from repository page using created tag
  -- Attach generated APK file
