# GeTaTaPi Compose design tokens

Values in `Color.kt`, `Type.kt`, `Dimens.kt`, and `Shape.kt` are transcribed from
`docs/05-design-system.docx`.

The design specification names Inter as the font family. The theme currently
uses Android's Sans Serif fallback so the project remains buildable without
bundled font assets. To finish the font part, add licensed Inter static TTF files
to `presentation/src/main/res/font/` (Regular, Medium, SemiBold, Bold), then
replace `AppFontFamily` in `Type.kt` with a `FontFamily` built from those
resources. The official Inter download and SIL Open Font License are available
from https://rsms.me/inter/download/.
