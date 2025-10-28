package pl.ejdev.zwoje.core

import io.kotest.core.config.AbstractProjectConfig
import kotlin.time.Duration.Companion.seconds

object KotestProjectConfig : AbstractProjectConfig() {
    override val failOnIgnoredTests = true
    override val timeout = 5.seconds
}