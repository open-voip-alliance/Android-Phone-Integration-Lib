include(":pil")
include(":app")
rootProject.name = "Android Phone Integration Lib"
val useLocalVoipLibrary = false


if (file("../android-voip-lib").exists() && useLocalVoipLibrary) {
    includeBuild("../android-voip-lib") {
        dependencySubstitution {
            substitute(module("com.github.open-voip-alliance:Android-VoIP-Lib")).with(project(":AndroidVoIPLib"))
        }
    }
}
