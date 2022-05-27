package it.paoloinfante.rowerplus.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.paoloinfante.rowerplus.database.models.Scheme
import it.paoloinfante.rowerplus.database.models.SchemeStep
import it.paoloinfante.rowerplus.database.models.SchemeStepVariable
import it.paoloinfante.rowerplus.database.models.SchemeWithStepsAndVariables
import it.paoloinfante.rowerplus.database.repositories.SchemeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class AllSchemesViewModel @Inject constructor(private val schemeRepository: SchemeRepository) :
    ViewModel() {

    fun getAllSchemes(): Flow<List<SchemeWithStepsAndVariables>> {
        return schemeRepository.getAllSchemes()
    }

    suspend fun insertScheme(scheme: Scheme): Long {
        return schemeRepository.insertScheme(scheme)
    }

    suspend fun insertSchemeStep(schemeStep: SchemeStep): Long {
        return schemeRepository.insertSchemeStep(schemeStep)
    }

    suspend fun insertSchemeStepVariable(schemeStepVariable: SchemeStepVariable): Long {
        return schemeRepository.insertSchemeStepVariable(schemeStepVariable)
    }

    suspend fun insertSchemeWithStepsAndVariables(scheme: SchemeWithStepsAndVariables): Long {
        return schemeRepository.insertSchemeWithStepsAndVariables(scheme)
    }
}