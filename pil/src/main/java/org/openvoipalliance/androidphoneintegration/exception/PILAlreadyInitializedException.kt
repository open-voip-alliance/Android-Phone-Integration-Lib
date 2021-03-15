package org.openvoipalliance.androidphoneintegration.exception

class PILAlreadyInitializedException internal constructor(): Exception("The PIL has already been initialized, make sure startAndroidPIL is only called once.")