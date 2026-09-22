# BeanPack null-safe crash-report diagnostic coremod

This temporary Minecraft 1.12.2 coremod changes the three `String.equals`
comparisons in `CrashReportCategory`'s stack-frame matching method to
`Objects.equals`. This lets Minecraft record the original exception when a
stack frame has no source filename.

It does not alter saves, registries, recipes, or gameplay. Remove
`!beanpack-nullsafe-crashreport-1.0.jar` from the instance's `mods` directory
after the concealed crash has been captured.
