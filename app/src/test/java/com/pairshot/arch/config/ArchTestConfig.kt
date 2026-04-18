package com.pairshot.arch.config

import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.core.importer.Location
import java.util.regex.Pattern

class DoNotIncludeAndroidGenerated : ImportOption {
    private val pattern =
        Pattern.compile(
            """.*(\bR\.class|R\$\w+\.class|BuildConfig\.class|_Factory\.class|_HiltModules.*\.class|_MembersInjector\.class|_Impl\.class)$""",
        )

    override fun includes(location: Location): Boolean = !pattern.matcher(location.toString()).matches()
}

class DoNotIncludeKotlinWhenMappings : ImportOption {
    private val pattern = Pattern.compile(""".*\${'$'}WhenMappings\.class""")

    override fun includes(location: Location): Boolean = !pattern.matcher(location.toString()).matches()
}
