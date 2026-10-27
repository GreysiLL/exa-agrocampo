package pe.upeu.agrosierra.domain.usecase
import pe.upeu.agrosierra.domain.repository.*
import pe.upeu.agrosierra.domain.model.*
class SincronizarUseCase(private val repo: AgroRepository) { suspend operator fun invoke()=repo.sincronizar() }
class GuardarLaborUseCase(private val repo: AgroRepository) { suspend operator fun invoke(labor: Labor)=repo.guardar(labor) }
class ObservarLotesUseCase(private val repo: AgroRepository) { operator fun invoke(valle: String,busqueda: String)=repo.lotes(valle,busqueda) }
