package io.nicolaszurbuchen.appname.infra.preview

/**
 * The two values `@Preview(uiMode = ...)` understands, named.
 *
 * They are Android's `Configuration.UI_MODE_NIGHT_YES` and `_NIGHT_NO`, which commonMain cannot
 * import — the annotation is multiplatform and the constant it takes is not. Writing `0x20` at the
 * call site is the alternative, and a hex literal in an annotation argument is exactly the kind of
 * thing that gets copied into the next file with one digit changed.
 *
 * Nothing but [PreviewThemes] should need these. A preview asking for a ui mode directly is a
 * preview that has decided it is only worth seeing on one ground.
 */
object PreviewUiMode {
    const val NIGHT_NO = 0x10
    const val NIGHT_YES = 0x20
}
