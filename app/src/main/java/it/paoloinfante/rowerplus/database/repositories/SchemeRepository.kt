package it.paoloinfante.rowerplus.database.repositories

import androidx.room.withTransaction
import it.paoloinfante.rowerplus.database.RowerPlusDatabase
import it.paoloinfante.rowerplus.database.dao.SchemeDao
import it.paoloinfante.rowerplus.database.dao.SchemeStepDao
import it.paoloinfante.rowerplus.database.dao.SchemeStepVariableDao
import it.paoloinfante.rowerplus.database.models.Scheme
import it.paoloinfante.rowerplus.database.models.SchemeStep
import it.paoloinfante.rowerplus.database.models.SchemeStepVariable
import it.paoloinfante.rowerplus.database.models.SchemeWithStepsAndVariables
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SchemeRepository @Inject constructor(
    private val schemeDao: SchemeDao,
    private val schemeStepDao: SchemeStepDao,
    private val schemeStepVariableDao: SchemeStepVariableDao,
    private val rowerPlusDatabase: RowerPlusDatabase
) {
    fun getAllSchemes(): Flow<List<SchemeWithStepsAndVariables>> {
        return schemeDao.getAllSchemes()
    }

    suspend fun insertScheme(scheme: Scheme): Long {
        return schemeDao.insert(scheme)
    }

    suspend fun insertSchemeStep(schemeStep: SchemeStep): Long {
        return schemeStepDao.insert(schemeStep)
    }

    suspend fun insertSchemeStepVariable(schemeStepVariable: SchemeStepVariable): Long {
        return schemeStepVariableDao.insert(schemeStepVariable)
    }

    suspend fun insertSchemeWithStepsAndVariables(scheme: SchemeWithStepsAndVariables): Long {
        return rowerPlusDatabase.withTransaction {
            val schemeId = schemeDao.insert(scheme.scheme)
            scheme.schemeSteps.forEach { step ->
                step.schemeStep.schemeId = schemeId.toInt()
                val stepId = schemeStepDao.insert(step.schemeStep)

                step.schemeStepVariables.forEach { variable ->
                    variable.schemeStepId = stepId.toInt()
                    schemeStepVariableDao.insert(variable)
                }
            }
            return@withTransaction schemeId
        }
    }
}