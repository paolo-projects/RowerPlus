package it.paoloinfante.rowerplus.utils

import it.paoloinfante.rowerplus.database.models.*
import it.paoloinfante.rowerplus.models.yamldocument.Document
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.time.Duration
import java.util.*

class YamlSchemeParser(private val filePath: String) {
    fun parse(): SchemeWithStepsAndVariables? {
        val yamlFile = File(filePath)
        val yamlInputStream = yamlFile.inputStream()

        try {
            val yaml = Yaml().load(yamlInputStream) as Document
            return SchemeWithStepsAndVariables(
                scheme = Scheme(
                    id = null,
                    name = yaml.name,
                    time = Date()
                ),
                schemeSteps = yaml.steps.map { step ->
                    SchemeStepWithVariables(
                        schemeStep = SchemeStep(
                            id = null,
                            schemeId = -1,
                            duration = Duration.ofSeconds(step.duration)
                        ),
                        schemeStepVariables = step.variables.map { variable ->
                            SchemeStepVariable(
                                id = null,
                                schemeStepId = -1,
                                parameter = variable.name,
                                value = variable.value
                            )
                        }
                    )
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}